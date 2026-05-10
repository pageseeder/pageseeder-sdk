package org.pageseeder.sdk.auth;

import java.net.http.HttpRequest;
import java.util.Objects;

/**
 * Session cookie authentication.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SessionCookie implements Credentials {

  private final String name;
  private final String value;

  /**
   * Constructs a new SessionCookie instance using the default name "JSESSIONID".
   *
   * @param value The value to assign to the session cookie. Must not be null.
   */
  public SessionCookie(String value) {
    this("JSESSIONID", value);
  }

  /**
   * Constructs a new SessionCookie instance with the specified name and value.
   *
   * @param name  The name of the session cookie. Must not be null.
   * @param value The value of the session cookie. Must not be null.
   */
  public SessionCookie(String name, String value) {
    this.name = Objects.requireNonNull(name, "name");
    this.value = Objects.requireNonNull(value, "value");
  }

  /**
   * Applies the session cookie authentication to the specified HTTP request builder
   * by adding a "Cookie" header with the session cookie name and value.
   *
   * @param builder The HTTP request builder to which the session cookie should be applied. Must not be null.
   */
  @Override
  public void apply(HttpRequest.Builder builder) {
    builder.header("Cookie", this.name + "=" + this.value);
  }
}
