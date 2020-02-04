package com.github.cdeleray.mailsender.javamail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;

/**
 * An abstract factory of {@link MimeBodyPart}.
 *
 * @author Christophe Deleray
 */
class MimeBodyPartFactory {
  /**
   * Creates the {@link MimeBodyPart} associated to the specified file.
   *
   * @param fileName the name of the file
   * @param file the file
   */
  public MimeBodyPart newFileBodyPart(String fileName, File file) throws MessagingException {
    return newFileBodyPart(fileName, new FileDataSource(file));
  }

  /**
   * Creates the {@link MimeBodyPart} associated to the specified file.
   *
   * @param fileName the name of the file
   * @param fileContent the content of the file
   * @param mimeType the MIME type of the file
   */
  public MimeBodyPart newFileBodyPart(String fileName, byte[] fileContent, String mimeType) throws MessagingException {
    return newFileBodyPart(fileName, new ByteArrayDataSource(fileContent, mimeType));
  }

  /**
   * Creates the {@link MimeBodyPart} associated to the specified image.
   */
  public MimeBodyPart newImageBodyPart(String imageId, byte[] imageContent, String mimeType) throws MessagingException {
    return newImageBodyPart(imageId, new ByteArrayDataSource(imageContent, mimeType));
  }

  /**
   * Creates the {@link MimeBodyPart} associated to the specified image.
   *
   */
  public MimeBodyPart newImageBodyPart(String imageId, File imageFile) throws MessagingException {
    return newImageBodyPart(imageId, new FileDataSource(imageFile));
  }

  private MimeBodyPart newFileBodyPart(String name, DataSource dataSource) throws MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    part.setDataHandler(new DataHandler(dataSource));
    part.setFileName(name);
    return part;
  }

  private MimeBodyPart newImageBodyPart(String imageId, DataSource dataSource) throws MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    part.setDataHandler(new DataHandler(dataSource));
    part.setContentID(imageId);
    part.setDisposition(MimeBodyPart.INLINE);
    return part;
  }
}