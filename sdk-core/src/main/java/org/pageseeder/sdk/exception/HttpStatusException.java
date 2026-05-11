package org.pageseeder.sdk.exception;

/**
 * Non-success HTTP status response.
 */
public class HttpStatusException extends PageSeederException {

  /** HTTP status code returned by PageSeeder. */
  private final int statusCode;

  /**
   * Creates an exception for a non-success HTTP response.
   *
   * @param statusCode the HTTP status code
   * @param message    the exception message
   */
  public HttpStatusException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  /**
   * Returns the HTTP status code.
   *
   * @return the HTTP status code
   */
  public int getStatusCode() {
    return this.statusCode;
  }
}
