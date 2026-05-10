package org.pageseeder.sdk.exception;

/**
 * Base exception for the modern PageSeeder SDK.
 */
public class PageSeederException extends RuntimeException {

  public PageSeederException(String message) {
    super(message);
  }

  public PageSeederException(String message, Throwable cause) {
    super(message, cause);
  }
}
