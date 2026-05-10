package org.pageseeder.sdk.exception;

/**
 * Payload parsing or serialization failure.
 */
public final class ParsingException extends PageSeederException {

  public ParsingException(String message) {
    super(message);
  }

  public ParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}
