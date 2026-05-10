package org.pageseeder.sdk.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.PageSeederInstance;
import org.pageseeder.sdk.auth.BasicCredentials;
import org.pageseeder.sdk.auth.BearerToken;
import org.pageseeder.sdk.auth.SessionCookie;
import org.pageseeder.sdk.exception.ServiceErrorException;
import org.pageseeder.sdk.exception.TransportException;
import org.pageseeder.sdk.service.PayloadFormat;
import org.pageseeder.sdk.service.ServiceCall;
import org.pageseeder.sdk.service.ServiceCatalog;
import org.pageseeder.sdk.service.ServiceEndpoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

final class PageSeederClientTest {

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
  void shouldSendBasicAuthHeader() {
    AtomicReference<String> authorization = new AtomicReference<>();
    this.server.createContext("/ps/api/members/jdoe.xml", exchange -> {
      authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      reply(exchange, 200, "application/xml", read("fixtures/member.xml"));
    });

    PageSeederClient client = PageSeederClient.builder()
        .apiOrigin(this.baseUri)
        .credentials(new BasicCredentials("john", "secret"))
        .build();

    PageSeederResponse response = client.execute(
        ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "jdoe"));

    assertEquals(200, response.statusCode());
    assertTrue(authorization.get().startsWith("Basic "));
  }

  @Test
  void shouldOverrideDefaultCredentialsPerRequest() {
    AtomicReference<String> authorization = new AtomicReference<>();
    this.server.createContext("/ps/api/members/jdoe.xml", exchange -> {
      authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      reply(exchange, 200, "application/xml", read("fixtures/member.xml"));
    });

    PageSeederClient client = PageSeederClient.builder()
        .instance(PageSeederInstance.of(this.baseUri))
        .credentials(new BasicCredentials("john", "secret"))
        .build();

    PageSeederResponse response = client.execute(
        ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "jdoe"),
        new BearerToken("override-token"));

    assertEquals(200, response.statusCode());
    assertEquals("Bearer override-token", authorization.get());
  }

  @Test
  void shouldSendBearerAndCookieCredentials() {
    AtomicReference<String> bearer = new AtomicReference<>();
    AtomicReference<String> cookie = new AtomicReference<>();
    this.server.createContext("/ps/api/groups/docs.xml", exchange -> {
      bearer.set(exchange.getRequestHeaders().getFirst("Authorization"));
      reply(exchange, 200, "application/xml", "<group id=\"4\" name=\"docs\"/>".getBytes(StandardCharsets.UTF_8));
    });
    this.server.createContext("/ps/api/version.xml", exchange -> {
      cookie.set(exchange.getRequestHeaders().getFirst("Cookie"));
      reply(exchange, 200, "application/xml", read("fixtures/version.xml"));
    });

    PageSeederClient.builder().apiOrigin(this.baseUri).credentials(new BearerToken("abc123")).build()
        .execute(ServiceCall.of(ServiceCatalog.GROUP).pathVariable("group", "docs"));
    PageSeederClient.builder().apiOrigin(this.baseUri).credentials(new SessionCookie("session-1")).build()
        .execute(ServiceCall.of(ServiceCatalog.VERSION));

    assertEquals("Bearer abc123", bearer.get());
    assertEquals("JSESSIONID=session-1", cookie.get());
  }

  @Test
  void shouldConstructClientWithDefaultSettings() {
    this.server.createContext("/ps/api/version.xml",
        exchange -> reply(exchange, 200, "application/xml", read("fixtures/version.xml")));

    PageSeederClient client = new PageSeederClient(PageSeederInstance.of(this.baseUri));
    PageSeederResponse response = client.execute(ServiceCall.of(ServiceCatalog.VERSION));

    assertEquals(PageSeederInstance.of(this.baseUri).apiRoot(), client.apiRoot());
    assertEquals(200, response.statusCode());
  }

  @Test
  void shouldCreateDerivedClientsWithUpdatedAttributes() {
    AtomicReference<String> authorization = new AtomicReference<>();
    AtomicReference<String> acceptEncoding = new AtomicReference<>();
    this.server.createContext("/ps/api/version.json", exchange -> {
      authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      acceptEncoding.set(exchange.getRequestHeaders().getFirst("Accept-Encoding"));
      reply(exchange, 200, "application/json", read("fixtures/version.json"));
    });

    PageSeederClient baseClient = new PageSeederClient(PageSeederInstance.of(this.baseUri));
    PageSeederClient derivedClient = baseClient
        .withCredentials(new BearerToken("derived-token"))
        .withDefaultFormat(PayloadFormat.JSON)
        .withGzipEnabled(false);

    PageSeederResponse response = derivedClient.execute(ServiceCall.of(ServiceCatalog.VERSION));

    assertEquals(200, response.statusCode());
    assertEquals("Bearer derived-token", authorization.get());
    assertNull(acceptEncoding.get());
    assertEquals(URI.create(this.baseUri + "/ps/api/"), baseClient.apiRoot());
  }

  @Test
  void shouldEncodeQueryAndFormParameters() {
    AtomicReference<String> query = new AtomicReference<>();
    AtomicReference<String> body = new AtomicReference<>();
    this.server.createContext("/ps/api/members/jdoe.xml", exchange -> {
      query.set(exchange.getRequestURI().getQuery());
      body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      reply(exchange, 200, "application/xml", read("fixtures/member.xml"));
    });

    PageSeederClient.builder().apiOrigin(this.baseUri).build()
        .execute(ServiceCall.of(new ServiceEndpoint("POST", "/members/{member}"))
            .pathVariable("member", "jdoe")
            .query("xformat", "json")
            .query("q", "hello world")
            .form("member-username", "john doe")
            .form("firstname", "John"));

    assertEquals("xformat=json&q=hello+world", query.get());
    assertEquals("member-username=john+doe&firstname=John", body.get());
  }

  @Test
  void shouldDecodeGzipResponses() {
    this.server.createContext("/ps/api/members/jdoe.xml", exchange -> {
      byte[] payload = gzip(read("fixtures/member.xml"));
      exchange.getResponseHeaders().add("Content-Type", "application/xml");
      exchange.getResponseHeaders().add("Content-Encoding", "gzip");
      exchange.sendResponseHeaders(200, payload.length);
      exchange.getResponseBody().write(payload);
      exchange.close();
    });

    PageSeederClient client = PageSeederClient.builder().apiOrigin(this.baseUri).build();
    PageSeederResponse response = client.execute(
        ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "jdoe"));

    assertEquals(200, response.statusCode());
    assertTrue(response.body().length > 0);
  }

  @Test
  void shouldThrowServiceErrorForNonSuccessStatus() {
    this.server.createContext("/ps/api/members/missing.xml", exchange ->
        reply(exchange, 404, "application/xml", read("fixtures/service-error.xml")));

    PageSeederClient client = PageSeederClient.builder().apiOrigin(this.baseUri).build();
    ServiceCall call = ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "missing");

    ServiceErrorException ex = assertThrows(ServiceErrorException.class, () -> client.execute(call));

    assertEquals(404, ex.getStatusCode());
    assertEquals("deadbeef", ex.getError().getId());
  }

  @Test
  void shouldThrowServiceErrorForJsonNonSuccessStatus() {
    this.server.createContext("/ps/api/members/missing.xml", exchange ->
        reply(exchange, 404, "application/json", read("fixtures/service-error.json")));

    PageSeederClient client = PageSeederClient.builder().apiOrigin(this.baseUri).build();
    ServiceCall call = ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "missing");

    ServiceErrorException ex = assertThrows(ServiceErrorException.class, () -> client.execute(call));

    assertEquals(404, ex.getStatusCode());
    assertEquals("deadbeef", ex.getError().getId());
    assertEquals("Missing member", ex.getError().getMessage());
  }

  @Test
  void shouldTimeoutSlowRequests() {
    CountDownLatch releaseResponse = new CountDownLatch(1);
    this.server.createContext("/ps/api/version.xml", exchange -> {
      if (await(releaseResponse)) {
        reply(exchange, 200, "application/xml", read("fixtures/version.xml"));
      }
    });

    PageSeederClient client = PageSeederClient.builder()
        .apiOrigin(this.baseUri)
        .timeout(Duration.ofMillis(50))
        .build();
    ServiceCall call = ServiceCall.of(ServiceCatalog.VERSION);

    TransportException ex;
    try {
      ex = assertThrows(TransportException.class, () -> client.execute(call));
    } finally {
      releaseResponse.countDown();
    }

    assertNotNull(ex.getCause());
  }

  @Test
  void shouldCreateDerivedClientWithUpdatedTimeout()  {
    CountDownLatch releaseResponse = new CountDownLatch(1);
    this.server.createContext("/ps/api/version.xml", exchange -> {
      if (await(releaseResponse)) {
        reply(exchange, 200, "application/xml", read("fixtures/version.xml"));
      }
    });

    PageSeederClient client = new PageSeederClient(PageSeederInstance.of(this.baseUri))
        .withTimeout(Duration.ofMillis(50));
    ServiceCall call = ServiceCall.of(ServiceCatalog.VERSION);

    TransportException ex;
    try {
      ex = assertThrows(TransportException.class, () -> client.execute(call));
    } finally {
      releaseResponse.countDown();
    }

    assertNotNull(ex.getCause());
  }

  @Test
  void shouldDefaultInstanceUrisFromWebsite() {
    URI website = URI.create("https://example.com:8443");
    PageSeederInstance instance = PageSeederInstance.of(website);

    assertEquals(website, instance.websiteOrigin());
    assertEquals(website, instance.apiOrigin());
    assertEquals("/ps", instance.sitePrefix());
    assertEquals(URI.create("https://example.com:8443/ps/"), instance.websiteRoot());
    assertEquals(URI.create("https://example.com:8443/ps/api/"), instance.apiRoot());
    assertEquals(URI.create("https://example.com:8443/ps/oauth/"), instance.oauthRoot());
    assertEquals(URI.create("http://example.com:80"), instance.documentOrigin());
  }

  @Test
  void shouldCreateInstanceFromOriginStrings() {
    PageSeederInstance instance = PageSeederInstance.of("https://example.com:8443", "http://api.example.com:8080");

    assertEquals(URI.create("https://example.com:8443"), instance.websiteOrigin());
    assertEquals(URI.create("http://api.example.com:8080"), instance.apiOrigin());
    assertEquals(URI.create("http://api.example.com:8080/ps/api/"), instance.apiRoot());
    assertEquals(URI.create("http://example.com:80"), instance.documentOrigin());
  }

  @Test
  void shouldAllowCustomApiAndDocumentUrisOnInstance() {
    PageSeederInstance instance = PageSeederInstance.builder()
        .websiteOrigin(URI.create("https://public.example.com"))
        .apiOrigin(URI.create("http://internal.example.local:8282"))
        .documentOrigin(URI.create("http://legacy.example.local:8080"))
        .sitePrefix("/custom")
        .build();

    assertEquals(URI.create("https://public.example.com/custom/"), instance.websiteRoot());
    assertEquals(URI.create("http://internal.example.local:8282/custom/api/"), instance.apiRoot());
    assertEquals(URI.create("http://internal.example.local:8282/custom/oauth/"), instance.oauthRoot());
  }

  @Test
  void shouldAllowBuilderConfigurationUsingStrings() {
    PageSeederInstance instance = PageSeederInstance.builder()
        .websiteOrigin("https://public.example.com")
        .apiOrigin("http://internal.example.local:8282")
        .documentOrigin("http://legacy.example.local:8080")
        .sitePrefix("/custom")
        .build();

    assertEquals(URI.create("https://public.example.com"), instance.websiteOrigin());
    assertEquals(URI.create("http://internal.example.local:8282"), instance.apiOrigin());
    assertEquals(URI.create("http://legacy.example.local:8080"), instance.documentOrigin());
    assertEquals("/custom", instance.sitePrefix());
  }

  private static void reply(HttpExchange exchange, int status, String contentType, byte[] payload) throws IOException {
    exchange.getResponseHeaders().add("Content-Type", contentType);
    exchange.sendResponseHeaders(status, payload.length);
    exchange.getResponseBody().write(payload);
    exchange.close();
  }

  private static boolean await(CountDownLatch latch) {
    try {
      return latch.await(1, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private static byte[] gzip(byte[] bytes) throws IOException {
    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
    try (java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(buffer)) {
      gzip.write(bytes);
    }
    return buffer.toByteArray();
  }

  private static byte[] read(String path) throws IOException {
    try (InputStream in = PageSeederClientTest.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) throw new IOException("Missing fixture " + path);
      return in.readAllBytes();
    }
  }
}
