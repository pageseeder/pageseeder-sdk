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
@SuppressWarnings("java:S1192") // OAuth parameter names are clearer inline in this small request builder.
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

  /**
   * Creates a token request for the authorization-code grant.
   *
   * @param instance          the PageSeeder instance
   * @param code              the authorization code returned by the authorization endpoint
   * @param clientCredentials the OAuth client credentials
   * @return an authorization-code token request
   */
  public static TokenRequest authorizationCode(PageSeederInstance instance, String code,
                                               ClientCredentials clientCredentials) {
    Objects.requireNonNull(code, "code");
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("grant_type", GrantType.AUTHORIZATION_CODE.parameterValue());
    parameters.put("code", code);
    return new TokenRequest(tokenEndpointUri(instance), parameters, clientCredentials);
  }

  /**
   * Creates a token request for the password grant.
   *
   * @param instance          the PageSeeder instance
   * @param username          the resource owner username
   * @param password          the resource owner password
   * @param clientCredentials the OAuth client credentials
   * @return a password-grant token request
   */
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

  /**
   * Creates a token request for the client-credentials grant.
   *
   * @param instance          the PageSeeder instance
   * @param clientCredentials the OAuth client credentials
   * @return a client-credentials token request
   */
  public static TokenRequest clientCredentials(PageSeederInstance instance, ClientCredentials clientCredentials) {
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("grant_type", GrantType.CLIENT_CREDENTIALS.parameterValue());
    return new TokenRequest(tokenEndpointUri(instance), parameters, clientCredentials);
  }

  /**
   * Creates a token request for the refresh-token grant.
   *
   * @param instance          the PageSeeder instance
   * @param refreshToken      the refresh token
   * @param clientCredentials the OAuth client credentials
   * @return a refresh-token request
   */
  public static TokenRequest refreshToken(PageSeederInstance instance, String refreshToken,
                                          ClientCredentials clientCredentials) {
    Objects.requireNonNull(refreshToken, "refreshToken");
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("grant_type", GrantType.REFRESH_TOKEN.parameterValue());
    parameters.put("refresh_token", refreshToken);
    return new TokenRequest(tokenEndpointUri(instance), parameters, clientCredentials);
  }

  /**
   * Returns the OAuth token endpoint URI.
   *
   * @return the token endpoint URI
   */
  public URI endpointUri() {
    return this.endpointUri;
  }

  /**
   * Returns the token request parameters.
   *
   * @return an immutable parameter map
   */
  public Map<String, String> parameters() {
    return this.parameters;
  }

  /**
   * Returns the OAuth client credentials used by this request.
   *
   * @return the client credentials
   */
  public ClientCredentials clientCredentials() {
    return this.clientCredentials;
  }

  /**
   * Returns the grant type for this request.
   *
   * @return the grant type
   */
  public GrantType grantType() {
    return GrantType.fromParameterValue(this.parameters.get("grant_type"));
  }

  /**
   * Returns the requested scope.
   *
   * @return the scope parameter, or {@code null} if absent
   */
  public @Nullable String scope() {
    return this.parameters.get("scope");
  }

  /**
   * Returns the redirect URI associated with this token request.
   *
   * @return the redirect URI, or {@code null} if absent
   */
  public @Nullable URI redirectUri() {
    String value = this.parameters.get("redirect_uri");
    return value == null ? null : URI.create(value);
  }

  /**
   * Returns a request parameter by name.
   *
   * @param name the parameter name
   * @return the parameter value, or {@code null} if absent
   */
  public @Nullable String parameter(String name) {
    Objects.requireNonNull(name, "name");
    return this.parameters.get(name);
  }

  /**
   * Returns a copy of this request with the supplied redirect URI.
   *
   * @param redirectUri the redirect URI
   * @return the updated request
   */
  public TokenRequest withRedirectUri(URI redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri");
    return withParameter("redirect_uri", redirectUri.toString());
  }

  /**
   * Returns a copy of this request with the supplied scope.
   *
   * @param scope the requested scope
   * @return the updated request
   */
  public TokenRequest withScope(String scope) {
    return withParameter("scope", scope);
  }

  /**
   * Returns a copy of this request with the supplied parameter.
   *
   * @param name  the parameter name
   * @param value the parameter value
   * @return the updated request
   */
  public TokenRequest withParameter(String name, String value) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(value, "value");
    Map<String, String> updated = new LinkedHashMap<>(this.parameters);
    updated.put(name, value);
    return new TokenRequest(this.endpointUri, updated, this.clientCredentials);
  }

  /**
   * Executes this token request with a default HTTP client.
   *
   * @return the parsed token response
   */
  public TokenResponse execute() {
    return execute(HttpClient.newHttpClient());
  }

  /**
   * Executes this token request with the supplied HTTP client.
   *
   * @param httpClient the HTTP client to use
   * @return the parsed token response
   */
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

  /**
   * Resolves the OAuth token endpoint URI for a PageSeeder instance.
   *
   * @param instance the PageSeeder instance
   * @return the token endpoint URI
   */
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
