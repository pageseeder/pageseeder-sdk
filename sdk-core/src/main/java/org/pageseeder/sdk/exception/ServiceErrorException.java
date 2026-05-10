package org.pageseeder.sdk.exception;


/**
 * PageSeeder service error response.
 */
public final class ServiceErrorException extends HttpStatusException {

  private final ServiceError error;

  public ServiceErrorException(int statusCode, ServiceError error) {
    super(statusCode, error.getMessage());
    this.error = error;
  }

  public ServiceError getError() {
    return this.error;
  }
}
