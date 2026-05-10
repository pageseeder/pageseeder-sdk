package org.pageseeder.sdk.client;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.service.PayloadFormat;

import java.net.URI;
import java.util.Map;

/**
 * Resolved SDK request.
 */
public final class PageSeederRequest {

  private final String method;
  private final URI uri;
  private final Map<String, String> headers;
  private final byte @Nullable [] body;
  private final @Nullable String contentType;
  private final PayloadFormat format;

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

  public String method() {
    return this.method;
  }

  public URI uri() {
    return this.uri;
  }

  public Map<String, String> headers() {
    return this.headers;
  }

  public byte @Nullable [] body() {
    return this.body;
  }

  public @Nullable String contentType() {
    return this.contentType;
  }

  public PayloadFormat format() {
    return this.format;
  }
}
