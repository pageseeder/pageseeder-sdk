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
public final class AuthorizationRequest {

  private final URI endpointUri;
  private final Map<String, String> parameters;

  private AuthorizationRequest(URI endpointUri, Map<String, String> parameters) {
    this.endpointUri = Objects.requireNonNull(endpointUri, "endpointUri");
    this.parameters = Map.copyOf(parameters);
  }

  public static AuthorizationRequest authorizationCode(PageSeederInstance instance, String clientId) {
    Objects.requireNonNull(instance, "instance");
    Objects.requireNonNull(clientId, "clientId");
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("response_type", "code");
    parameters.put("client_id", clientId);
    parameters.put("state", UUID.randomUUID().toString().substring(0, 12));
    return new AuthorizationRequest(authorizationEndpointUri(instance), parameters);
  }

  public URI endpointUri() {
    return this.endpointUri;
  }

  public URI authorizationUri() {
    String query = formEncode(this.parameters);
    return URI.create(this.endpointUri + "?" + query);
  }

  public Map<String, String> parameters() {
    return this.parameters;
  }

  public @Nullable String state() {
    return this.parameters.get("state");
  }

  public @Nullable String clientId() {
    return this.parameters.get("client_id");
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

  public AuthorizationRequest withState(String state) {
    return withParameter("state", state);
  }

  public AuthorizationRequest withRedirectUri(URI redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri");
    return withParameter("redirect_uri", redirectUri.toString());
  }

  public AuthorizationRequest withScope(String scope) {
    return withParameter("scope", scope);
  }

  public AuthorizationRequest withParameter(String name, String value) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(value, "value");
    Map<String, String> updated = new LinkedHashMap<>(this.parameters);
    updated.put(name, value);
    return new AuthorizationRequest(this.endpointUri, updated);
  }

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
