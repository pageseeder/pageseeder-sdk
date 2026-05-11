package org.pageseeder.sdk.exception;

/**
 * Transport-level SDK failure.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TransportException extends PageSeederException {

  /**
   * Creates a transport exception with a cause.
   *
   * @param message the exception message
   * @param cause   the exception cause
   */
  public TransportException(String message, Throwable cause) {
    super(message, cause);
  }
}
