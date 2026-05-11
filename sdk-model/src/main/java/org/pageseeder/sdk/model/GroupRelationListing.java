package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Listed status for a PageSeeder group relationship.
 */
public enum GroupRelationListing {

  /** The relationship is listed. */
  LISTED,

  /** The relationship is not listed. */
  NOT_LISTED,

  /** The relationship inherits its listed status. */
  INHERIT,

  /** Unknown or unrecognized listed status. */
  UNKNOWN;

  /**
   * Parses a PageSeeder {@code listed} field value.
   *
   * @param value the raw field value
   * @return the listed status
   */
  public static GroupRelationListing fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    if ("true".equalsIgnoreCase(value)) {
      return LISTED;
    }
    if ("false".equalsIgnoreCase(value)) {
      return NOT_LISTED;
    }
    try {
      return valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }
}
