package org.pageseeder.sdk.oauth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.PageSeederInstance;

import java.net.URI;

final class TokenRequestTest {

  @Test
  void passwordRequestMasksPasswordInToString() {
    PageSeederInstance instance = PageSeederInstance.of(URI.create("https://example.com"));
    ClientCredentials clientCredentials = new ClientCredentials("1234567890123456", "secret-value");

    TokenRequest request = TokenRequest.password(instance, "alice", "open-sesame", clientCredentials);

    Assertions.assertFalse(request.toString().contains("open-sesame"));
    Assertions.assertTrue(request.toString().contains("password=******"));
  }

  @Test
  void clientCredentialsRequestUsesExpectedGrantType() {
    PageSeederInstance instance = PageSeederInstance.of(URI.create("https://example.com"));
    ClientCredentials clientCredentials = new ClientCredentials("1234567890123456", "secret-value");

    TokenRequest request = TokenRequest.clientCredentials(instance, clientCredentials);

    Assertions.assertEquals(GrantType.CLIENT_CREDENTIALS, request.grantType());
    Assertions.assertEquals("client_credentials", request.parameter("grant_type"));
    Assertions.assertEquals("https://example.com/ps/oauth/token", request.endpointUri().toString());
  }
}
