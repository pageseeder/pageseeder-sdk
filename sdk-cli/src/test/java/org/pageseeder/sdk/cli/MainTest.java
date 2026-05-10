package org.pageseeder.sdk.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MainTest {

  @Test
  void shouldPrintHelpWhenNoArgumentsAreProvided() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new Main().run(
        java.util.List.of(),
        new PrintStream(out, true, StandardCharsets.UTF_8),
        new PrintStream(err, true, StandardCharsets.UTF_8));

    assertEquals(0, exitCode);
    assertTrue(out.toString(StandardCharsets.UTF_8).contains("Commands:"));
    assertEquals("", err.toString(StandardCharsets.UTF_8));
  }

  @Test
  void shouldReturnErrorForUnknownCommand() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new Main().run(
        java.util.List.of("wat"),
        new PrintStream(out, true, StandardCharsets.UTF_8),
        new PrintStream(err, true, StandardCharsets.UTF_8));

    assertEquals(1, exitCode);
    assertTrue(err.toString(StandardCharsets.UTF_8).contains("Unknown command: wat"));
    assertTrue(err.toString(StandardCharsets.UTF_8).contains("help"));
  }
}
