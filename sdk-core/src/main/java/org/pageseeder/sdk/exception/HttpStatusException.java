package org.pageseeder.sdk.exception;

/**
 * Non-success HTTP status response.
 */
public class HttpStatusException extends PageSeederException {

  private final int statusCode;

  public HttpStatusException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return this.statusCode;
  }
}
