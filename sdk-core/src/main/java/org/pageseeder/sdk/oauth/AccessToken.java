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

  public AccessToken(String value) {
    this(value, null);
  }

  public AccessToken(String value, @Nullable Instant expiresAt) {
    this.value = requireValidValue(value);
    this.expiresAt = expiresAt;
  }

  public String value() {
    return this.value;
  }

  public @Nullable Instant expiresAt() {
    return this.expiresAt;
  }

  public boolean isExpired() {
    return this.expiresAt != null && !Instant.now().isBefore(this.expiresAt);
  }

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
