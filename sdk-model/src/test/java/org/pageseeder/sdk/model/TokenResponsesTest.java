package org.pageseeder.sdk.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.oauth.ClientCredentials;
import org.pageseeder.sdk.oauth.TokenResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

final class TokenResponsesTest {

  @Test
  void toMemberExtractsMemberFromJwtClaims() throws Exception {
    ClientCredentials credentials = new ClientCredentials("1234567890123456", "super-secret");
    String idToken = idToken(credentials.clientSecret());
    String rawResponse = """
        {
          "access_token": "abcdefghijklmnopqrstuvwxyz012345",
          "token_type": "bearer",
          "expires_in": 3600,
          "id_token": "%s"
        }""".formatted(idToken);

    TokenResponse response = TokenResponse.parse(200, rawResponse, Instant.parse("2026-04-12T00:00:00Z"), credentials);
    Member member = TokenResponses.toMember(response);

    Assertions.assertNotNull(member);
    Assertions.assertEquals(42L, member.id());
    Assertions.assertEquals("clauret", member.username());
    Assertions.assertEquals("clauret@example.com", member.email());
    Assertions.assertEquals("Christophe", member.firstname());
    Assertions.assertEquals("Lauret", member.surname());
  }

  @Test
  void toMemberReturnsNullWhenNoIdToken() {
    TokenResponse response = TokenResponse.parse(
        200,
        "{\"access_token\":\"abcdefghijklmnopqrstuvwxyz012345\",\"expires_in\":3600}",
        Instant.now(),
        new ClientCredentials("1234567890123456", "super-secret")
    );

    Assertions.assertNull(TokenResponses.toMember(response));
  }

  @Test
  void toMemberReturnsNullForErrorResponse() {
    TokenResponse response = TokenResponse.parse(
        400,
        "{\"error\":\"invalid_grant\"}",
        Instant.now(),
        new ClientCredentials("1234567890123456", "super-secret")
    );

    Assertions.assertNull(TokenResponses.toMember(response));
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
