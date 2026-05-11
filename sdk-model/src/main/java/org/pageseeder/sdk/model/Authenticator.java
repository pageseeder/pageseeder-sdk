package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Immutable PageSeeder authenticator.
 *
 * @param id         the authenticator ID
 * @param memberId   the owner member ID
 * @param publicId   the public authenticator ID
 * @param data       confidential setup data, only present before verification
 * @param name       the authenticator display name
 * @param type       the authenticator type
 * @param created    the creation timestamp
 * @param lastUsed   the last-used timestamp
 * @param verified   whether the authenticator has been verified
 * @param parameters the authenticator parameters
 */
public record Authenticator(long id, long memberId, @Nullable String publicId, @Nullable String data,
                            @Nullable String name, @Nullable String type, @Nullable OffsetDateTime created,
                            @Nullable OffsetDateTime lastUsed, boolean verified, Map<String, String> parameters) {

  /**
   * Creates an authenticator with immutable parameters.
   *
   * @param id         the authenticator ID
   * @param memberId   the owner member ID
   * @param publicId   the public authenticator ID
   * @param data       confidential setup data, only present before verification
   * @param name       the authenticator display name
   * @param type       the authenticator type
   * @param created    the creation timestamp
   * @param lastUsed   the last-used timestamp
   * @param verified   whether the authenticator has been verified
   * @param parameters the authenticator parameters
   */
  public Authenticator {
    //noinspection ConstantValue (Defensive check)
    parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
  }
}
