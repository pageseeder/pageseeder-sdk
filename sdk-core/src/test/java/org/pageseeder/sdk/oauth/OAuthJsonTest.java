package org.pageseeder.sdk.oauth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

final class OAuthJsonTest {

  @Test
  void parseFlatObjectKeepsOAuthScalarValues() {
    Map<String, String> values = OAuthJson.parseFlatObject("""
        {
          "access_token": "abc123",
          "token_type": "bearer",
          "expires_in": 3600,
          "active": true,
          "refresh_token": null
        }""");

    Assertions.assertEquals("abc123", values.get("access_token"));
    Assertions.assertEquals("bearer", values.get("token_type"));
    Assertions.assertEquals("3600", values.get("expires_in"));
    Assertions.assertEquals("true", values.get("active"));
    Assertions.assertFalse(values.containsKey("refresh_token"));
  }

  @Test
  void parseFlatObjectUnescapesStringValues() {
    Map<String, String> values = OAuthJson.parseFlatObject("""
        {"name":"Christophe\\nLauret","city":"\\u0053ydney","quote":"Say \\"hello\\""}""");

    Assertions.assertEquals("Christophe\nLauret", values.get("name"));
    Assertions.assertEquals("Sydney", values.get("city"));
    Assertions.assertEquals("Say \"hello\"", values.get("quote"));
  }

  @Test
  void parseFlatObjectKeepsNestedValuesAsRawJson() {
    Map<String, String> values = OAuthJson.parseFlatObject("""
        {"claims":{"roles":["admin", "author"]},"scope":"openid"}""");

    Assertions.assertEquals("{\"roles\":[\"admin\", \"author\"]}", values.get("claims"));
    Assertions.assertEquals("openid", values.get("scope"));
  }

  @Test
  void parseBase64UrlFlatObjectReturnsEmptyMapForInvalidSegment() {
    Assertions.assertTrue(OAuthJson.parseBase64UrlFlatObject("not valid").isEmpty());
  }

  @Test
  void parseBase64UrlFlatObjectDecodesJsonSegment() {
    String segment = Base64.getUrlEncoder().withoutPadding()
        .encodeToString("{\"typ\":\"JWT\",\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));

    Map<String, String> values = OAuthJson.parseBase64UrlFlatObject(segment);

    Assertions.assertEquals("JWT", values.get("typ"));
    Assertions.assertEquals("HS256", values.get("alg"));
  }
}
