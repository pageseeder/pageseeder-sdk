package org.pageseeder.sdk.model;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.PageSeederInstance;
import org.pageseeder.sdk.client.PageSeederClient;
import org.pageseeder.sdk.auth.BasicCredentials;
import org.pageseeder.sdk.auth.BearerToken;
import org.pageseeder.sdk.model.codec.Decoders;
import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.Membership;
import org.pageseeder.sdk.model.ResourceUri;
import org.pageseeder.sdk.model.ResultPage;
import org.pageseeder.sdk.model.Version;
import org.pageseeder.sdk.service.PayloadFormat;
import org.pageseeder.sdk.service.ServiceCall;
import org.pageseeder.sdk.service.ServiceCatalog;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class PageSeederClientMappingTest {

  private HttpServer server;
  private URI baseUri;

  @BeforeEach
  public void setUp() throws Exception {
    this.server = HttpServer.create(new InetSocketAddress(0), 0);
    this.server.start();
    this.baseUri = URI.create("http://localhost:" + this.server.getAddress().getPort());
  }

  @AfterEach
  public void tearDown() {
    this.server.stop(0);
  }

  @Test
  public void shouldDecodeMemberWithBasicCredentials() throws Exception {
    this.server.createContext("/ps/api/members/jdoe.xml",
        exchange -> reply(exchange, 200, "application/xml", read("fixtures/member.xml")));

    PageSeederClient client = PageSeederClient.builder()
        .apiOrigin(this.baseUri)
        .credentials(new BasicCredentials("john", "secret"))
        .build();

    Member member = client.execute(
        ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "jdoe"),
        Decoders.object(Member.class));

    assertEquals("jdoe", member.getUsername());
  }

  @Test
  public void shouldDecodeWithOverriddenCredentials() throws Exception {
    this.server.createContext("/ps/api/members/jdoe.xml",
        exchange -> reply(exchange, 200, "application/xml", read("fixtures/member.xml")));

    PageSeederClient client = PageSeederClient.builder()
        .instance(PageSeederInstance.of(this.baseUri))
        .credentials(new BasicCredentials("john", "secret"))
        .build();

    Member member = client.execute(
        ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "jdoe"),
        new BearerToken("override-token"),
        Decoders.object(Member.class));

    assertEquals("jdoe", member.getUsername());
  }

  @Test
  public void shouldDecodeVersionXmlAndJson() throws Exception {
    this.server.createContext("/ps/api/version.xml",
        exchange -> reply(exchange, 200, "application/xml", read("fixtures/version.xml")));
    this.server.createContext("/ps/api/version.json",
        exchange -> reply(exchange, 200, "application/json", read("fixtures/version.json")));

    PageSeederClient client = PageSeederClient.builder().apiOrigin(this.baseUri).build();

    Version xmlVersion = client.execute(ServiceCall.of(ServiceCatalog.VERSION), Decoders.object(Version.class));
    Version jsonVersion = client.execute(
        ServiceCall.of(ServiceCatalog.VERSION).accept(PayloadFormat.JSON), Decoders.object(Version.class));

    assertEquals("5.9804", xmlVersion.getString());
    assertEquals(9804, jsonVersion.getBuild());
  }

  @Test
  public void shouldDecodeMemberMembershipsAndUri() throws Exception {
    this.server.createContext("/ps/api/members/jdoe.xml",
        exchange -> reply(exchange, 200, "application/xml", read("fixtures/member.xml")));
    this.server.createContext("/ps/api/members/jdoe/memberships.xml", exchange ->
        reply(exchange, 200, "application/xml",
            read("api/get_list-member-memberships/200_memberships-formember.xml")));
    this.server.createContext("/ps/api/uri/88.json",
        exchange -> reply(exchange, 200, "application/json", read("fixtures/resource-uri.json")));

    PageSeederClient client = PageSeederClient.builder()
        .apiOrigin(this.baseUri).defaultFormat(PayloadFormat.XML).build();

    Member member = client.execute(
        ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "jdoe"),
        Decoders.object(Member.class));
    ResultPage<Membership> memberships = client.execute(
        ServiceCall.of(ServiceCatalog.MEMBER_MEMBERSHIPS).pathVariable("member", "jdoe"),
        Decoders.page(Membership.class));
    ResourceUri uri = client.execute(
        ServiceCall.of(ServiceCatalog.RESOURCE_URI).pathVariable("uri", "88").accept(PayloadFormat.JSON),
        Decoders.object(ResourceUri.class));

    assertEquals("jdoe", member.getUsername());
    assertEquals(3, memberships.getItems().size());
    assertNull(memberships.getItems().get(0).getMember());
    assertEquals("acme-info", memberships.getItems().get(1).getGroup().getName());
    assertEquals("Guide", uri.getTitle());
  }

  private static void reply(HttpExchange exchange, int status, String contentType, byte[] payload) throws IOException {
    exchange.getResponseHeaders().add("Content-Type", contentType);
    exchange.sendResponseHeaders(status, payload.length);
    exchange.getResponseBody().write(payload);
    exchange.close();
  }

  private static byte[] read(String path) throws IOException {
    try (InputStream in = PageSeederClientMappingTest.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) throw new IOException("Missing fixture " + path);
      return in.readAllBytes();
    }
  }
}
