package org.pageseeder.sdk.auth;

import java.net.http.HttpRequest;
import java.util.Objects;

/**
 * Bearer token authentication.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BearerToken implements Credentials {

  private final String token;

  /**
   * Creates a new Bearer token.
   *
   * @param token The token. Must not be null.
   */
  public BearerToken(String token) {
    this.token = Objects.requireNonNull(token, "token");
  }

  /**
   * Applies the Bearer token authentication to the specified HTTP request builder
   * by adding an "Authorization" header with the Bearer token.
   *
   * @param builder The HTTP request builder to which the Bearer token should be applied. Must not be null.
   */
  @Override
  public void apply(HttpRequest.Builder builder) {
    builder.header("Authorization", "Bearer " + this.token);
  }
}
