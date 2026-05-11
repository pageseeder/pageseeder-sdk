package org.pageseeder.sdk.exception;

import java.io.ObjectStreamField;
import java.io.Serial;
import java.io.Serializable;

/**
 * PageSeeder service error returned by the API.
 *
 * @param id      the service error ID
 * @param message the service error message
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record ServiceError(String id, String message) implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

}
