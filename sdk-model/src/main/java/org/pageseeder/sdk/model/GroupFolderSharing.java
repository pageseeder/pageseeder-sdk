package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Sharing status for a PageSeeder group folder.
 */
public enum GroupFolderSharing {

  /** The sharing field was omitted. */
  UNKNOWN,

  /** The folder is shared. */
  SHARED,

  /** The folder is private. */
  PRIVATE;

  /**
   * Parses a PageSeeder {@code sharing} field value.
   *
   * @param value the raw field value
   * @return the sharing status
   */
  public static GroupFolderSharing fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    return valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
  }
}
