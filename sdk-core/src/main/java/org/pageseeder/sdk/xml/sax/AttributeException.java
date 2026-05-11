package org.pageseeder.sdk.xml.sax;

import org.jspecify.annotations.Nullable;

import java.io.Serial;

/**
 * Base class for SAX attribute parsing failures.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AttributeException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  /** XML attribute name associated with the failure. */
  private final String name;

  /**
   * Creates an attribute exception for the supplied attribute name.
   *
   * @param name the attribute name
   */
  protected AttributeException(String name) {
    this.name = name;
  }

  /**
   * Creates an attribute exception with a message.
   *
   * @param name    the attribute name
   * @param message the exception message
   */
  protected AttributeException(String name, String message) {
    super(message);
    this.name = name;
  }

  /**
   * Creates an attribute exception with a message and cause.
   *
   * @param name    the attribute name
   * @param message the exception message
   * @param cause   the exception cause
   */
  protected AttributeException(String name, String message, @Nullable Throwable cause) {
    super(message, cause);
    this.name = name;
  }

  /**
   * Returns the XML attribute name that caused the exception.
   *
   * @return the attribute name
   */
  public String getAttributeName() {
    return this.name;
  }
}
