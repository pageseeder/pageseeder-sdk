package org.pageseeder.sdk.model;

/**
 * Group role.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public enum GroupRole {

  /** 'Guest' role PageSeeder. */
  GUEST("Guest"),

  /** 'Reviewer' role PageSeeder. */
  REVIEWER("Reviewer"),

  /** 'Contributor' role PageSeeder. */
  CONTRIBUTOR("Contributor"),

  /** 'Manager' role PageSeeder. */
  MANAGER("Manager"),

  /** 'Moderator' role PageSeeder. */
  MODERATOR("Moderator"),

  /** 'Approver' role PageSeeder. */
  APPROVER("Approver"),

  /** 'Mod &amp; App' role PageSeeder. */
  MODERATOR_AND_APPROVER("Mod & App"),

  /** 'Unknown' role PageSeeder. */
  UNKNOWN("Unknown");

  /**
   * Parameter that can be sent to the service.
   */
  private final String parameter;

  /**
   * Sole constructor.
   *
   * @param p The name of the parameter for the PageSeeder services.
   */
  GroupRole(String p) {
    this.parameter = p;
  }

  public static GroupRole fromValue(String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    try {
      return GroupRole.valueOf(value.trim().toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }

  public String getParameter() {
    return this.parameter;
  }

}
