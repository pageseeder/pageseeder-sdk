package org.pageseeder.sdk.oauth;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.PageSeederInstance;
import org.pageseeder.sdk.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an OAuth client registration request for PageSeeder.
 *
 * <p>Use the {@code with*} methods to configure the registration, then call
 * {@link #register(PageSeederInstance, Credentials, String)} to register the client
 * with the PageSeeder server and obtain {@link ClientCredentials}.
 *
 * <p>The minimum required field is the client name. The scope defaults to
 * {@value #DEFAULT_OPENID_SCOPE}.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ClientRegistration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegistration.class);

  /**
   * Default OpenID Connect scope requested when no explicit scope is configured.
   */
  public static final String DEFAULT_OPENID_SCOPE = "openid email profile";

  private final String clientName;
  private final @Nullable GrantType grantType;
  private final @Nullable URI redirectUri;
  private final @Nullable URI clientUri;
  private final @Nullable String applicationName;
  private final @Nullable String webhookSecret;
  private final @Nullable Duration accessTokenLifetime;
  private final @Nullable Duration refreshTokenLifetime;
  private final String scope;
  private final @Nullable String description;

  /**
   * Creates a client registration with the default OpenID Connect scope.
   *
   * @param clientName the client display name
   */
  public ClientRegistration(String clientName) {
    this(clientName, null, null, null, null, null, null, null, DEFAULT_OPENID_SCOPE, null);
  }

  private ClientRegistration(String clientName, @Nullable GrantType grantType, @Nullable URI redirectUri,
                             @Nullable URI clientUri, @Nullable String applicationName,
                             @Nullable String webhookSecret, @Nullable Duration accessTokenLifetime,
                             @Nullable Duration refreshTokenLifetime, String scope, @Nullable String description) {
    this.clientName = requireNonBlank(clientName, "clientName");
    this.grantType = grantType;
    this.redirectUri = redirectUri;
    this.clientUri = clientUri;
    this.applicationName = applicationName;
    this.webhookSecret = webhookSecret;
    this.accessTokenLifetime = requireNonNegative(accessTokenLifetime, "accessTokenLifetime");
    this.refreshTokenLifetime = requireNonNegative(refreshTokenLifetime, "refreshTokenLifetime");
    this.scope = requireNonBlank(scope, "scope");
    this.description = description;
  }

  /**
   * Returns the client display name.
   *
   * @return the client display name
   */
  public String clientName() {
    return this.clientName;
  }

  /**
   * Returns the requested grant type.
   *
   * @return the grant type, or {@code null} if omitted
   */
  public @Nullable GrantType grantType() {
    return this.grantType;
  }

  /**
   * Returns the redirect URI.
   *
   * @return the redirect URI, or {@code null} if omitted
   */
  public @Nullable URI redirectUri() {
    return this.redirectUri;
  }

  /**
   * Returns the client application URI.
   *
   * @return the client URI, or {@code null} if omitted
   */
  public @Nullable URI clientUri() {
    return this.clientUri;
  }

  /**
   * Returns the PageSeeder application name.
   *
   * @return the application name, or {@code null} if omitted
   */
  public @Nullable String applicationName() {
    return this.applicationName;
  }

  /**
   * Returns the webhook secret.
   *
   * @return the webhook secret, or {@code null} if omitted
   */
  public @Nullable String webhookSecret() {
    return this.webhookSecret;
  }

  /**
   * Returns the requested access-token lifetime.
   *
   * @return the access-token lifetime, or {@code null} if omitted
   */
  public @Nullable Duration accessTokenLifetime() {
    return this.accessTokenLifetime;
  }

  /**
   * Returns the requested refresh-token lifetime.
   *
   * @return the refresh-token lifetime, or {@code null} if omitted
   */
  public @Nullable Duration refreshTokenLifetime() {
    return this.refreshTokenLifetime;
  }

  /**
   * Returns the requested OAuth scope.
   *
   * @return the scope string
   */
  public String scope() {
    return this.scope;
  }

  /**
   * Returns the client description.
   *
   * @return the description, or {@code null} if omitted
   */
  public @Nullable String description() {
    return this.description;
  }

  /**
   * Returns a copy of this registration with the supplied grant type.
   *
   * @param grantType the grant type
   * @return the updated registration
   */
  public ClientRegistration withGrantType(GrantType grantType) {
    Objects.requireNonNull(grantType, "grantType");
    if (grantType == GrantType.REFRESH_TOKEN) {
      throw new IllegalArgumentException("grantType REFRESH_TOKEN is not valid for registration");
    }
    return new ClientRegistration(this.clientName, grantType, this.redirectUri, this.clientUri, this.applicationName,
        this.webhookSecret, this.accessTokenLifetime, this.refreshTokenLifetime, this.scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied redirect URI.
   *
   * @param redirectUri the redirect URI
   * @return the updated registration
   */
  public ClientRegistration withRedirectUri(URI redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri");
    return new ClientRegistration(this.clientName, this.grantType, redirectUri, this.clientUri, this.applicationName,
        this.webhookSecret, this.accessTokenLifetime, this.refreshTokenLifetime, this.scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied client URI.
   *
   * @param clientUri the client application URI
   * @return the updated registration
   */
  public ClientRegistration withClientUri(URI clientUri) {
    Objects.requireNonNull(clientUri, "clientUri");
    return new ClientRegistration(this.clientName, this.grantType, this.redirectUri, clientUri, this.applicationName,
        this.webhookSecret, this.accessTokenLifetime, this.refreshTokenLifetime, this.scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied PageSeeder application name.
   *
   * @param applicationName the PageSeeder application name
   * @return the updated registration
   */
  public ClientRegistration withApplicationName(String applicationName) {
    Objects.requireNonNull(applicationName, "applicationName");
    return new ClientRegistration(this.clientName, this.grantType, this.redirectUri, this.clientUri, applicationName,
        this.webhookSecret, this.accessTokenLifetime, this.refreshTokenLifetime, this.scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied webhook secret.
   *
   * @param webhookSecret the webhook secret
   * @return the updated registration
   */
  public ClientRegistration withWebhookSecret(String webhookSecret) {
    Objects.requireNonNull(webhookSecret, "webhookSecret");
    return new ClientRegistration(this.clientName, this.grantType, this.redirectUri, this.clientUri, this.applicationName,
        webhookSecret, this.accessTokenLifetime, this.refreshTokenLifetime, this.scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied access-token lifetime.
   *
   * @param accessTokenLifetime the access-token lifetime
   * @return the updated registration
   */
  public ClientRegistration withAccessTokenLifetime(Duration accessTokenLifetime) {
    Objects.requireNonNull(accessTokenLifetime, "accessTokenLifetime");
    return new ClientRegistration(this.clientName, this.grantType, this.redirectUri, this.clientUri, this.applicationName,
        this.webhookSecret, accessTokenLifetime, this.refreshTokenLifetime, this.scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied refresh-token lifetime.
   *
   * @param refreshTokenLifetime the refresh-token lifetime
   * @return the updated registration
   */
  public ClientRegistration withRefreshTokenLifetime(Duration refreshTokenLifetime) {
    Objects.requireNonNull(refreshTokenLifetime, "refreshTokenLifetime");
    return new ClientRegistration(this.clientName, this.grantType, this.redirectUri, this.clientUri, this.applicationName,
        this.webhookSecret, this.accessTokenLifetime, refreshTokenLifetime, this.scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied OAuth scope.
   *
   * @param scope the OAuth scope
   * @return the updated registration
   */
  public ClientRegistration withScope(String scope) {
    return new ClientRegistration(this.clientName, this.grantType, this.redirectUri, this.clientUri, this.applicationName,
        this.webhookSecret, this.accessTokenLifetime, this.refreshTokenLifetime, scope, this.description);
  }

  /**
   * Returns a copy of this registration with the supplied description.
   *
   * @param description the client description
   * @return the updated registration
   */
  public ClientRegistration withDescription(String description) {
    Objects.requireNonNull(description, "description");
    return new ClientRegistration(this.clientName, this.grantType, this.redirectUri, this.clientUri, this.applicationName,
        this.webhookSecret, this.accessTokenLifetime, this.refreshTokenLifetime, this.scope, description);
  }

  /**
   * Registers this OAuth client using a default HTTP client.
   *
   * @param instance       the PageSeeder instance
   * @param credentials    credentials authorized to register clients
   * @param memberUsername the PageSeeder member username that owns the client
   * @return the created client credentials, or {@code null} if registration failed
   */
  public @Nullable ClientCredentials register(PageSeederInstance instance, Credentials credentials, String memberUsername) {
    return register(HttpClient.newHttpClient(), instance, credentials, memberUsername);
  }

  /**
   * Registers this OAuth client using the supplied HTTP client.
   *
   * @param httpClient     the HTTP client to use
   * @param instance       the PageSeeder instance
   * @param credentials    credentials authorized to register clients
   * @param memberUsername the PageSeeder member username that owns the client
   * @return the created client credentials, or {@code null} if registration failed
   */
  public @Nullable ClientCredentials register(HttpClient httpClient, PageSeederInstance instance, Credentials credentials,
                                              String memberUsername) {
    Objects.requireNonNull(httpClient, "httpClient");
    Objects.requireNonNull(instance, "instance");
    Objects.requireNonNull(credentials, "credentials");
    String encodedMember = encodePathSegment(requireNonBlank(memberUsername, "memberUsername"));
    URI endpoint = instance.oauthRoot().resolve("members/" + encodedMember + "/clients");
    HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint)
        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .POST(HttpRequest.BodyPublishers.ofString(AuthorizationRequest.formEncode(toFormParameters())));
    credentials.apply(builder);
    try {
      HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        return null;
      }
      return parseClientCredentials(response.body());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return null;
    } catch (IOException ex) {
      LOGGER.warn("Failed to register OAuth client '{}' with PageSeeder", this.clientName, ex);
      return null;
    }
  }

  Map<String, String> toFormParameters() {
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("name", this.clientName);
    if (this.description != null) {
      parameters.put("description", this.description);
    }
    if (this.grantType != null) {
      parameters.put("grant-type", this.grantType.parameterValue());
    }
    boolean requiresRedirectUri = this.grantType == GrantType.AUTHORIZATION_CODE || this.grantType == GrantType.IMPLICIT;
    if (requiresRedirectUri && this.redirectUri != null) {
      parameters.put("redirect-uri", this.redirectUri.toString());
    }
    if (this.clientUri != null) {
      parameters.put("client-uri", this.clientUri.toString());
    }
    if (this.accessTokenLifetime != null) {
      parameters.put("access-token-max-age", Long.toString(this.accessTokenLifetime.getSeconds()));
    }
    if (this.refreshTokenLifetime != null && this.grantType != GrantType.CLIENT_CREDENTIALS) {
      parameters.put("refresh-token-max-age", Long.toString(this.refreshTokenLifetime.getSeconds()));
    }
    if (this.applicationName != null) {
      parameters.put("app", this.applicationName);
    }
    parameters.put("scope", this.scope);
    if (this.webhookSecret != null) {
      parameters.put("webhook-secret", this.webhookSecret);
    }
    return parameters;
  }

  static @Nullable ClientCredentials parseClientCredentials(byte[] body) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      var builder = factory.newDocumentBuilder();
      var document = builder.parse(new ByteArrayInputStream(body));
      var registration = document.getDocumentElement();
      if (registration == null) {
        return null;
      }
      String secret = registration.getAttribute("secret");
      var clientNodes = registration.getElementsByTagName("client");
      if (clientNodes.getLength() == 0) {
        return null;
      }
      var client = clientNodes.item(0);
      var identifier = client.getAttributes() == null ? null : client.getAttributes().getNamedItem("identifier");
      if (identifier == null || secret.isBlank()) {
        return null;
      }
      return new ClientCredentials(identifier.getNodeValue(), secret);
    } catch (SAXException | ParserConfigurationException | IOException ex) {
      LOGGER.debug("Unable to parse client credentials XML from PageSeeder", ex);
      return null;
    }
  }

  private static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  private static @Nullable Duration requireNonNegative(@Nullable Duration duration, String name) {
    if (duration != null && duration.isNegative()) {
      throw new IllegalArgumentException(name + " must not be negative");
    }
    return duration;
  }

  private static String encodePathSegment(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }
}
