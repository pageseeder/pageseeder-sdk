package org.pageseeder.sdk.model;

import java.util.List;

/**
 * Lightweight PageSeeder result wrapper.
 *
 * @param total the total number of available results
 * @param start the result start offset
 * @param items the result items
 * @param <T> the result item type.
 */
public record ResultPage<T>(int total, int start, List<T> items) {

  public ResultPage {
    items = List.copyOf(items);
  }

  public int getTotal() {
    return this.total;
  }

  public int getStart() {
    return this.start;
  }

  public List<T> getItems() {
    return this.items;
  }
}
