package org.pageseeder.sdk.oauth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * OAuth client credentials.
 */
public final class ClientCredentials {

  static final Pattern VALID_CLIENT_ID = Pattern.compile("[a-zA-Z0-9=.+/_-]{16,512}");

  private final String clientId;
  private final String clientSecret;
  private final String basicAuthorization;

  public ClientCredentials(String clientId, String clientSecret) {
    this.clientId = requireValidClientId(clientId);
    this.clientSecret = requireValidSecret(clientSecret);
    String userInfo = this.clientId + ":" + this.clientSecret;
    this.basicAuthorization = "Basic " + Base64.getEncoder()
        .encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
  }

  public String clientId() {
    return this.clientId;
  }

  public String clientSecret() {
    return this.clientSecret;
  }

  public String toBasicAuthorization() {
    return this.basicAuthorization;
  }

  private static String requireValidClientId(String clientId) {
    Objects.requireNonNull(clientId, "clientId");
    if (!VALID_CLIENT_ID.matcher(clientId).matches()) {
      throw new IllegalArgumentException("clientId is invalid");
    }
    return clientId;
  }

  private static String requireValidSecret(String clientSecret) {
    Objects.requireNonNull(clientSecret, "clientSecret");
    if (clientSecret.isEmpty()) {
      throw new IllegalArgumentException("clientSecret must not be empty");
    }
    return clientSecret;
  }
}
