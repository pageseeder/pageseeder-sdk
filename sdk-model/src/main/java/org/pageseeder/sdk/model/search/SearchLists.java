package org.pageseeder.sdk.model.search;

import java.util.Collections;
import java.util.List;

/**
 * Utility methods for working with lists in search results.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
final class SearchLists {

  private SearchLists() {
  }

  @SuppressWarnings("java:S2583") // Defensive null-check
  static <T> List<T> copy(List<T> items) {
    return items == null ? List.of() : List.copyOf(items);
  }

  static <T> List<T> trusted(List<T> items) {
    if (items.isEmpty()) {
      return List.of();
    }
    return Collections.unmodifiableList(items);
  }
}
