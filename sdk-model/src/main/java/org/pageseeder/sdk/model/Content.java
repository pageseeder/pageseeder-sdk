package org.pageseeder.sdk.model;

import java.util.Objects;

/**
 * Immutable PageSeeder content block.
 *
 * @param type  the content media type
 * @param value the content value
 */
public record Content(String type, String value) {

  public Content {
    type = Objects.toString(type, "text/plain");
    value = Objects.toString(value, "");
  }

  public String getType() {
    return this.type;
  }

  public String getValue() {
    return this.value;
  }
}
