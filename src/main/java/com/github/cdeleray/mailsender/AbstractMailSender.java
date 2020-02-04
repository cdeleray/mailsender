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

import com.github.cdeleray.multimap.MultiMap;

import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@code AbstractMailSender} class provides a skeleton 
 * implementation to minimize the effort required to implement the 
 * {@link MailSender} interface.
 *
 * @author Christophe Deleray
 */
public abstract class AbstractMailSender implements MailSender {
  /** The default mail subject. */
  protected static final String NO_SUBJECT = "no subject";
  
  /** Determines if the current content is in plain text. This 
   * {@code true} by default. */
  protected boolean textMode = true;

  /** The mail text content. */
  protected StringBuilder text = new StringBuilder(1024);
  
  /** The mail subject. */
  protected String subject = NO_SUBJECT;
  
  /** The address of the sender. */
  protected String from;
  
  /** A map containing {file name, file(s)) pairs. */
  protected final MultiMap<String,File> namedFiles = new MultiMap<>();
  
  /** A map containing {name, <em>mimeTypedFileContent</em>} pairs where
   * <em>mimeTypedFileContent</em> is a list of 2-length arrays, each containing
   * a {file content and MIME type} pair. */
  protected final MultiMap<String, Object[]> namedMimeTypedFileContent = new MultiMap<>();
  
  /** A map containing {image name, file} pairs. */
  protected final Map<String, File> images = new HashMap<>();
  
  /** A map containing {image name, <em>mimeTypedData</em>} pairs where 
   * <em>mimeTypedData</em> is a list of 2-length arrays, each containing
   * a {data byte array and MIME type} pair. */
  protected final Map<String, Object[]> namedMimeTypedImages = new HashMap<>();

  /** The recipients addresses. */
  protected final Set<String> recipients = new LinkedHashSet<>();
    
  /** The CC (Carbon Copy) recipients addresses. */
  protected final Set<String> recipientsCC = new LinkedHashSet<>();

  /** The BCC (Blind Carbon Copy) recipients addresses. */
  protected final Set<String> recipientsBCC = new LinkedHashSet<>();

  /** Some listeners that handle events telling whether messages have 
   * been successfully delivered or not. */
  protected final List<TransportListener> transportListeners = new ArrayList<>();
  
  /** Some listeners that handle events whether telling that a connection 
   * to the underlying host is opened, closed or disconnected. */
  protected final List<ConnectionListener> connectionListeners = new ArrayList<>();

  @Override
  public MailSender addFile(String fileName, File file) {
    namedFiles.putValue(fileName, file);
    return this;
  }

  @Override
  public MailSender addFile(String fileName, byte[] fileContent, String mimeType) {
    namedMimeTypedFileContent.putValue(fileName, new Object[] { fileContent.clone(), mimeType });
    return this;
  }

  @Override
  public MailSender addImage(String imageId, byte[] imageContent, String mimeType) {
    namedMimeTypedImages.put(imageId, new Object[] { imageContent.clone(), mimeType });
    setHtmlMode();
    return this;
  }

  @Override
  public MailSender addImage(String imageId, File imageFile) {
    images.put(imageId, imageFile);
    setHtmlMode();
    return this;
  }

  @Override
  public MailSender addRecipient(String address) {
    recipients.add(address);
    return this;
  }
  
  @Override
  public MailSender addRecipientCC(String address) {
    recipientsCC.add(address);
    return this;
  }

  @Override
  public MailSender addRecipientBCC(String address) {
    recipientsBCC.add(address);
    return this;
  }

  @Override
  public MailSender addText(String text) {
    this.text.append(text);
    return this;
  }

  @Override
  public MailSender addTransportListener(TransportListener listener) {
    transportListeners.add(listener);
    return this;
  }

  @Override
  public MailSender addConnectionListener(ConnectionListener listener) {
    connectionListeners.add(listener);
    return this;
  }

  @Override
  public MailSender setFrom(String address) {
    this.from = address;
    return this;
  }

  @Override
  public MailSender setSubject(String subject) {
    this.subject = (subject == null) ? NO_SUBJECT : subject;
    return this;
  }

  @Override
  public MailSender setHtmlMode() {
    this.textMode = false;
    return this;
  }

  @Override
  public MailSender setTextMode() {
    this.textMode = true;
    return this;
  }

  @Override
  public void reset() {
    this.text = new StringBuilder(1024);
    this.namedFiles.clear();
    this.namedMimeTypedFileContent.clear();
    this.recipients.clear();
    this.recipientsCC.clear();
    this.recipientsBCC.clear();
    this.transportListeners.clear();
    this.connectionListeners.clear();
    this.subject = NO_SUBJECT;
    this.from = null;
  }  

  @Override
  public MailSender removeRecipient(String address) {
    recipients.remove(address);
    return this;
  }

  @Override
  public MailSender removeRecipientCC(String address) {
    recipientsCC.remove(address);
    return this;
  }

  @Override
  public MailSender removeRecipientBCC(String address) {
    recipientsBCC.remove(address);
    return this;
  }

  @Override
  public MailSender setRecipients(String... addresses) {
    return clearThenAddAll(recipients, addresses);
  }

  @Override
  public MailSender setRecipientsCC(String... addresses) {
    return clearThenAddAll(recipientsCC, addresses);
  }

  @Override
  public MailSender setRecipientsBCC(String... addresses) {
    return clearThenAddAll(recipientsBCC, addresses);
  }
  
  /** Clears the given collection and adds the given strings. */
  private MailSender clearThenAddAll(Collection<String> recipients, String... addresses) {
    recipients.clear();
    recipients.addAll(List.of(addresses));
    return this;
  }

  @Override 
  public String toString() {
    StringBuilder sb = new StringBuilder(1024);
    sb.append("MailSender {\n");
    sb.append('\t').append("subject: ").append(subject).append('\n');
    sb.append('\t').append("from: ").append(from).append('\n');
    sb.append('\t').append("text: ").append(text).append('\n');
    sb.append('\t').append("recipients: ").append(recipients).append('\n');
    sb.append('\t').append("recipients CC: ").append(recipientsCC).append('\n');
    sb.append('\t').append("recipients BCC: ").append(recipientsBCC).append('\n');

    sb.append('\t').append("files attachments:\n");
    namedFiles.forEach((name, files) ->
        files.forEach(f -> sb.append("\t\t").append(name).append(": ").append(f).append('\n')));
      
    sb.append('\t').append("data attachments:\n");
    namedMimeTypedFileContent.forEach((name, mimeTypedFileContent) ->
        mimeTypedFileContent.forEach(array -> sb.append("\t\t").append(name).append(": ").append(array[1]).append('\n')));
    
    sb.append('\t').append("mode: ").append(textMode ? "text" : "html").append('\n');
    sb.append("}");
    
    return sb.toString();    
  }  
}