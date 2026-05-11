package org.pageseeder.sdk.oauth;

import org.jspecify.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parsed response from the OAuth token endpoint.
 *
 * <p>If the response includes an OpenID Connect {@code id_token}, the raw JWT claims are
 * accessible via {@link #jwtSubject()}, {@link #jwtPreferredUsername()}, etc. To convert
 * these claims into a {@code Member}, use {@code TokenResponses.toMember(this)} from
 * {@code sdk-mapping}.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TokenResponse {

  private final int statusCode;
  private final @Nullable String rawResponse;
  private final Map<String, String> parameters;
  private final @Nullable AccessToken accessToken;
  private final @Nullable Map<String, String> jwtClaims;

  private TokenResponse(int statusCode, @Nullable String rawResponse, Map<String, String> parameters,
                        @Nullable AccessToken accessToken, @Nullable Map<String, String> jwtClaims) {
    this.statusCode = statusCode;
    this.rawResponse = rawResponse;
    this.parameters = Map.copyOf(parameters);
    this.accessToken = accessToken;
    this.jwtClaims = jwtClaims != null ? Map.copyOf(jwtClaims) : null;
  }

  /**
   * @return {@code true} if the token endpoint returned an HTTP response (status code &gt; 0)
   */
  public boolean isAvailable() {
    return this.statusCode > 0;
  }

  /**
   * @return {@code true} if the token endpoint responded with HTTP 200
   */
  public boolean isSuccessful() {
    return this.statusCode == 200;
  }

  /**
   * @return the HTTP status code from the token endpoint, or {@code -1} if unavailable
   */
  public int statusCode() {
    return this.statusCode;
  }

  /**
   * @return the raw JSON response body, or {@code null} if none was received
   */
  public @Nullable String rawResponse() {
    return this.rawResponse;
  }

  /**
   * Returns the parsed access token, or {@code null} if the response was not successful or
   * did not include an {@code access_token}.
   *
   * @return the access token, or {@code null}
   */
  public @Nullable AccessToken accessToken() {
    return this.accessToken;
  }

  /**
   * @return the {@code refresh_token} parameter, or {@code null} if absent
   */
  public @Nullable String refreshToken() {
    return parameter("refresh_token");
  }

  /**
   * @return the {@code scope} parameter, or {@code null} if absent
   */
  public @Nullable String scope() {
    return parameter("scope");
  }

  /**
   * @return the {@code token_type} parameter (e.g. {@code "Bearer"}), or {@code null} if absent
   */
  public @Nullable String tokenType() {
    return parameter("token_type");
  }

  /**
   * @return the raw encoded {@code id_token} JWT, or {@code null} if absent
   */
  public @Nullable String idToken() {
    return parameter("id_token");
  }

  /**
   * @return the {@code error} parameter (RFC 6749 error code), or {@code null} if absent
   */
  public @Nullable String error() {
    return parameter("error");
  }

  /**
   * @return the {@code error_description} parameter, or {@code null} if absent
   */
  public @Nullable String errorDescription() {
    return parameter("error_description");
  }

  /**
   * @return the {@code error_uri} parameter, or {@code null} if absent
   */
  public @Nullable String errorUri() {
    return parameter("error_uri");
  }

  /**
   * @return the {@code expires_in} parameter as seconds, or {@code null} if absent or non-numeric
   */
  public @Nullable Long expiresInSeconds() {
    String value = this.parameters.get("expires_in");
    if (value == null) return null;
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Returns the raw string value of any response parameter by name, or {@code null} if absent.
   *
   * @param name the parameter name (e.g. {@code "access_token"})
   * @return the parameter value, or {@code null}
   */
  public @Nullable String parameter(String name) {
    Objects.requireNonNull(name, "name");
    return this.parameters.get(name);
  }

  // --- JWT identity claims ---

  /**
   * @return the {@code sub} claim (PageSeeder member ID as a string), or {@code null} if absent
   */
  public @Nullable String jwtSubject() {
    return jwtClaim("sub");
  }

  /**
   * @return the {@code preferred_username} claim, or {@code null} if absent
   */
  public @Nullable String jwtPreferredUsername() {
    return jwtClaim("preferred_username");
  }

  /**
   * @return the {@code email} claim, or {@code null} if absent
   */
  public @Nullable String jwtEmail() {
    return jwtClaim("email");
  }

  /**
   * @return the {@code given_name} claim, or {@code null} if absent
   */
  public @Nullable String jwtGivenName() {
    return jwtClaim("given_name");
  }

  /**
   * @return the {@code family_name} claim, or {@code null} if absent
   */
  public @Nullable String jwtFamilyName() {
    return jwtClaim("family_name");
  }

  /**
   * @return all raw JWT payload claims, or {@code null} if no valid {@code id_token} was present
   */
  public @Nullable Map<String, String> jwtClaims() {
    return this.jwtClaims;
  }

  private @Nullable String jwtClaim(String name) {
    return this.jwtClaims != null ? this.jwtClaims.get(name) : null;
  }

  // --- Factory methods ---

  /**
   * Parses a token endpoint response.
   *
   * @param statusCode   the HTTP status code returned by the token endpoint
   * @param rawResponse  the raw JSON response body
   * @param requestedAt  the time the request was sent, used to compute token expiry
   * @param credentials  the client credentials used to verify the {@code id_token} signature
   * @return the parsed response
   */
  public static TokenResponse parse(int statusCode, String rawResponse, Instant requestedAt, ClientCredentials credentials) {
    Objects.requireNonNull(rawResponse, "rawResponse");
    Objects.requireNonNull(requestedAt, "requestedAt");
    Objects.requireNonNull(credentials, "credentials");
    Map<String, String> parameters = OAuthJson.parseFlatObject(rawResponse);
    AccessToken accessToken = null;
    Map<String, String> jwtClaims = null;
    if (statusCode == 200) {
      accessToken = parseAccessToken(parameters, requestedAt);
      jwtClaims = parseJwtClaims(parameters.get("id_token"), credentials);
    }
    return new TokenResponse(statusCode, rawResponse, parameters, accessToken, jwtClaims);
  }

  static TokenResponse error(String error, @Nullable String description) {
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("error", error);
    if (description != null) {
      parameters.put("error_description", description);
    }
    return new TokenResponse(-1, null, parameters, null, null);
  }

  private static @Nullable AccessToken parseAccessToken(Map<String, String> parameters, Instant requestedAt) {
    String token = parameters.get("access_token");
    if (token == null) return null;
    String expiresIn = parameters.get("expires_in");
    Instant expiresAt = null;
    if (expiresIn != null) {
      try {
        expiresAt = requestedAt.plusSeconds(Long.parseLong(expiresIn));
      } catch (NumberFormatException ignored) {
        // Ignore invalid expiration time, token will expire when it expires
      }
    }
    return new AccessToken(token, expiresAt);
  }

  private static @Nullable Map<String, String> parseJwtClaims(@Nullable String idToken,
                                                               ClientCredentials credentials) {
    if (idToken == null) return null;
    String[] segments = idToken.split("\\.");
    if (segments.length != 3) return null;
    Map<String, String> header = OAuthJson.parseBase64UrlFlatObject(segments[0]);
    if (!"JWT".equals(header.get("typ")) || !"HS256".equals(header.get("alg"))) return null;
    if (!signatureMatches(segments, credentials.clientSecret())) return null;
    return OAuthJson.parseBase64UrlFlatObject(segments[1]);
  }

  private static boolean signatureMatches(String[] segments, String clientSecret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] signature = mac.doFinal((segments[0] + "." + segments[1]).getBytes(StandardCharsets.UTF_8));
      byte[] actual = Base64.getUrlDecoder().decode(segments[2]);
      return Arrays.equals(signature, actual);
    } catch (Exception ex) {
      return false;
    }
  }
}
