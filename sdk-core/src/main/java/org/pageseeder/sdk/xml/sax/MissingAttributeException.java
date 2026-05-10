package org.pageseeder.sdk.xml.sax;

/**
 * Exception thrown when a required attribute is missing.
 */
public final class MissingAttributeException extends AttributeException {

  private static final long serialVersionUID = 1L;

  public MissingAttributeException(String name) {
    super(name, "Missing required attribute `" + name + "`");
  }
}
