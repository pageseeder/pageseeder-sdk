package org.pageseeder.sdk.model;

import java.util.List;

/**
 * Lightweight PageSeeder result wrapper.
 *
 * @param <T> the result item type.
 */
public final class ResultPage<T> {

  private final int total;
  private final int start;
  private final List<T> items;

  public ResultPage(int total, int start, List<T> items) {
    this.total = total;
    this.start = start;
    this.items = List.copyOf(items);
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
