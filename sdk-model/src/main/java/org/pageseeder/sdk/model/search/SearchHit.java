package org.pageseeder.sdk.model.search;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A single search hit.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SearchHit {

  private final double score;
  private final List<SearchField> fields;
  // racy single-check (EJ §83): volatile ensures safe publication; race is benign — both threads compute the same immutable map
  @SuppressWarnings("java:S3077")
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

  /**
   * Returns the first value for the specified field name as an {@code Integer}.
   *
   * @param name the field name
   * @return the first matching value as an {@code Integer}, or {@code null} if the field is absent
   * @throws NumberFormatException if the value cannot be parsed as an {@code int}
   */
  public @Nullable Integer firstInt(String name) {
    String v = firstValue(name);
    return v == null ? null : Integer.parseInt(v);
  }

  /**
   * Returns the first value for the specified field name as a {@code Long}.
   *
   * @param name the field name
   * @return the first matching value as a {@code Long}, or {@code null} if the field is absent
   * @throws NumberFormatException if the value cannot be parsed as a {@code long}
   */
  public @Nullable Long firstLong(String name) {
    String v = firstValue(name);
    return v == null ? null : Long.parseLong(v);
  }

  /**
   * Returns the first value for the specified field name as a {@code LocalDate}.
   *
   * @param name the field name
   * @return the first matching value as a {@code LocalDate}, or {@code null} if the field is absent
   * @throws java.time.format.DateTimeParseException if the value matches neither Lucene nor ISO-8601 date format
   */
  public @Nullable LocalDate firstLocalDate(String name) {
    List<SearchField> matching = fields(name);
    return matching.isEmpty() ? null : matching.get(0).asLocalDate();
  }

  /**
   * Returns the first value for the specified field name as an {@code Instant}.
   *
   * @param name the field name
   * @return the first matching value as an {@code Instant}, or {@code null} if the field is absent
   * @throws java.time.format.DateTimeParseException if the value matches neither Lucene nor ISO-8601 datetime format
   */
  public @Nullable Instant firstInstant(String name) {
    List<SearchField> matching = fields(name);
    return matching.isEmpty() ? null : matching.get(0).asInstant();
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
