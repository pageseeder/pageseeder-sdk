package org.pageseeder.sdk.model;

import java.util.Objects;

/**
 * Immutable PageSeeder content block.
 *
 * @param type  the content media type
 * @param value the content value
 */
public record Content(String type, String value) {

  /**
   * Creates a content block, defaulting missing values to text content.
   *
   * @param type  the content media type
   * @param value the content value
   */
  public Content {
    type = Objects.toString(type, "text/plain");
    value = Objects.toString(value, "");
  }

  /**
   * Returns the content media type.
   *
   * @return the content media type
   */
  public String getType() {
    return this.type;
  }

  /**
   * Returns the content value.
   *
   * @return the content value
   */
  public String getValue() {
    return this.value;
  }
}
