package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Public-group access status for a PageSeeder group folder.
 */
public enum GroupFolderPublicAccess {

  /** The public access flag was omitted or could not be determined. */
  UNKNOWN,

  /** The folder belongs to the public group. */
  PUBLIC,

  /** The folder does not belong to the public group. */
  NOT_PUBLIC;

  /**
   * Parses a PageSeeder {@code public} field value.
   *
   * @param value the raw field value
   * @return the public access status
   */
  public static GroupFolderPublicAccess fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    if ("true".equalsIgnoreCase(value)) {
      return PUBLIC;
    }
    if ("false".equalsIgnoreCase(value)) {
      return NOT_PUBLIC;
    }
    return valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
  }
}
