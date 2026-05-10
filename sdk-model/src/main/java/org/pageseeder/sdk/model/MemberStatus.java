package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Member activation status.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public enum MemberStatus {

  /**
   * THe account is activated.
   */
  ACTIVATED("activated"),

  /**
   * The account is disabled.
   */
  DISABLED("disabled"),

  /**
   * The account email is verified by the user hasn't set their own password.
   */
  SET_PASSWORD("set-password"),

  /**
   * The account is not activated yet.
   */
  UNACTIVATED("unactivated"),

  /**
   * Any other status.
   */
  UNKNOWN("");

  private final String parameter;

  MemberStatus(String parameter) {
    this.parameter = parameter;
  }

  public String getParameter() {
    return parameter;
  }

  public static MemberStatus fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    try {
      return MemberStatus.valueOf(value.trim().toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }
}
