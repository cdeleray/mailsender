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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.testng.Assert.*;

/**
 * Test class for {@link JavaMailSender}.
 *
 * @author Christophe Deleray
 */
@Test
public class JavaMailSenderTest {
  private Path file;
  private Path file2;
  private FileTypeMap fileTypeMap;
  private byte[] data;
  private TransportListener transportListener;
  private TransportEvent transportEvent;

  private String subject;
  private String from;
  private String recipient;
  private String recipient2;
  private String recipientCC;
  private String htmlContent;
  private String smtpHost;

  @BeforeClass
  void beforeClass() throws IOException {
    file = Files.createTempFile("tmp", ".txt");
    file.toFile().deleteOnExit();

    file2 = Files.createTempFile("tmp", ".txt");
    file2.toFile().deleteOnExit();

    Files.writeString(file, "hello world!");
    Files.writeString(file2, "hello world!");

    fileTypeMap = FileTypeMap.getDefaultFileTypeMap();

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    Files.copy(file, bout);
    data = bout.toByteArray();

    transportListener = new TransportListener() {
      @Override
      public void messageDelivered(TransportEvent e) {
        transportEvent = e;
      }
      @Override
      public void messageNotDelivered(TransportEvent e) {
        transportEvent = e;
      }
      @Override
      public void messagePartiallyDelivered(TransportEvent e) {
        transportEvent = e;
      }
    };

    from = "me.myslef@and.i.com";
    subject = "here, some accents : ééééé àààààà";
    recipient = "john.doe@unknown.com";
    recipient2 = "john2.doe@unknown.com";
    recipientCC = "jane.doe@unknown.com";
    htmlContent = "<html><head><title/></head><body><b>HELLO</b></body></html>";
    smtpHost = "dummy";
  }

  @AfterMethod
  void afterMethod() {
    transportEvent = null;
  }

  private Address[] array(String... adresses) throws MessagingException {
    Address[] adr = new Address[adresses.length];

    for (int i = 0; i < adr.length; i++) {
      adr[i] = new InternetAddress(adresses[i]);
    }

    return adr;
  }

  /** Counts the number of {@link BodyPart} instances with the given
   * MIME type and contained within the given {@link MimeMultipart}. */
  private int countBodyPart(MimeMultipart mimeMultipart, String mimeType) throws MessagingException {
    int count = 0;
    for (int i = 0, len = mimeMultipart.getCount(); i < len; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);

      if (mimeType.equals(bodyPart.getDataHandler().getContentType())) {
        ++count;
      }
    }

    return count;
  }

  /** Returns the {@link BodyPart} instances with the given
   * MIME type and contained within the given {@link MimeMultipart}. */
  private List<BodyPart> bodyParts(MimeMultipart mimeMultipart, String mimeType) throws MessagingException {
    List<BodyPart> bodyParts = new ArrayList<>();

    for (int i = 0, len = mimeMultipart.getCount(); i < len; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);

      if (mimeType.equals(bodyPart.getDataHandler().getContentType())) {
        bodyParts.add(bodyPart);
      }
    }

    return bodyParts;
  }

  /** Returns the first {@link BodyPart} instance with the given
   * MIME type and contained within the given {@link MimeMultipart}. */
  private Optional<BodyPart> bodyPart(MimeMultipart mimeMultipart, String mimeType) throws MessagingException {
    List<BodyPart> bodyParts = bodyParts(mimeMultipart, mimeType);
    return bodyParts.isEmpty() ? Optional.empty() : Optional.of(bodyParts.get(0));
  }

  /**
   * Tests {@link JavaMailSender#send()} method for an HTML-based email.
   *
   * @throws Exception if any error occurs
   */
  public void testSendHtml() throws Exception {
    new JavaMailSender(smtpHost)
        .setHtmlMode()
        .setSubject(subject)
        .setFrom(from)
        .addRecipient(recipient)
        .addRecipient(recipient)
        .addRecipient(recipient2)
        .addRecipientCC(recipientCC)
        .addText(htmlContent)
        .addFile(file.toFile())
        .addFile("hi.txt", file.toFile())
        .addFile("hi.txt", file2.toFile())
        .addFile("hello.txt", data, fileTypeMap.getContentType(file.toFile()))
        .addFile("hello2.txt", data, fileTypeMap.getContentType(file2.toFile()))
        .addTransportListener(transportListener)
        .send();

    Message message = transportEvent.getMessage();

    assertEquals(message.getFrom(), array(from));
    assertEquals(message.getSubject(), subject);
    assertEquals(message.getRecipients(TO), array(recipient, recipient2));
    assertEquals(message.getRecipients(CC), array(recipientCC));

    assertTrue(message instanceof MimeMessage);

    MimeMessage mimeMessage = (MimeMessage)message;
    Object content = mimeMessage.getContent();

    assertTrue(content instanceof  MimeMultipart);

    MimeMultipart mimeMultipart = (MimeMultipart) content;

    assertEquals(mimeMultipart.getCount(), 6);
    assertEquals(countBodyPart(mimeMultipart, "text/html"), 1);

    BodyPart textBodyPart = bodyPart(mimeMultipart, "text/html").orElseThrow(AssertionError::new);

    assertTrue((textBodyPart instanceof MimeBodyPart));

    MimeBodyPart textMimeBodyPart = (MimeBodyPart) textBodyPart;

    assertEquals(textBodyPart.getContent(), htmlContent);
  }
}