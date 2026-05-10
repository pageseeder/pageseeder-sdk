package org.pageseeder.sdk.auth;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * Basic authentication credentials.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BasicCredentials implements Credentials {

  private final String username;
  private final String password;

  /**
   * Constructs an instance of BasicCredentials with the specified username and password.
   *
   * @param username The username to use for basic authentication. Must not be null.
   * @param password The password to use for basic authentication. Must not be null.
   */
  public BasicCredentials(String username, String password) {
    this.username = Objects.requireNonNull(username, "username");
    this.password = Objects.requireNonNull(password, "password");
  }

  /**
   * Applies the basic authentication credentials to the specified HTTP request builder.
   *
   * @param builder The HTTP request builder to which the authentication details should be added. Must not be null.
   */
  @Override
  public void apply(HttpRequest.Builder builder) {
    String userInfo = this.username + ":" + this.password;
    String token = Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
    builder.header("Authorization", "Basic " + token);
  }
}
