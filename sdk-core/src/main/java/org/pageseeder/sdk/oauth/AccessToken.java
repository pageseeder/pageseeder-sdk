package org.pageseeder.sdk.oauth;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.auth.BearerToken;
import org.pageseeder.sdk.auth.Credentials;

import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * OAuth access token.
 */
public final class AccessToken implements Credentials {

  static final Pattern VALID_ACCESS_TOKEN = Pattern.compile("[a-zA-Z0-9=.+/_-]{16,1024}");

  private final String value;
  private final @Nullable Instant expiresAt;

  /**
   * Creates an access token without an expiry time.
   *
   * @param value the raw access token value
   */
  public AccessToken(String value) {
    this(value, null);
  }

  /**
   * Creates an access token with an optional expiry time.
   *
   * @param value     the raw access token value
   * @param expiresAt the instant when the token expires, or {@code null} if unknown
   */
  public AccessToken(String value, @Nullable Instant expiresAt) {
    this.value = requireValidValue(value);
    this.expiresAt = expiresAt;
  }

  /**
   * Returns the raw access token value.
   *
   * @return the raw access token value
   */
  public String value() {
    return this.value;
  }

  /**
   * Returns the expiry time for this token.
   *
   * @return the expiry time, or {@code null} if unknown
   */
  public @Nullable Instant expiresAt() {
    return this.expiresAt;
  }

  /**
   * Indicates whether this token has expired.
   *
   * @return {@code true} when an expiry time is known and is not in the future
   */
  public boolean isExpired() {
    return this.expiresAt != null && !Instant.now().isBefore(this.expiresAt);
  }

  /**
   * Converts this access token to bearer-token credentials.
   *
   * @return bearer-token credentials using this token value
   */
  public BearerToken toBearerToken() {
    return new BearerToken(this.value);
  }

  @Override
  public void apply(HttpRequest.Builder builder) {
    toBearerToken().apply(builder);
  }

  private static String requireValidValue(String value) {
    Objects.requireNonNull(value, "value");
    if (!VALID_ACCESS_TOKEN.matcher(value).matches()) {
      throw new IllegalArgumentException("value is invalid");
    }
    return value;
  }
}
