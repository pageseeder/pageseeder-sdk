package org.pageseeder.sdk.model.search;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchHitTest {

  private static final SearchHit HIT = new SearchHit(1.0, List.of(
      new SearchField("count",   SearchFieldKind.FIELD, "42"),
      new SearchField("size",    SearchFieldKind.FIELD, "9876543210"),
      new SearchField("date",    SearchFieldKind.FIELD, "20230402"),
      new SearchField("created", SearchFieldKind.FIELD, "20230402055526")
  ));

  // firstInt

  @Test
  void shouldReturnFirstInt() {
    assertEquals(42, HIT.firstInt("count"));
  }

  @Test
  void shouldReturnNullForAbsentFieldAsInt() {
    assertNull(HIT.firstInt("missing"));
  }

  @Test
  void shouldThrowNumberFormatExceptionForInvalidInt() {
    SearchHit hit = new SearchHit(1.0, List.of(new SearchField("x", SearchFieldKind.FIELD, "abc")));
    assertThrows(NumberFormatException.class, () -> hit.firstInt("x"));
  }

  // firstLong

  @Test
  void shouldReturnFirstLong() {
    assertEquals(9_876_543_210L, HIT.firstLong("size"));
  }

  @Test
  void shouldReturnNullForAbsentFieldAsLong() {
    assertNull(HIT.firstLong("missing"));
  }

  @Test
  void shouldThrowNumberFormatExceptionForInvalidLong() {
    SearchHit hit = new SearchHit(1.0, List.of(new SearchField("x", SearchFieldKind.FIELD, "abc")));
    assertThrows(NumberFormatException.class, () -> hit.firstLong("x"));
  }

  // firstLocalDate

  @Test
  void shouldReturnFirstLocalDate() {
    assertEquals(LocalDate.of(2023, 4, 2), HIT.firstLocalDate("date"));
  }

  @Test
  void shouldReturnNullForAbsentFieldAsLocalDate() {
    assertNull(HIT.firstLocalDate("missing"));
  }

  // firstInstant

  @Test
  void shouldReturnFirstInstant() {
    assertEquals(Instant.parse("2023-04-02T05:55:26Z"), HIT.firstInstant("created"));
  }

  @Test
  void shouldReturnNullForAbsentFieldAsInstant() {
    assertNull(HIT.firstInstant("missing"));
  }
}
