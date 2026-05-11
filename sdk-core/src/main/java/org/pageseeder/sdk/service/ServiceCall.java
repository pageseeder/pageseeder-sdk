package org.pageseeder.sdk.service;

import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A single executable call to a PageSeeder service.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ServiceCall {

  private final ServiceEndpoint endpoint;
  private final Map<String, Object> pathVariables = new LinkedHashMap<>();
  private final QueryParameters query = new QueryParameters();
  private final QueryParameters form = new QueryParameters();
  private final Map<String, String> headers = new LinkedHashMap<>();
  private @Nullable PayloadFormat format;
  private byte @Nullable[] rawBody;
  private @Nullable String contentType;

  /**
   * Creates a service call for the supplied endpoint.
   *
   * @param endpoint the service endpoint to call
   */
  public ServiceCall(ServiceEndpoint endpoint) {
    this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
  }

  /**
   * @param endpoint The service endpoint to call.
   * @return A new service call for the given endpoint.
   */
  public static ServiceCall of(ServiceEndpoint endpoint) {
    return new ServiceCall(endpoint);
  }

  /**
   * Sets a path variable to be substituted into the endpoint's path template.
   *
   * @param name  The variable name (must match a {@code {name}} placeholder in the path).
   * @param value The value; will be percent-encoded when the path is resolved.
   * @return {@code this} for chaining.
   */
  public ServiceCall pathVariable(String name, Object value) {
    this.pathVariables.put(name, value);
    return this;
  }

  /**
   * Appends a query parameter to this call.
   *
   * @param name  The parameter name.
   * @param value The parameter value.
   * @return {@code this} for chaining.
   */
  public ServiceCall query(String name, String value) {
    this.query.add(name, value);
    return this;
  }

  /**
   * Appends a form parameter to this call.
   *
   * <p>When any form parameter is set, the request body is encoded as
   * {@code application/x-www-form-urlencoded} and the method should be POST or PATCH.
   *
   * @param name  The parameter name.
   * @param value The parameter value.
   * @return {@code this} for chaining.
   */
  public ServiceCall form(String name, String value) {
    this.form.add(name, value);
    return this;
  }

  /**
   * Sets an additional request header.
   *
   * @param name  The header name.
   * @param value The header value.
   * @return {@code this} for chaining.
   */
  public ServiceCall header(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  /**
   * Overrides the response format for this call, taking precedence over the client's default.
   *
   * @param format The desired response payload format.
   * @return {@code this} for chaining.
   */
  public ServiceCall accept(PayloadFormat format) {
    this.format = Objects.requireNonNull(format, "format");
    return this;
  }

  /**
   * Sets a raw byte array as the request body with an explicit content type.
   *
   * @param body        The raw body bytes.
   * @param contentType The {@code Content-Type} header value.
   * @return {@code this} for chaining.
   */
  public ServiceCall rawBody(byte[] body, String contentType) {
    this.rawBody = body;
    this.contentType = contentType;
    return this;
  }

  /**
   * Sets a UTF-8 encoded string as the request body with an explicit content type.
   *
   * @param body        The request body text.
   * @param contentType The {@code Content-Type} header value.
   * @return {@code this} for chaining.
   */
  public ServiceCall rawBody(String body, String contentType) {
    return rawBody(body.getBytes(StandardCharsets.UTF_8), contentType);
  }

  /** @return The target service endpoint. */
  public ServiceEndpoint endpoint() {
    return this.endpoint;
  }

  /** @return An immutable snapshot of the current path variables. */
  public Map<String, Object> pathVariables() {
    return Map.copyOf(this.pathVariables);
  }

  /** @return The query parameters accumulated for this call. */
  public QueryParameters queryParameters() {
    return this.query;
  }

  /** @return The form parameters accumulated for this call. */
  public QueryParameters formParameters() {
    return this.form;
  }

  /** @return An immutable snapshot of the additional request headers. */
  public Map<String, String> headers() {
    return Map.copyOf(this.headers);
  }

  /** @return The raw request body, or {@code null} if none was set. */
  public byte @Nullable [] rawBody() {
    return this.rawBody;
  }

  /** @return The explicit {@code Content-Type} for the request body, or {@code null}. */
  public @Nullable String contentType() {
    return this.contentType;
  }

  /** @return The response format override, or {@code null} to use the client's default. */
  public @Nullable PayloadFormat format() {
    return this.format;
  }
}
