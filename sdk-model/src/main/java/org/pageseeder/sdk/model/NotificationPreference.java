package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Membership or group notification preference.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public enum NotificationPreference {

  /**
   * No notification.
   */
  NONE("none"),

  /**
   * Immediate notification.
   */
  IMMEDIATE("immediate"),

  /**
   * Daily digest at scheduled time.
   */
  DAILY("daily"),

  /**
   * Daily digest at scheduled time.
   */
  WEEKLY("weekly"),

  /**
   * Essential people in the task.
   */
  ESSENTIAL("essential"),

  /**
   * Unknown notification preference.
   */
  UNKNOWN("");

  /**
   * Parameter that can be sent to the service.
   */
  private final String parameter;

  NotificationPreference(String parameter) {
    this.parameter = parameter;
  }

  /**
   * Return the parameter that can be sent to the service.
   *
   * @return the parameter that can be sent to the service.
   */
  public String getParameter() {
    return this.parameter;
  }

  public static NotificationPreference fromValue(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }
    try {
      return NotificationPreference.valueOf(value.trim().toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }
}
