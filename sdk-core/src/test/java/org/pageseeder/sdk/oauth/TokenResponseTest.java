package org.pageseeder.sdk.oauth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

final class TokenResponseTest {

  @Test
  void parseSuccessfulResponseExtractsAccessTokenAndJwtClaims() throws Exception {
    ClientCredentials credentials = new ClientCredentials("1234567890123456", "super-secret");
    Instant requestedAt = Instant.parse("2026-04-12T00:00:00Z");
    String idToken = idToken(credentials.clientSecret());
    String rawResponse = """
        {
          "access_token": "abcdefghijklmnopqrstuvwxyz012345",
          "token_type": "bearer",
          "expires_in": 3600,
          "refresh_token": "refresh-token-value",
          "scope": "openid profile",
          "id_token": "%s"
        }""".formatted(idToken);

    TokenResponse response = TokenResponse.parse(200, rawResponse, requestedAt, credentials);

    Assertions.assertTrue(response.isSuccessful());
    Assertions.assertNotNull(response.accessToken());
    Assertions.assertEquals("abcdefghijklmnopqrstuvwxyz012345", response.accessToken().value());
    Assertions.assertEquals(Instant.parse("2026-04-12T01:00:00Z"), response.accessToken().expiresAt());
    Assertions.assertEquals("refresh-token-value", response.refreshToken());

    Assertions.assertEquals("42", response.jwtSubject());
    Assertions.assertEquals("clauret", response.jwtPreferredUsername());
    Assertions.assertEquals("Christophe", response.jwtGivenName());
    Assertions.assertEquals("Lauret", response.jwtFamilyName());
    Assertions.assertEquals("clauret@example.com", response.jwtEmail());
  }

  @Test
  void parseErrorResponseKeepsErrorFields() {
    TokenResponse response = TokenResponse.parse(
        400,
        "{\"error\":\"invalid_grant\",\"error_description\":\"bad code\"}",
        Instant.parse("2026-04-12T00:00:00Z"),
        new ClientCredentials("1234567890123456", "super-secret")
    );

    Assertions.assertFalse(response.isSuccessful());
    Assertions.assertEquals("invalid_grant", response.error());
    Assertions.assertEquals("bad code", response.errorDescription());
    Assertions.assertNull(response.accessToken());
    Assertions.assertNull(response.jwtSubject());
  }

  private static String idToken(String secret) throws Exception {
    String header = base64Url("{\"typ\":\"JWT\",\"alg\":\"HS256\"}");
    String payload = base64Url("""
        {
          "sub": "42",
          "preferred_username": "clauret",
          "given_name": "Christophe",
          "family_name": "Lauret",
          "email": "clauret@example.com"
        }""");
    String content = header + "." + payload;
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(
        mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    return content + "." + signature;
  }

  private static String base64Url(String value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}
