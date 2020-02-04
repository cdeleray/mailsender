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

import javax.activation.FileTypeMap;
import javax.mail.event.TransportAdapter;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Test class for {@link JavaMailSender}.
 *
 * @author Christophe Deleray
 */
public class JavaMailSenderTest {
  public static void main(String[] args) throws Exception {
    Path file = Files.createTempFile("tmp", ".txt");
    file.toFile().deleteOnExit();

    Path file2 = Files.createTempFile("tmp", ".txt");
    file2.toFile().deleteOnExit();

    Files.writeString(file, "hello world!");
    Files.writeString(file2, "hello world!");

    FileTypeMap ftm = FileTypeMap.getDefaultFileTypeMap();

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    Files.copy(file, bout);
    byte[] data = bout.toByteArray();

    TransportListener transportListener = new TransportAdapter() {
      @Override
      public void messageDelivered(TransportEvent e) {
        System.out.println("message delivered!");
        System.out.println("\tvalid sent adresses: " + Arrays.toString(e.getValidSentAddresses()));
      }

      @Override
      public void messageNotDelivered(TransportEvent e) {
        System.out.println("message not delivered!");
        System.out.println("\tinvalid adresses: " + Arrays.toString(e.getInvalidAddresses()));
        System.out.println("\tvalid unsent adresses: " + Arrays.toString(e.getValidUnsentAddresses()));
      }

      @Override
      public void messagePartiallyDelivered(TransportEvent e) {
        System.out.println("message partially delivered!");
        System.out.println("\tinvalid adresses: " + Arrays.toString(e.getInvalidAddresses()));
        System.out.println("\tvalid sent adresses: " + Arrays.toString(e.getValidSentAddresses()));
        System.out.println("\tvalid unsent adresses: " + Arrays.toString(e.getValidUnsentAddresses()));
      }
    };

    JavaMailSender sender = new JavaMailSender("lalala");
    sender.addText("<html><head><title/></head><body><b>HELLO</b></body></html>")
          .addRecipient("cdeleray@gmail.com")
          .addRecipient("cdeleray@gmail.com")
          .addRecipient("cdeleray@gmail.com")
          .addRecipientCC("cdeleray@hotmail.fr")
          .setSubject("HA HA HA en texte accentué!")
          .addFile(file.toFile())
          .addFile("hi.txt", file.toFile())
          .addFile("hi.txt", file2.toFile())
          .addFile("hello.txt", data, ftm.getContentType(file.toFile()))
          .addFile("hello2.txt", data, ftm.getContentType(file.toFile()))
          .setFrom("devine.qui.cest.fr")
          .addTransportListener(transportListener)
          .send();
  }
}