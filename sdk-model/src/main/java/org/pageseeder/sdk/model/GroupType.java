package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Distinguishes PageSeeder groups from projects.
 */
public enum GroupType {
  /** Standard PageSeeder group. */
  GROUP("group"),

  /** PageSeeder project. */
  PROJECT("project"),

  /** Unknown or unrecognized group type. */
  UNKNOWN("");

  /**
   * Parameter that can be sent to the service.
   */
  private final String parameter;

  GroupType(String parameter) {
    this.parameter = parameter;
  }

  /**
   * Parses a PageSeeder group type value.
   *
   * @param value the value to parse
   * @return the matching type, or {@link #UNKNOWN} when unknown
   */
  public static GroupType fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    try {
      return GroupType.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }

  /**
   * Returns the service parameter value for this group type.
   *
   * @return the service parameter value
   */
  public String getParameter() {
    return this.parameter;
  }
}
