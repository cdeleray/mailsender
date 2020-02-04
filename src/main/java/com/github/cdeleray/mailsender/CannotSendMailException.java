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

/**
 * A {@code CannotSendMailException} is the exception thrown by the
 * {@link MailSender#send() send} method of a {@link MailSender}.
 *
 * @author Christophe Deleray
 */
public class CannotSendMailException extends RuntimeException {
  /** Private serial version unique ID to ensure serialization compatibility. */
  private static final long serialVersionUID = -71351019665900203L;

  /**
   * Creates a new {@code CannotSendMailException} object with {@code null} as 
   * its detail message.  The cause is not initialized, and may subsequently be
   * initialized by a call to {@link #initCause}.
   */
  public CannotSendMailException() {
  }

  /**
   * Creates a new CannotSendMailException object initialized with the given
   * detail message.
   * 
   * @param message the message of this exception
   */
  public CannotSendMailException(String message) {
    super(message);
  }

  /**
   * Creates a new CannotSendMailException object initialized with the given 
   * cause.
   * 
   * @param cause the cause of this exception
   */
  public CannotSendMailException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new CannotSendMailException object initialized with the given
   * detail message and cause.
   * 
   * @param message the message of this exception
   * @param cause the cause of this exception
   */
  public CannotSendMailException(String message, Throwable cause) {
    super(message, cause);
  }
}
