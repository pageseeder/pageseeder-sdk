package org.pageseeder.sdk.model.search;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

  /**
   * Returns the value as an {@code int}.
   *
   * @return the value parsed as an {@code int}
   * @throws NumberFormatException if the value cannot be parsed as an {@code int}
   */
  public int asInt() {
    return Integer.parseInt(this.value);
  }

  /**
   * Returns the value as a {@code long}.
   *
   * @return the value parsed as a {@code long}
   * @throws NumberFormatException if the value cannot be parsed as a {@code long}
   */
  public long asLong() {
    return Long.parseLong(this.value);
  }

  /**
   * Returns the value as a {@code LocalDate}.
   *
   * <p>Tries Lucene compact format ({@code yyyyMMdd}) first, then ISO-8601 ({@code yyyy-MM-dd}).
   *
   * <p>If the value is a datetime, use {@code asInstant()} instead. To get the UTC date from a
   * datetime call {@code asInstant().atOffset(ZoneOffset.UTC).toLocalDate()}.
   *
   * @return the value parsed as a {@code LocalDate}
   * @throws DateTimeParseException if the value is a datetime or matches neither date format
   */
  public LocalDate asLocalDate() {
    if (this.value.length() == 14 || this.value.indexOf('T') >= 0) {
      throw new DateTimeParseException(
          "Value is a datetime; use asInstant(), or asInstant().atOffset(ZoneOffset.UTC).toLocalDate() for the UTC date",
          this.value, 0);
    }
    try {
      return LocalDate.parse(this.value, LUCENE_DATE);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(this.value);
    }
  }

  /**
   * Returns the value as an {@code Instant}.
   *
   * <p>Tries Lucene compact format ({@code yyyyMMddHHmmss}, UTC) first, then ISO-8601.
   *
   * @return the value parsed as an {@code Instant}
   * @throws DateTimeParseException if the value matches neither format
   */
  public Instant asInstant() {
    try {
      return LocalDateTime.parse(this.value, LUCENE_DATETIME).toInstant(ZoneOffset.UTC);
    } catch (DateTimeParseException e) {
      return Instant.parse(this.value);
    }
  }

  private static final DateTimeFormatter LUCENE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter LUCENE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
}
