package org.pageseeder.sdk.cli;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VersionCommandTest {

  private HttpServer server;
  private URI baseUri;

  @BeforeEach
  void setUp() throws Exception {
    this.server = HttpServer.create(new InetSocketAddress(0), 0);
    this.server.start();
    this.baseUri = URI.create("http://localhost:" + this.server.getAddress().getPort());
  }

  @AfterEach
  void tearDown() {
    this.server.stop(0);
  }

  @Test
  void shouldPrintRemoteVersion() {
    this.server.createContext("/ps/api/version.xml", exchange -> {
      byte[] payload = read("/fixtures/version.xml");
      exchange.getResponseHeaders().add("Content-Type", "application/xml");
      exchange.sendResponseHeaders(200, payload.length);
      exchange.getResponseBody().write(payload);
      exchange.close();
    });

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new VersionCommand().run(
        List.of(this.baseUri.toString()),
        new PrintStream(out, true, StandardCharsets.UTF_8),
        new PrintStream(err, true, StandardCharsets.UTF_8));

    assertEquals(0, exitCode);
    assertEquals("5.9804" + System.lineSeparator(), out.toString(StandardCharsets.UTF_8));
    assertEquals("", err.toString(StandardCharsets.UTF_8));
  }

  @Test
  void shouldRejectMissingOrigin() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new VersionCommand().run(
        List.of(),
        new PrintStream(out, true, StandardCharsets.UTF_8),
        new PrintStream(err, true, StandardCharsets.UTF_8));

    assertEquals(1, exitCode);
    assertTrue(err.toString(StandardCharsets.UTF_8).contains("Usage: pageseeder-sdk version <api-origin>"));
  }

  private static byte[] read(String path) throws IOException {
    try (InputStream in = VersionCommandTest.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IOException("Missing resource: " + path);
      }
      return in.readAllBytes();
    }
  }
}
