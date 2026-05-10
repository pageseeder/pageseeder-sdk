package org.pageseeder.sdk.model;

import java.util.Objects;

/**
 * Immutable PageSeeder content block.
 */
public final class Content {

  private final String type;
  private final String value;

  public Content(String type, String value) {
    this.type = Objects.toString(type, "text/plain");
    this.value = Objects.toString(value, "");
  }

  public String getType() {
    return this.type;
  }

  public String getValue() {
    return this.value;
  }
}
