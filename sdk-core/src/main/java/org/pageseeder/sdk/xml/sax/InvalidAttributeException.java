package org.pageseeder.sdk.xml.sax;

import org.jspecify.annotations.Nullable;

/**
 * Exception thrown when an attribute value is invalid.
 */
public class InvalidAttributeException extends AttributeException {

  private static final long serialVersionUID = 1L;

  public InvalidAttributeException(String name) {
    this(name, null);
  }

  public InvalidAttributeException(String name, @Nullable Throwable cause) {
    super(name, "Invalid attribute `" + name + "`", cause);
  }
}
