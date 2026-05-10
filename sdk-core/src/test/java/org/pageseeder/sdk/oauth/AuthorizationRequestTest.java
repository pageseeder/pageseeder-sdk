package org.pageseeder.sdk.oauth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.PageSeederInstance;

import java.net.URI;

final class AuthorizationRequestTest {

  @Test
  void authorizationRequestBuildsExpectedUri() {
    PageSeederInstance instance = PageSeederInstance.of(URI.create("https://example.com"));

    AuthorizationRequest request = AuthorizationRequest.authorizationCode(instance, "1234567890123456")
        .withRedirectUri(URI.create("https://client.example.com/callback"))
        .withScope("openid profile");

    Assertions.assertEquals("https://example.com/ps/oauth/authorize", request.endpointUri().toString());
    Assertions.assertEquals("1234567890123456", request.clientId());
    Assertions.assertNotNull(request.state());
    Assertions.assertEquals("openid profile", request.scope());
    Assertions.assertEquals(URI.create("https://client.example.com/callback"), request.redirectUri());
    Assertions.assertTrue(request.authorizationUri().toString().contains("response_type=code"));
    Assertions.assertTrue(request.authorizationUri().toString().contains("client_id=1234567890123456"));
  }
}
