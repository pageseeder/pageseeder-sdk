package org.pageseeder.sdk.oauth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

final class ClientRegistrationTest {

  @Test
  void registrationSerializesExpectedFormParameters() {
    ClientRegistration registration = new ClientRegistration("My App")
        .withGrantType(GrantType.AUTHORIZATION_CODE)
        .withRedirectUri(URI.create("https://client.example.com/callback"))
        .withClientUri(URI.create("https://client.example.com"))
        .withApplicationName("Client Suite")
        .withScope("openid email profile")
        .withDescription("OAuth client")
        .withAccessTokenLifetime(Duration.ofHours(1))
        .withRefreshTokenLifetime(Duration.ofDays(7))
        .withWebhookSecret("webhook-secret");

    Map<String, String> parameters = registration.toFormParameters();

    Assertions.assertEquals("My App", parameters.get("name"));
    Assertions.assertEquals("authorization_code", parameters.get("grant-type"));
    Assertions.assertEquals("https://client.example.com/callback", parameters.get("redirect-uri"));
    Assertions.assertEquals("https://client.example.com", parameters.get("client-uri"));
    Assertions.assertEquals("Client Suite", parameters.get("app"));
    Assertions.assertEquals("3600", parameters.get("access-token-max-age"));
    Assertions.assertEquals("604800", parameters.get("refresh-token-max-age"));
    Assertions.assertEquals("webhook-secret", parameters.get("webhook-secret"));
  }

  @Test
  void registrationParsesClientCredentialsFromXml() {
    String xml = """
        <client-registration secret="secret-value">
          <client identifier="1234567890123456"/>
        </client-registration>""";

    ClientCredentials credentials = ClientRegistration.parseClientCredentials(xml.getBytes());

    Assertions.assertNotNull(credentials);
    Assertions.assertEquals("1234567890123456", credentials.clientId());
    Assertions.assertEquals("secret-value", credentials.clientSecret());
  }

  @Test
  void parseClientCredentialsReturnsNullForEmptyBody() {
    Assertions.assertNull(ClientRegistration.parseClientCredentials(new byte[0]));
  }

  @Test
  void parseClientCredentialsReturnsNullForMalformedXml() {
    Assertions.assertNull(ClientRegistration.parseClientCredentials("<not valid xml".getBytes()));
  }

  @Test
  void parseClientCredentialsReturnsNullWhenClientElementMissing() {
    String xml = "<client-registration secret=\"s\"/>";
    Assertions.assertNull(ClientRegistration.parseClientCredentials(xml.getBytes()));
  }

  @Test
  void parseClientCredentialsReturnsNullWhenSecretIsBlank() {
    String xml = """
        <client-registration secret="">
          <client identifier="1234567890123456"/>
        </client-registration>""";
    Assertions.assertNull(ClientRegistration.parseClientCredentials(xml.getBytes()));
  }

  @Test
  void registrationExcludesRefreshTokenLifetimeForClientCredentials() {
    ClientRegistration registration = new ClientRegistration("My App")
        .withGrantType(GrantType.CLIENT_CREDENTIALS)
        .withRefreshTokenLifetime(Duration.ofDays(7));

    Map<String, String> parameters = registration.toFormParameters();

    Assertions.assertNull(parameters.get("refresh-token-max-age"));
  }

  @Test
  void registrationExcludesRedirectUriForClientCredentials() {
    ClientRegistration registration = new ClientRegistration("My App")
        .withGrantType(GrantType.CLIENT_CREDENTIALS)
        .withRedirectUri(URI.create("https://client.example.com/callback"));

    Map<String, String> parameters = registration.toFormParameters();

    Assertions.assertNull(parameters.get("redirect-uri"));
  }
}
