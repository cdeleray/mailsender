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

package com.github.cdeleray.mailsender;

import java.io.File;
import java.util.stream.Stream;

import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportListener;

/**
 * A {@code MailSender} object is designed to create and send a multipart 
 * message.
 *
 * @author Christophe Deleray
 */
public interface MailSender {
  /**
   * Adds a specified text to sent.
   * 
   * @param text the text of the message to be sent
   * @return this {@code MailSender} object
   */
  MailSender addText(String text);
  
  /**
   * Adds a file to send.
   *
   * @implSpec
   * The default implementation calls the {@link #addFile(String, File)} with 
   * the {@linkplain File#getName() name} of the given file as argument. 
   * Furthermore, it does nothing if {@code file} is {@code null}. 
   * 
   * @param file the file
   * @return this {@code MailSender} object
   */
  default MailSender addFile(File file) {
    return (file == null) ? this : addFile(file.getName(), file);
  }

  /**
   * Adds a file to send by specifying its name.
   *
   * @param fileName the name of the file
   * @param file the file
   * @return this {@code MailSender} object
   */
  MailSender addFile(String fileName, File file);

  /**
   * Adds a file to send by specifying its name, content and MIME type.
   * 
   * @param fileName the name of the file
   * @param fileContent the content of the file
   * @param mimeType the MIME type of the file
   * @return this {@code MailSender} object
   */
  MailSender addFile(String fileName, byte[] fileContent, String mimeType);

  /**
   * Adds an image to sent, specifying its "Content-ID" as its appears within
   * the HTML body of the mail (e.g in {@literal <img src="cid:id" ... />}).
   * <p>
   * Calling this method make this {@code MailSender} to send HTML content
   * by using the {@link #setHtmlMode()} method.
   * 
   * @param cid the "Content-ID" value associated with the image as in
   *            {@literal <img src="cid:id" ... />}
   * @param imageFile the file corresponding to the image
   * @return this {@code MailSender} object
   */
  MailSender addImage(String cid, File imageFile);
  
  /**
   * Adds an image to sent, specifying its "Content-ID" as its appears within
   * the HTML body of the mail (e.g in {@code <img src="cid:<em>id</em>" ... />}),
   * and its MIME type
   * <p>
   * Calling this method make this {@code MailSender} to send HTML content
   * by using the {@link #setHtmlMode()} method.
   *
   * @param cid the "Content-ID" value associated with the image as in
   *            {@literal <img src="cid:id" ... />}
   * @param imageContent the image content
   * @param mimeType the MIME type of the image
   * @return this {@code MailSender} object
   */
  MailSender addImage(String cid, byte[] imageContent, String mimeType);
  
  /**
   * Adds the specified recipient address.
   * 
   * @param address a recipient address
   * @return this {@code MailSender} object
   */
  MailSender addRecipient(String address);
  
  /**
   * Adds the specified recipient addresses.
   * 
   * @implSpec
   * The default implementation calls the {@link #addRecipient(String) addRecipient}
   * method for each entry of the {@code addresses} array. 
   * 
   * @param addresses some recipient addresses
   * @return this {@code MailSender} object
   */
  default MailSender addRecipients(String... addresses) {
    Stream.of(addresses).forEach(this::addRecipient);
    return this;
  }

  /**
   * Adds the specified CC (Carbon Copy) recipient address.
   * 
   * @param address a CC recipient address
   * @return this {@code MailSender} object
   */
  MailSender addRecipientCC(String address);
  
  /**
   * Adds the specified CC (Carbon Copy) recipient addresses.
   * 
   * @implSpec
   * The default implementation calls the {@link #addRecipientCC(String) addRecipientCC}
   * method for each entry of the {@code addresses} array. 
   * 
   * @param addresses some recipients CC addresses
   * @return this {@code MailSender} object
   */
  default MailSender addRecipientsCC(String... addresses) {
    Stream.of(addresses).forEach(this::addRecipientCC);
    return this;
  }

  /**
   * Adds the specified BCC (Blind Carbon Copy) recipient address.
   * 
   * @param address a BCC recipient address
   * @return this {@code MailSender} object
   */
  MailSender addRecipientBCC(String address);

  /**
   * Adds the specified BCC (Blind Carbon Copy) recipient addresses.
   * 
   * @implSpec
   * The default implementation calls the {@link #addRecipientBCC(String) addRecipientBCC}
   * method for each entry of the {@code addresses} array. 
   * 
   * @param addresses some recipients BCC addresses
   * @return this {@code MailSender} object
   */
  default MailSender addRecipientsBCC(String... addresses) {
    Stream.of(addresses).forEach(this::addRecipientBCC);
    return this;
  }
  
  /**
   * Adds a new {@link TransportListener} that handles events telling
   * that a message has been successfully delivered or not.
   * 
   * @param listener the listener to add
   * @return this {@code MailSender} object
   */
  MailSender addTransportListener(TransportListener listener);

  /**
   * Adds a new {@link ConnectionListener} that handles events whether telling
   * that a connection to the underlying host is opened, closed or disconnected.
   * 
   * @param listener the listener to add
   * @return this {@code MailSender} object
   */
  MailSender addConnectionListener(ConnectionListener listener);

  /**
   * Replaces by {@code address} the RFC 822 "From" header field
   * of the message to be sent.
   * 
   * @param address the RFC 822 "From" header field of the message to be sent
   * @return this {@code MailSender} object
   */
  MailSender setFrom(String address);

  /**
   * Replaces by {@code subject} the subject of the message to be sent.
   * 
   * @param subject the subject of the message to be sent
   * @return this {@code MailSender} object
   */
  MailSender setSubject(String subject);
  
  /**
   * Sends the message to all recipients previously added. 
   * 
   * @throws CannotSendMailException if any error occurs
   */
  void send() throws CannotSendMailException;

  /**
   * Switches to the text plain mode.  
   * 
   * @return this {@code MailSender} object
   */
  MailSender setTextMode();
  
  /**
   * Switches to the HTML mode.  
   * 
   * @return this {@code MailSender} object
   */
  MailSender setHtmlMode();
  
  /**
   * Replaces the recipient addresses by the given ones.
   * 
   * @param addresses some recipient addresses
   * @return this {@code MailSender} object
   */
  MailSender setRecipients(String... addresses);

  /**
   * Replaces the CC (Carbon Copy) recipient addresses by the given ones.
   * 
   * @param addresses some CC recipient addresses
   * @return this {@code MailSender} object
   */
  MailSender setRecipientsCC(String... addresses);

  /**
   * Replaces the BCC (Blind Carbon Copy) recipient addresses by the given ones.
   * 
   * @param addresses some BCC recipient addresses
   * @return this {@code MailSender} object
   */
  MailSender setRecipientsBCC(String... addresses);

  /**
   * Removes the specified recipient address.
   * 
   * @param address recipient address
   * @return this {@code MailSender} object
   */
  MailSender removeRecipient(String address);
  
  /**
   * Removes the specified CC (Carbon Copy) recipient address.
   * 
   * @param address a CC recipient address
   * @return this {@code MailSender} object
   */
  MailSender removeRecipientCC(String address);

  /**
   * Removes the specified BCC (Blind Carbon Copy) recipient address.
   * 
   * @param address a BCC recipient address
   * @return this {@code MailSender} object
   */
  MailSender removeRecipientBCC(String address);
  
  /**
   * Resets this {@code MailSender} object to its initial configuration.
   */
  void reset();
}