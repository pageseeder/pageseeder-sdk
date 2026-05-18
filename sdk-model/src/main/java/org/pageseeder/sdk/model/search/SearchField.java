package org.pageseeder.sdk.model.search;

import java.util.Objects;

/**
 * A single field or extract returned for a search hit.
 *
 * @param name  the index field name
 * @param kind  whether the value came from a field or extract element
 * @param value the field value
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record SearchField(String name, SearchFieldKind kind, String value) {

  /**
   * Creates a search field.
   *
   * @param name  the index field name
   * @param kind  whether the value came from a field or extract element
   * @param value the field value
   */
  public SearchField {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(kind, "kind");
    value = Objects.toString(value, "");
  }
}
