package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Distinguishes PageSeeder groups from projects.
 */
public enum GroupType {
  GROUP("group"),
  PROJECT("project"),
  UNKNOWN("");

  /**
   * Parameter that can be sent to the service.
   */
  private final String parameter;

  GroupType(String parameter) {
    this.parameter = parameter;
  }

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

  public String getParameter() {
    return this.parameter;
  }
}
