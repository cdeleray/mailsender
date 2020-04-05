/*
 * MIT License
 *
 * Copyright (c) 2020 Christophe Deleray
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.cdeleray.mailsender.javamail;

import com.github.cdeleray.mailsender.AbstractMailSender;
import com.github.cdeleray.mailsender.CannotSendMailException;
import com.github.cdeleray.mailsender.MailSender;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;

/**
 * The <a href="http://www.oracle.com/technetwork/java/javamail/index.html/">
 * JavaMail</a>-based implementation of the {@link MailSender} interface. 
 *
 * @author Christophe Deleray
 */
public class JavaMailSender extends AbstractMailSender {
  /** The {@code mail.smtp.host} header key. See the Appendix A of the 
   * JavaMail specification. */
  private static final String MAIL_SMTP_HOST = "mail.smtp.host";
  
  /** The {@code mail.from} header key. See the Appendix A of the JavaMail 
   * specification. */
  private static final String MAIL_FROM = "mail.from";

  /** The {@code mail.transport.protocol} header key. See the Appendix A 
   * of the JavaMail specification. */
  private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

  /** For logging purpose. */
  private static final Logger LOG = Logger.getLogger(JavaMailSender.class.getName());

  /** The SMTP server address. */
  private final String host;
  
  /** The authenticator that knows how to obtain authentication for the 
   * network connection. */
  private Authenticator authenticator;

  /** Will create {@link MimeBodyPart} instances. */
  private final MimeBodyPartFactory mimeBodyPartFactory = new MimeBodyPartFactory();

  /**
   *
   * Creates a new {@code JavaMailSender} object by specifying the SMTP 
   * server address.
   * 
   * @param smtpHost the SMTP server address
   */
  public JavaMailSender(String smtpHost) {
    this.host = smtpHost;
  }

  /**
   * Creates a new {@code JavaMailSender} object by specifying the SMTP server 
   * address, the login and the password.
   * 
   * @param smtpHost the SMTP server address
   * @param login the login 
   * @param password the password
   */
  public JavaMailSender(String smtpHost, String login, String password) {
    this(smtpHost, new PasswordAuthentication(login, password));
  }

  /**
   * Creates a new {@code JavaMailSender} object by specifying the SMTP server 
   * address and, the login and the password, both provided by the given
   * {@linkplain PasswordAuthentication} instance.
   * 
   * @param smtpHost the SMTP server address
   * @param authentication holds the login and the password
   */
  public JavaMailSender(String smtpHost, PasswordAuthentication authentication) {
    this(smtpHost);
    this.authenticator = new Authenticator() {
      @Override 
      protected PasswordAuthentication getPasswordAuthentication() {
        return authentication;
      }
    };
  }
  
  @Override
  public void send() throws CannotSendMailException {
    Properties props = new Properties();
    props.put(MAIL_SMTP_HOST, host);
    props.put(MAIL_FROM, from);
    props.put(MAIL_TRANSPORT_PROTOCOL, "smtp");
    
    Transport transport = null;

    try {
      MimeMultipart multipart = new MimeMultipart();

      MimeBodyPart textPart = new MimeBodyPart();

      if(textMode) {
        textPart.setText(text.toString(), UTF_8.name());
      }
      else {
        try {
          textPart.setDataHandler(new DataHandler(new ByteArrayDataSource(text.toString(), "text/html")));
        } catch (IOException e) {
          throw new MessagingException(e.getMessage(), e);
        }
      }

      multipart.addBodyPart(textPart);
  
      for (Map.Entry<String, Collection<File>> e : namedFiles.entrySet()) {
        String fileName = e.getKey();
        for (File file : e.getValue()) {
          multipart.addBodyPart(mimeBodyPartFactory.newFileBodyPart(fileName, file));
        }
      }
        
      for (Map.Entry<String, Collection<Object[]>> e : namedMimeTypedFileContent.entrySet()) {
        String fileName = e.getKey();
        for (Object[] mimeTypedFileContent : e.getValue()) {
          byte[] fileContent = (byte[])mimeTypedFileContent[0];
          String mimeType = (String)mimeTypedFileContent[1];
          multipart.addBodyPart(mimeBodyPartFactory.newFileBodyPart(fileName, fileContent, mimeType));
        }
      }

      for (Map.Entry<String, File> e : images.entrySet()) {
        String imageId = e.getKey();
        File imageFile = e.getValue();
        multipart.addBodyPart(mimeBodyPartFactory.newImageBodyPart(imageId, imageFile));
      }
      
      for (Map.Entry<String, Object[]> e : namedMimeTypedImages.entrySet()) {
        String imageId = e.getKey();
        Object[] mimeTypedImage = e.getValue();
        byte[] imageContent = (byte[]) mimeTypedImage[0];
        String mimeType = (String) mimeTypedImage[1];
        multipart.addBodyPart(mimeBodyPartFactory.newImageBodyPart(imageId, imageContent, mimeType));
      }
      
      beforeCreateSession(props);      
      
      Session session = Session.getInstance(props, authenticator);
      
      MimeMessage message = new MimeMessage(session);
      message.addRecipients(TO, asAddresses(recipients));
      message.addRecipients(CC, asAddresses(recipientsCC));
      message.addRecipients(BCC, asAddresses(recipientsBCC));
      message.setSentDate(new Date());
      message.setFrom(new InternetAddress(from));
      message.setSubject(subject);
      message.setContent(multipart);
      message.saveChanges();
        
      transport = session.getTransport();
      
      transportListeners.forEach(transport::addTransportListener);
      connectionListeners.forEach(transport::addConnectionListener);
      
      transport.connect();      
      
      beforeSendMessage(transport, message);
     
      LOG.finest(this::toString);
      
      transport.sendMessage(message, message.getAllRecipients());
    }
    catch (MessagingException e) {
      throw new CannotSendMailException("Cannot send the message.", e);
    }
    finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (MessagingException ignore) {
        }
      }
    } 
  }

  /** Converts the given recipients as {@link Address} objects. */
  private Address[] asAddresses(Set<String> recipients) throws AddressException {
    int count = recipients.size();
    Address[] addresses = new Address[count];
    int i = 0;
    for(String recipient : recipients) {
      addresses[i++] = new InternetAddress(recipient);
    }
    
    return addresses;
  }
    
  /**
   * Called by the {@link #send()} method before the given {@link Transport} 
   * object gets used to send the given message.
   * <p>
   * The default implementation of this method does nothing but any subclass 
   * can override it when needing some custom configuration of the given 
   * {@link Transport} object and/or the given message.
   * 
   * @param transport handles the transport of messages
   * @param message the message to be sent
   */
  protected void beforeSendMessage(Transport transport, Message message) {}

  /**
   * Called by the {@link #send()} method before the a new {@link Session} 
   * gets created from the given properties.
   * <p>
   * The default implementation of this method does nothing but any subclass 
   * can override it when needing to add some custom properties.
   * 
   * @param props the properties a new Session will be created from
   */
  protected void beforeCreateSession(Properties props) {}
}