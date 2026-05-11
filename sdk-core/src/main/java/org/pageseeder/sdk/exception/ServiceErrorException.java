package org.pageseeder.sdk.exception;


/**
 * PageSeeder service error response.
 */
public final class ServiceErrorException extends HttpStatusException {

  /** Service error payload returned by PageSeeder. */
  private final ServiceError error;

  /**
   * Creates an exception for a PageSeeder service error response.
   *
   * @param statusCode the HTTP status code
   * @param error      the service error payload
   */
  public ServiceErrorException(int statusCode, ServiceError error) {
    super(statusCode, error.message());
    this.error = error;
  }

  /**
   * Returns the service error payload.
   *
   * @return the service error payload
   */
  public ServiceError getError() {
    return this.error;
  }
}
