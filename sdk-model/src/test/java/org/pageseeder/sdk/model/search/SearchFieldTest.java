package org.pageseeder.sdk.model.search;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
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
    SearchField field = field("abc");
    assertThrows(NumberFormatException.class, field::asInt);
  }

  // asLong

  @Test
  void shouldParseLong() {
    assertEquals(9_876_543_210L, field("9876543210").asLong());
  }

  @Test
  void shouldThrowNumberFormatExceptionForInvalidLong() {
    SearchField field = field("abc");
    assertThrows(NumberFormatException.class, field::asLong);
  }

  // asLocalDate

  @Test
  void shouldParseLocalDateFromLuceneFormat() {
    assertEquals(LocalDate.of(2023, Month.APRIL, 2), field("20230402").asLocalDate());
  }

  @Test
  void shouldParseLocalDateFromIsoFormat() {
    assertEquals(LocalDate.of(2023, Month.APRIL, 2), field("2023-04-02").asLocalDate());
  }

  @Test
  void shouldThrowDateTimeParseExceptionForDatetimeValueInAsLocalDate() {
    SearchField field = field("20230402055526");
    assertThrows(DateTimeParseException.class, field::asLocalDate);
  }

  @Test
  void shouldThrowDateTimeParseExceptionForIsoDatetimeValueInAsLocalDate() {
    SearchField field = field("2023-04-02T05:55:26Z");
    assertThrows(DateTimeParseException.class, field::asLocalDate);
  }

  @Test
  void shouldThrowDateTimeParseExceptionForInvalidDate() {
    SearchField field = field("not-a-date");
    assertThrows(DateTimeParseException.class, field::asLocalDate);
  }

  // asInstant

  @Test
  void shouldParseInstantFromLuceneFormat() {
    SearchField field = field("20230402055526");
    assertEquals(Instant.parse("2023-04-02T05:55:26Z"), field.asInstant());
  }

  @Test
  void shouldParseInstantFromIsoFormat() {
    SearchField field = field("2023-04-02T05:55:26Z");
    assertEquals(Instant.parse("2023-04-02T05:55:26Z"), field.asInstant());
  }

  @Test
  void shouldThrowDateTimeParseExceptionForInvalidInstant() {
    SearchField field = field("not-a-datetime");
    assertThrows(DateTimeParseException.class, field::asInstant);
  }
}
