package org.pageseeder.sdk.exception;

/**
 * Transport-level SDK failure.
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
