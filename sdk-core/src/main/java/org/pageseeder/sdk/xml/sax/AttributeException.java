package org.pageseeder.sdk.xml.sax;

import org.jspecify.annotations.Nullable;

/**
 * Base class for SAX attribute parsing failures.
 */
public abstract class AttributeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String name;

  protected AttributeException(String name) {
    this.name = name;
  }

  protected AttributeException(String name, String message) {
    super(message);
    this.name = name;
  }

  protected AttributeException(String name, String message, @Nullable Throwable cause) {
    super(message, cause);
    this.name = name;
  }

  public String getAttributeName() {
    return this.name;
  }
}
