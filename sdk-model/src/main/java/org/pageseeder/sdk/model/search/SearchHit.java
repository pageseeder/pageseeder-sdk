package org.pageseeder.sdk.model.search;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A single search hit.
 */
public final class SearchHit {

  private final double score;
  private final List<SearchField> fields;
  private volatile @Nullable Map<String, List<SearchField>> fieldsByName;

  /**
   * Creates a search hit.
   *
   * @param score  the search score
   * @param fields the returned fields and extracts
   */
  public SearchHit(double score, List<SearchField> fields) {
    this(score, fields, false);
  }

  SearchHit(double score, List<SearchField> fields, boolean trusted) {
    this.score = score;
    this.fields = trusted ? SearchLists.trusted(fields) : SearchLists.copy(fields);
  }

  static SearchHit trusted(double score, List<SearchField> fields) {
    return new SearchHit(score, fields, true);
  }

  /**
   * @return the search score
   */
  public double score() {
    return this.score;
  }

  /**
   * @return all fields and extracts in response order
   */
  public List<SearchField> fields() {
    return this.fields;
  }

  /**
   * Returns fields with the specified name in response order.
   *
   * @param name the field name
   * @return matching fields
   */
  public List<SearchField> fields(String name) {
    List<SearchField> values = byName().get(name);
    return values == null ? List.of() : values;
  }

  /**
   * Returns values for fields with the specified name in response order.
   *
   * @param name the field name
   * @return matching field values
   */
  public List<String> values(String name) {
    List<SearchField> matching = fields(name);
    if (matching.isEmpty()) {
      return List.of();
    }
    return new AbstractList<>() {
      @Override
      public String get(int index) {
        return matching.get(index).value();
      }

      @Override
      public int size() {
        return matching.size();
      }
    };
  }

  /**
   * Returns the first value for the specified field name.
   *
   * @param name the field name
   * @return the first matching value, or {@code null}
   */
  public @Nullable String firstValue(String name) {
    List<SearchField> matching = fields(name);
    return matching.isEmpty() ? null : matching.get(0).value();
  }

  private Map<String, List<SearchField>> byName() {
    Map<String, List<SearchField>> current = this.fieldsByName;
    if (current != null) {
      return current;
    }
    Map<String, List<SearchField>> indexed = new LinkedHashMap<>();
    for (SearchField field : this.fields) {
      indexed.computeIfAbsent(field.name(), ignored -> new ArrayList<>()).add(field);
    }
    indexed.replaceAll((name, values) -> Collections.unmodifiableList(values));
    current = Collections.unmodifiableMap(indexed);
    this.fieldsByName = current;
    return current;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof SearchHit that)) {
      return false;
    }
    return Double.compare(this.score, that.score) == 0 && this.fields.equals(that.fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.score, this.fields);
  }

  @Override
  public String toString() {
    return "SearchHit[score=" + this.score + ", fields=" + this.fields + ']';
  }
}
