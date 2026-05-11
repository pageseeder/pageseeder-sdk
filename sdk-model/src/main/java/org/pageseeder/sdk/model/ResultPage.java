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

  /**
   * Creates a result page with an immutable copy of the supplied items.
   *
   * @param total the total number of available results
   * @param start the result start offset
   * @param items the result items
   */
  public ResultPage {
    items = List.copyOf(items);
  }

  /**
   * Returns the total number of available results.
   *
   * @return the total number of available results
   */
  public int getTotal() {
    return this.total;
  }

  /**
   * Returns the result start offset.
   *
   * @return the result start offset
   */
  public int getStart() {
    return this.start;
  }

  /**
   * Returns the result items.
   *
   * @return the result items
   */
  public List<T> getItems() {
    return this.items;
  }
}
