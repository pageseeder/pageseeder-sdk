package org.pageseeder.sdk.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.pageseeder.sdk.client.PageSeederResponse;
import org.pageseeder.sdk.model.codec.JsonResponseBody;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PageSeederResponseMappingTest {

  @Test
  void shouldDecodeJsonWithCustomTreeDecoder() throws IOException {
    JsonResponseBody json = jsonResponse("fixtures/member.json");

    String summary = json.map(root -> {
      JsonNode member = root.path("member");
      return member.path("username").asText() + "|" + member.path("email").asText("");
    });

    assertEquals("jdoe|jdoe@example.com", summary);
  }

  @Test
  void shouldDecodeJsonNodeWithFunction() throws IOException {
    JsonResponseBody json = jsonResponse("fixtures/member.json");

    String summary = json.at("/member",
        node -> node.path("username").asText() + "|" + node.path("email").asText(""));

    assertEquals("jdoe|jdoe@example.com", summary);
  }

  @Test
  void shouldDecodeJsonNodesWithFunction() throws IOException {
    JsonResponseBody json = jsonResponse("fixtures/memberships.json");

    List<String> memberships = json.listAt("/result/membership",
        node -> node.path("id").asText() + ":" + node.path("role").asText());

    assertEquals(List.of("10:manager", "11:writer"), memberships);
  }

  @Test
  void shouldExposeJsonTree() throws IOException {
    PageSeederResponse response = new PageSeederResponse(
        200,
        Map.of("Content-Type", List.of("application/json; charset=UTF-8")),
        read("fixtures/version.json"),
        "application/json; charset=UTF-8");

    JsonNode root = new JsonResponseBody(response.body()).tree();

    assertNotNull(root);
    assertTrue(root.has("string"));
    assertEquals("5.9804", root.path("string").asText());
  }

  private static JsonResponseBody jsonResponse(String path) throws IOException {
    return new JsonResponseBody(read(path));
  }

  private static byte[] read(String path) throws IOException {
    try (InputStream in = PageSeederResponseMappingTest.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) throw new IOException("Missing fixture " + path);
      return in.readAllBytes();
    }
  }
}
