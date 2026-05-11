package org.pageseeder.sdk.oauth;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.PageSeederInstance;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable OAuth authorization request.
 */
@SuppressWarnings("java:S1192") // OAuth parameter names are clearer inline in this small request builder.
public final class AuthorizationRequest {

  private final URI endpointUri;
  private final Map<String, String> parameters;

  private AuthorizationRequest(URI endpointUri, Map<String, String> parameters) {
    this.endpointUri = Objects.requireNonNull(endpointUri, "endpointUri");
    this.parameters = Map.copyOf(parameters);
  }

  /**
   * Creates an OAuth authorization-code request for a PageSeeder instance.
   *
   * @param instance the PageSeeder instance
   * @param clientId the registered OAuth client identifier
   * @return an authorization-code request
   */
  public static AuthorizationRequest authorizationCode(PageSeederInstance instance, String clientId) {
    Objects.requireNonNull(instance, "instance");
    Objects.requireNonNull(clientId, "clientId");
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("response_type", "code");
    parameters.put("client_id", clientId);
    parameters.put("state", UUID.randomUUID().toString().substring(0, 12));
    return new AuthorizationRequest(authorizationEndpointUri(instance), parameters);
  }

  /**
   * Returns the OAuth authorization endpoint URI.
   *
   * @return the authorization endpoint URI
   */
  public URI endpointUri() {
    return this.endpointUri;
  }

  /**
   * Builds the browser-facing authorization URI with query parameters.
   *
   * @return the full authorization URI
   */
  public URI authorizationUri() {
    String query = formEncode(this.parameters);
    return URI.create(this.endpointUri + "?" + query);
  }

  /**
   * Returns the request parameters.
   *
   * @return an immutable parameter map
   */
  public Map<String, String> parameters() {
    return this.parameters;
  }

  /**
   * Returns the request state parameter.
   *
   * @return the state parameter, or {@code null} if absent
   */
  public @Nullable String state() {
    return this.parameters.get("state");
  }

  /**
   * Returns the OAuth client identifier.
   *
   * @return the client identifier, or {@code null} if absent
   */
  public @Nullable String clientId() {
    return this.parameters.get("client_id");
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
   * Returns the redirect URI.
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
   * Returns a copy of this request with the supplied state parameter.
   *
   * @param state the state value
   * @return the updated request
   */
  public AuthorizationRequest withState(String state) {
    return withParameter("state", state);
  }

  /**
   * Returns a copy of this request with the supplied redirect URI.
   *
   * @param redirectUri the redirect URI
   * @return the updated request
   */
  public AuthorizationRequest withRedirectUri(URI redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri");
    return withParameter("redirect_uri", redirectUri.toString());
  }

  /**
   * Returns a copy of this request with the supplied scope.
   *
   * @param scope the requested scope
   * @return the updated request
   */
  public AuthorizationRequest withScope(String scope) {
    return withParameter("scope", scope);
  }

  /**
   * Returns a copy of this request with the supplied parameter.
   *
   * @param name  the parameter name
   * @param value the parameter value
   * @return the updated request
   */
  public AuthorizationRequest withParameter(String name, String value) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(value, "value");
    Map<String, String> updated = new LinkedHashMap<>(this.parameters);
    updated.put(name, value);
    return new AuthorizationRequest(this.endpointUri, updated);
  }

  /**
   * Resolves the authorization endpoint URI for a PageSeeder instance.
   *
   * @param instance the PageSeeder instance
   * @return the authorization endpoint URI
   */
  public static URI authorizationEndpointUri(PageSeederInstance instance) {
    Objects.requireNonNull(instance, "instance");
    return instance.oauthRoot().resolve("authorize");
  }

  @Override
  public String toString() {
    return this.authorizationUri().toString();
  }

  static String formEncode(Map<String, String> parameters) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      if (!first) {
        builder.append('&');
      }
      builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
      builder.append('=');
      builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      first = false;
    }
    return builder.toString();
  }
}
