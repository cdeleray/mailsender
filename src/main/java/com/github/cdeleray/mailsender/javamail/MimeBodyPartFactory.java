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

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;

/**
 * An abstract factory of {@link MimeBodyPart} instances.
 *
 * @author Christophe Deleray
 */
class MimeBodyPartFactory {
  /**
   * Creates the {@link MimeBodyPart} associated with the specified file.
   *
   * @param fileName the name of the file
   * @param file the file
   * @return a new {@link MimeBodyPart} for the given file
   * @throws MessagingException if any error occurs when building the {@link MimeBodyPart}
   */
  public MimeBodyPart newFileBodyPart(String fileName, File file) throws MessagingException {
    return newFileBodyPart(fileName, new FileDataSource(file));
  }

  /**
   * Creates the {@link MimeBodyPart} associated with the specified file.
   *
   * @param fileName the name of the file
   * @param content the content of the file
   * @param mimeType the MIME type of the file
   * @return a new {@link MimeBodyPart} for the given file
   * @throws MessagingException if any error occurs when building the {@link MimeBodyPart}
   */
  public MimeBodyPart newFileBodyPart(String fileName, byte[] content, String mimeType) throws MessagingException {
    return newFileBodyPart(fileName, new ByteArrayDataSource(content, mimeType));
  }

  /**
   * Creates the {@link MimeBodyPart} associated with the specified image
   * designed by the given "Content-ID" in the HTML content of the mail
   * (e.g in {@literal <img src="cid:id" ... />}) and its MIME type.
   *
   * @param cid the "Content-ID" header field of this body part associated
   *            with the image
   * @param image the image content
   * @param mimeType the MIME type of the image
   * @return a new {@link MimeBodyPart} for the given image
   * @throws MessagingException if any error occurs when building the {@link MimeBodyPart}
   */
  public MimeBodyPart newImageBodyPart(String cid, byte[] image, String mimeType) throws MessagingException {
    return newImageBodyPart(cid, new ByteArrayDataSource(image, mimeType));
  }

  /**
   * Creates the {@link MimeBodyPart} associated with the specified image
   * designed by the given "Content-ID" in the HTML content of the mail
   * (e.g in {@literal <img src="cid:id" ... />}).
   *
   * @param cid the "Content-ID" header field of this body part associated
   *            with the image
   * @param image the image
   * @return a new {@link MimeBodyPart} for the given image
   * @throws MessagingException if any error occurs when building the {@link MimeBodyPart}
   */
  public MimeBodyPart newImageBodyPart(String cid, File image) throws MessagingException {
    return newImageBodyPart(cid, new FileDataSource(image));
  }

  /** Creates a new {@link MimeBodyPart} builds from a file source. */
  private MimeBodyPart newFileBodyPart(String name, DataSource dataSource) throws MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    part.setDataHandler(new DataHandler(dataSource));
    part.setFileName(name);
    return part;
  }

  /** Creates a new {@link MimeBodyPart} builds from an image source. */
  private MimeBodyPart newImageBodyPart(String cid, DataSource dataSource) throws MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    part.setDataHandler(new DataHandler(dataSource));
    part.setContentID(cid);
    part.setDisposition(MimeBodyPart.INLINE);
    return part;
  }
}