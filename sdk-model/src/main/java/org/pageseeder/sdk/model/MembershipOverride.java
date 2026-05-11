package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Membership fields overridden from inherited subgroup defaults.
 */
public enum MembershipOverride {

  /** The listed flag is overridden. */
  LISTED,

  /** The notification preference is overridden. */
  NOTIFICATION,

  /** The role is overridden. */
  ROLE,

  /** Unknown or unrecognized override field. */
  UNKNOWN;

  /**
   * Parses a PageSeeder membership override value.
   *
   * @param value the value to parse
   * @return the matching override, or {@link #UNKNOWN} when unknown
   */
  public static MembershipOverride fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    try {
      return MembershipOverride.valueOf(value.trim().toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }
}
