package org.pageseeder.sdk.exception;

/**
 * Payload parsing or serialization failure.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ParsingException extends PageSeederException {

  /**
   * Creates a parsing exception.
   *
   * @param message the exception message
   */
  public ParsingException(String message) {
    super(message);
  }

  /**
   * Creates a parsing exception with a cause.
   *
   * @param message the exception message
   * @param cause   the exception cause
   */
  public ParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}
