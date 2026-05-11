package org.pageseeder.sdk.exception;

/**
 * Base exception for the modern PageSeeder SDK.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class PageSeederException extends RuntimeException {

  /**
   * Creates a PageSeeder SDK exception.
   *
   * @param message the exception message
   */
  public PageSeederException(String message) {
    super(message);
  }

  /**
   * Creates a PageSeeder SDK exception with a cause.
   *
   * @param message the exception message
   * @param cause   the exception cause
   */
  public PageSeederException(String message, Throwable cause) {
    super(message, cause);
  }
}
