package org.pageseeder.sdk.model.search;

import java.util.Collections;
import java.util.List;

final class SearchLists {

  private SearchLists() {
  }

  static <T> List<T> copy(List<T> items) {
    return items == null ? List.of() : List.copyOf(items);
  }

  static <T> List<T> trusted(List<T> items) {
    if (items == null || items.isEmpty()) {
      return List.of();
    }
    return Collections.unmodifiableList(items);
  }
}
