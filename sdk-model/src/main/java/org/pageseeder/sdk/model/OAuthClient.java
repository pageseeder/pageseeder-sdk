package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.oauth.GrantType;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Immutable PageSeeder OAuth client.
 *
 * @param id                 the client database ID
 * @param identifier         the OAuth client identifier
 * @param requiresConsent    whether this client requires member consent
 * @param confidential       whether this is a confidential client
 * @param name               the client display name
 * @param grantType          the OAuth grant type
 * @param created            the client creation timestamp
 * @param modified           the last modification timestamp
 * @param lastToken          the timestamp when a token was last issued
 * @param appName            the PageSeeder application name
 * @param webhookSecret      the webhook secret
 * @param redirectUri        the OAuth redirect URI
 * @param description        the client description
 * @param clientUri          the client application URI
 * @param scope              the OAuth scope
 * @param accessTokenMaxAge  the access-token maximum age
 * @param refreshTokenMaxAge the refresh-token maximum age
 * @param member             the member that owns the client
 */
public record OAuthClient(@Nullable Long id, String identifier, boolean requiresConsent, boolean confidential,
                          String name, @Nullable GrantType grantType, @Nullable OffsetDateTime created,
                          @Nullable OffsetDateTime modified, @Nullable OffsetDateTime lastToken,
                          @Nullable String appName, @Nullable String webhookSecret, @Nullable URI redirectUri,
                          @Nullable String description, @Nullable URI clientUri, @Nullable String scope,
                          Duration accessTokenMaxAge, Duration refreshTokenMaxAge, @Nullable Member member) {

  /**
   * Creates an OAuth client with non-null token lifetime values.
   *
   * @param id                 the client database ID
   * @param identifier         the OAuth client identifier
   * @param requiresConsent    whether this client requires member consent
   * @param confidential       whether this is a confidential client
   * @param name               the client display name
   * @param grantType          the OAuth grant type
   * @param created            the client creation timestamp
   * @param modified           the last modification timestamp
   * @param lastToken          the timestamp when a token was last issued
   * @param appName            the PageSeeder application name
   * @param webhookSecret      the webhook secret
   * @param redirectUri        the OAuth redirect URI
   * @param description        the client description
   * @param clientUri          the client application URI
   * @param scope              the OAuth scope
   * @param accessTokenMaxAge  the access-token maximum age
   * @param refreshTokenMaxAge the refresh-token maximum age
   * @param member             the member that owns the client
   */
  public OAuthClient {
    accessTokenMaxAge = accessTokenMaxAge == null ? Duration.ZERO : accessTokenMaxAge;
    refreshTokenMaxAge = refreshTokenMaxAge == null ? Duration.ZERO : refreshTokenMaxAge;
  }

  /**
   * Returns the access-token maximum age in seconds.
   *
   * @return the access-token maximum age in seconds
   */
  public long accessTokenMaxAgeSeconds() {
    return this.accessTokenMaxAge.getSeconds();
  }

  /**
   * Returns the refresh-token maximum age in seconds.
   *
   * @return the refresh-token maximum age in seconds
   */
  public long refreshTokenMaxAgeSeconds() {
    return this.refreshTokenMaxAge.getSeconds();
  }
}
