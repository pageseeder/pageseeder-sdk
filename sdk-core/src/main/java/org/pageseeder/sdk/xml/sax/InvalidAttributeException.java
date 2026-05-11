package org.pageseeder.sdk.xml.sax;

import org.jspecify.annotations.Nullable;

import java.io.Serial;

/**
 * Exception thrown when an attribute value is invalid.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class InvalidAttributeException extends AttributeException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Creates an invalid-attribute exception.
   *
   * @param name the attribute name
   */
  public InvalidAttributeException(String name) {
    this(name, null);
  }

  /**
   * Creates an invalid-attribute exception with a cause.
   *
   * @param name  the attribute name
   * @param cause the exception cause
   */
  public InvalidAttributeException(String name, @Nullable Throwable cause) {
    super(name, "Invalid attribute `" + name + "`", cause);
  }
}
