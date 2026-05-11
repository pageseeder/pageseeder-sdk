package org.pageseeder.sdk.client;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.service.PayloadFormat;

import java.net.URI;
import java.util.Map;

/**
 * Resolved SDK request.
 */
@SuppressWarnings("java:S6206") // Keep a class to preserve SDK API flexibility and avoid record identity over byte[].
public final class PageSeederRequest {

  private final String method;
  private final URI uri;
  private final Map<String, String> headers;
  private final byte @Nullable [] body;
  private final @Nullable String contentType;
  private final PayloadFormat format;

  /**
   * Creates a resolved SDK request.
   *
   * @param method      the HTTP method
   * @param uri         the resolved request URI
   * @param headers     the request headers
   * @param body        the request body, or {@code null} if there is no body
   * @param contentType the request content type, or {@code null} if unset
   * @param format      the expected response payload format
   */
  public PageSeederRequest(String method, URI uri, Map<String, String> headers,
                           byte @Nullable [] body,
                           @Nullable String contentType,
                           PayloadFormat format) {
    this.method = method;
    this.uri = uri;
    this.headers = Map.copyOf(headers);
    this.body = body;
    this.contentType = contentType;
    this.format = format;
  }

  /**
   * Returns the HTTP method.
   *
   * @return the HTTP method
   */
  public String method() {
    return this.method;
  }

  /**
   * Returns the resolved request URI.
   *
   * @return the request URI
   */
  public URI uri() {
    return this.uri;
  }

  /**
   * Returns the request headers.
   *
   * @return an immutable header map
   */
  public Map<String, String> headers() {
    return this.headers;
  }

  /**
   * Returns the request body.
   *
   * @return the request body, or {@code null} if there is none
   */
  public byte @Nullable [] body() {
    return this.body;
  }

  /**
   * Returns the request content type.
   *
   * @return the content type, or {@code null} if unset
   */
  public @Nullable String contentType() {
    return this.contentType;
  }

  /**
   * Returns the expected response payload format.
   *
   * @return the response payload format
   */
  public PayloadFormat format() {
    return this.format;
  }
}
