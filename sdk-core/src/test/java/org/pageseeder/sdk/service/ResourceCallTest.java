package org.pageseeder.sdk.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.PageSeederInstance;
import org.pageseeder.sdk.auth.BasicCredentials;
import org.pageseeder.sdk.client.PageSeederClient;
import org.pageseeder.sdk.client.PageSeederRequest;
import org.pageseeder.sdk.client.PageSeederResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

final class ResourceCallTest {

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

  // --- factory methods ---

  @Test
  void docIdResolvesToDocidPath() {
    PageSeederRequest req = client().toRequest(ResourceCall.docId("ABC-123"));
    assertEquals("/ps/docid/ABC-123", req.uri().getPath());
    assertNull(req.format());
  }

  @Test
  void docIdRejectsBlank() {
    assertThrows(IllegalArgumentException.class, () -> ResourceCall.docId(""));
  }

  @Test
  void docIdRejectsInvalidCharacters() {
    assertThrows(IllegalArgumentException.class, () -> ResourceCall.docId("abc def"));
    assertThrows(IllegalArgumentException.class, () -> ResourceCall.docId("abc/def"));
    assertThrows(IllegalArgumentException.class, () -> ResourceCall.docId("abc.def"));
  }

  @Test
  void docIdAcceptsAllValidCharacters() {
    assertDoesNotThrow(() -> ResourceCall.docId("abc-ABC_123"));
  }

  @Test
  void uriResolvesToUriPath() {
    PageSeederRequest req = client().toRequest(ResourceCall.uri(42L));
    assertEquals("/ps/uri/42", req.uri().getPath());
    assertNull(req.format());
  }

  @Test
  void thumbnailResolvesToThumbnailPath() {
    PageSeederRequest req = client().toRequest(ResourceCall.thumbnail(99L));
    assertEquals("/ps/servlet/thumbnail/99", req.uri().getPath());
    assertNull(req.format());
  }

  @Test
  void imageResolvesToImagePath() {
    PageSeederRequest req = client().toRequest(ResourceCall.image(7L));
    assertEquals("/ps/servlet/image/7", req.uri().getPath());
    assertNull(req.format());
  }

  @Test
  void ofStringCreatesCallWithGivenTemplate() {
    PageSeederRequest req = client().toRequest(ResourceCall.of("/some/{segment}").pathVariable("segment", "value"));
    assertEquals("/ps/some/value", req.uri().getPath());
  }

  // --- toRequest URL assembly ---

  @Test
  void toRequestResolvesAgainstWebsiteRoot() {
    PageSeederClient client = PageSeederClient.builder()
        .instance(PageSeederInstance.builder()
            .websiteOrigin(this.baseUri)
            .apiOrigin(URI.create("http://api.example.com"))
            .build())
        .build();

    PageSeederRequest req = client.toRequest(ResourceCall.uri(1L));
    assertEquals("localhost", req.uri().getHost());
    assertEquals(this.baseUri.getPort(), req.uri().getPort());
  }

  @Test
  void toRequestAppendsQueryParameters() {
    PageSeederRequest req = client().toRequest(ResourceCall.uri(1L).query("edit", "true").query("format", "xml"));
    assertEquals("edit=true&format=xml", req.uri().getQuery());
  }

  @Test
  void toRequestIncludesCustomHeaders() {
    PageSeederRequest req = client().toRequest(ResourceCall.uri(1L).header("X-Custom", "hello"));
    assertEquals("hello", req.headers().get("X-Custom"));
  }

  @Test
  void toRequestMethodIsGet() {
    PageSeederRequest req = client().toRequest(ResourceCall.uri(1L));
    assertEquals("GET", req.method());
  }

  // --- fetch integration ---

  @Test
  void fetchSendsGetToWebsiteRoot() {
    AtomicReference<String> method = new AtomicReference<>();
    AtomicReference<String> path = new AtomicReference<>();
    this.server.createContext("/ps/docid/XYZ-1", exchange -> {
      method.set(exchange.getRequestMethod());
      path.set(exchange.getRequestURI().getPath());
      reply(exchange, 200, "text/html", "<html/>".getBytes(StandardCharsets.UTF_8));
    });

    PageSeederResponse response = client().fetch(ResourceCall.docId("XYZ-1"));
    assertEquals(200, response.statusCode());
    assertEquals("GET", method.get());
    assertEquals("/ps/docid/XYZ-1", path.get());
  }

  @Test
  void fetchSendsCredentials() {
    AtomicReference<String> authorization = new AtomicReference<>();
    this.server.createContext("/ps/uri/55", exchange -> {
      authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      reply(exchange, 200, "application/psml+xml", "<document/>".getBytes(StandardCharsets.UTF_8));
    });

    PageSeederClient.builder()
        .instance(PageSeederInstance.of(this.baseUri))
        .credentials(new BasicCredentials("user", "pass"))
        .build()
        .fetch(ResourceCall.uri(55L));

    assertTrue(authorization.get().startsWith("Basic "));
  }

  @Test
  void fetchAllowsCredentialOverride() {
    AtomicReference<String> authorization = new AtomicReference<>();
    this.server.createContext("/ps/uri/10", exchange -> {
      authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      reply(exchange, 200, "application/psml+xml", "<document/>".getBytes(StandardCharsets.UTF_8));
    });

    PageSeederClient.builder()
        .instance(PageSeederInstance.of(this.baseUri))
        .credentials(new BasicCredentials("default", "pass"))
        .build()
        .fetch(ResourceCall.uri(10L), new BasicCredentials("override", "pass"));

    String encoded = java.util.Base64.getEncoder().encodeToString("override:pass".getBytes(StandardCharsets.UTF_8));
    assertEquals("Basic " + encoded, authorization.get());
  }

  // --- helpers ---

  private PageSeederClient client() {
    return PageSeederClient.builder()
        .instance(PageSeederInstance.of(this.baseUri))
        .gzipEnabled(false)
        .build();
  }

  private static void reply(HttpExchange exchange, int status, String contentType, byte[] payload) throws IOException {
    exchange.getResponseHeaders().add("Content-Type", contentType);
    exchange.sendResponseHeaders(status, payload.length);
    exchange.getResponseBody().write(payload);
    exchange.close();
  }
}
