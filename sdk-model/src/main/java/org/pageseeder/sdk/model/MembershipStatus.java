package org.pageseeder.sdk.model;

/**
 * Membership status.
 */
public enum MembershipStatus {
  ACTIVE,
  PENDING,
  INACTIVE,
  UNKNOWN;

  public static MembershipStatus fromValue(String value) {
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
