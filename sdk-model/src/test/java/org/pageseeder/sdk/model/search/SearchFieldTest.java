package org.pageseeder.sdk.model.search;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchFieldTest {

  private static SearchField field(String value) {
    return new SearchField("f", SearchFieldKind.FIELD, value);
  }

  // asInt

  @Test
  void shouldParseInt() {
    assertEquals(42, field("42").asInt());
  }

  @Test
  void shouldThrowNumberFormatExceptionForInvalidInt() {
    assertThrows(NumberFormatException.class, () -> field("abc").asInt());
  }

  // asLong

  @Test
  void shouldParseLong() {
    assertEquals(9_876_543_210L, field("9876543210").asLong());
  }

  @Test
  void shouldThrowNumberFormatExceptionForInvalidLong() {
    assertThrows(NumberFormatException.class, () -> field("abc").asLong());
  }

  // asLocalDate

  @Test
  void shouldParseLocalDateFromLuceneFormat() {
    assertEquals(LocalDate.of(2023, 4, 2), field("20230402").asLocalDate());
  }

  @Test
  void shouldParseLocalDateFromIsoFormat() {
    assertEquals(LocalDate.of(2023, 4, 2), field("2023-04-02").asLocalDate());
  }

  @Test
  void shouldThrowDateTimeParseExceptionForDatetimeValueInAsLocalDate() {
    assertThrows(DateTimeParseException.class, () -> field("20230402055526").asLocalDate());
  }

  @Test
  void shouldThrowDateTimeParseExceptionForIsoDatetimeValueInAsLocalDate() {
    assertThrows(DateTimeParseException.class, () -> field("2023-04-02T05:55:26Z").asLocalDate());
  }

  @Test
  void shouldThrowDateTimeParseExceptionForInvalidDate() {
    assertThrows(DateTimeParseException.class, () -> field("not-a-date").asLocalDate());
  }

  // asInstant

  @Test
  void shouldParseInstantFromLuceneFormat() {
    assertEquals(Instant.parse("2023-04-02T05:55:26Z"), field("20230402055526").asInstant());
  }

  @Test
  void shouldParseInstantFromIsoFormat() {
    assertEquals(Instant.parse("2023-04-02T05:55:26Z"), field("2023-04-02T05:55:26Z").asInstant());
  }

  @Test
  void shouldThrowDateTimeParseExceptionForInvalidInstant() {
    assertThrows(DateTimeParseException.class, () -> field("not-a-datetime").asInstant());
  }
}
