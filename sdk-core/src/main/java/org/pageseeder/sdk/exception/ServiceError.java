package org.pageseeder.sdk.exception;

/**
 * PageSeeder service error returned by the API.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ServiceError {

  private final String id;
  private final String message;

  public ServiceError(String id, String message) {
    this.id = id;
    this.message = message;
  }

  public String getId() {
    return this.id;
  }

  public String getMessage() {
    return this.message;
  }
}
