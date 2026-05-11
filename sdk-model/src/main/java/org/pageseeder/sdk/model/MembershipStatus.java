package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Membership status.
 */
public enum MembershipStatus {
  /** Normal active membership. */
  NORMAL,

  /** Invited membership. */
  INVITED,

  /** Self-invited membership. */
  SELF_INVITED,

  /** Moderated membership. */
  MODERATED,

  /** Disabled membership. */
  DISABLED,

  /** Active membership. */
  ACTIVE,

  /** Pending membership. */
  PENDING,

  /** Inactive membership. */
  INACTIVE,

  /** Unknown or unrecognized membership status. */
  UNKNOWN;

  /**
   * Parses a PageSeeder membership status value.
   *
   * @param value the value to parse
   * @return the matching status, or {@link #UNKNOWN} when unknown
   */
  public static MembershipStatus fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    try {
      return MembershipStatus.valueOf(value.trim().toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }
}
