package org.pageseeder.sdk.exception;

/**
 * Transport-level SDK failure.
 */
public final class TransportException extends PageSeederException {

  public TransportException(String message, Throwable cause) {
    super(message, cause);
  }
}
