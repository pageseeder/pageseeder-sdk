package org.pageseeder.sdk.xml.sax;

import java.io.Serial;

/**
 * Exception thrown when a required attribute is missing.
 */
public final class MissingAttributeException extends AttributeException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Creates a missing-attribute exception.
   *
   * @param name the missing attribute name
   */
  public MissingAttributeException(String name) {
    super(name, "Missing required attribute `" + name + "`");
  }
}
