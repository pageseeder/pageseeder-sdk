package org.pageseeder.sdk.oauth;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.PageSeederInstance;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable request for the OAuth token endpoint.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TokenRequest {

  private static final String TOKEN_ENDPOINT = "token";

  private final URI endpointUri;
  private final Map<String, String> parameters;
  private final ClientCredentials clientCredentials;

  private TokenRequest(URI endpointUri, Map<String, String> parameters, ClientCredentials clientCredentials) {
    this.endpointUri = Objects.requireNonNull(endpointUri, "endpointUri");
    this.parameters = Map.copyOf(parameters);
    this.clientCredentials = Objects.requireNonNull(clientCredentials, "clientCredentials");
  }

  public static TokenRequest authorizationCode(PageSeederInstance instance, String code,
                                               ClientCredentials clientCredentials) {
    Objects.requireNonNull(code, "code");
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("grant_type", GrantType.AUTHORIZATION_CODE.parameterValue());
    parameters.put("code", code);
    return new TokenRequest(tokenEndpointUri(instance), parameters, clientCredentials);
  }

  public static TokenRequest password(PageSeederInstance instance, String username, String password,
                                      ClientCredentials clientCredentials) {
    Objects.requireNonNull(username, "username");
    Objects.requireNonNull(password, "password");
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("grant_type", GrantType.PASSWORD.parameterValue());
    parameters.put("username", username);
    parameters.put("password", password);
    return new TokenRequest(tokenEndpointUri(instance), parameters, clientCredentials);
  }

  public static TokenRequest clientCredentials(PageSeederInstance instance, ClientCredentials clientCredentials) {
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("grant_type", GrantType.CLIENT_CREDENTIALS.parameterValue());
    return new TokenRequest(tokenEndpointUri(instance), parameters, clientCredentials);
  }

  public static TokenRequest refreshToken(PageSeederInstance instance, String refreshToken,
                                          ClientCredentials clientCredentials) {
    Objects.requireNonNull(refreshToken, "refreshToken");
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("grant_type", GrantType.REFRESH_TOKEN.parameterValue());
    parameters.put("refresh_token", refreshToken);
    return new TokenRequest(tokenEndpointUri(instance), parameters, clientCredentials);
  }

  public URI endpointUri() {
    return this.endpointUri;
  }

  public Map<String, String> parameters() {
    return this.parameters;
  }

  public ClientCredentials clientCredentials() {
    return this.clientCredentials;
  }

  public GrantType grantType() {
    return GrantType.fromParameterValue(this.parameters.get("grant_type"));
  }

  public @Nullable String scope() {
    return this.parameters.get("scope");
  }

  public @Nullable URI redirectUri() {
    String value = this.parameters.get("redirect_uri");
    return value == null ? null : URI.create(value);
  }

  public @Nullable String parameter(String name) {
    Objects.requireNonNull(name, "name");
    return this.parameters.get(name);
  }

  public TokenRequest withRedirectUri(URI redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri");
    return withParameter("redirect_uri", redirectUri.toString());
  }

  public TokenRequest withScope(String scope) {
    return withParameter("scope", scope);
  }

  public TokenRequest withParameter(String name, String value) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(value, "value");
    Map<String, String> updated = new LinkedHashMap<>(this.parameters);
    updated.put(name, value);
    return new TokenRequest(this.endpointUri, updated, this.clientCredentials);
  }

  public TokenResponse execute() {
    return execute(HttpClient.newHttpClient());
  }

  public TokenResponse execute(HttpClient httpClient) {
    Objects.requireNonNull(httpClient, "httpClient");
    Instant requestedAt = Instant.now();
    HttpRequest request = HttpRequest.newBuilder(this.endpointUri)
        .header("Authorization", this.clientCredentials.toBasicAuthorization())
        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .POST(HttpRequest.BodyPublishers.ofString(AuthorizationRequest.formEncode(this.parameters)))
        .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return TokenResponse.parse(response.statusCode(), response.body(), requestedAt, this.clientCredentials);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return TokenResponse.error("interrupted", ex.getMessage());
    } catch (IOException ex) {
      return TokenResponse.error("io_error", ex.getMessage());
    }
  }

  public static URI tokenEndpointUri(PageSeederInstance instance) {
    Objects.requireNonNull(instance, "instance");
    return instance.oauthRoot().resolve(TOKEN_ENDPOINT);
  }

  @Override
  @SuppressWarnings("java:S2068")
  public String toString() {
    String query = AuthorizationRequest.formEncode(this.parameters);
    return "POST " + this.endpointUri + "?" + query.replaceAll("password=([^&]+)", "password=******");
  }
}
