package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Optional PageSeeder group configuration settings.
 *
 * @param visibility          the control group name
 * @param template            the owner directory or template group name
 * @param detailsType         the details form type
 * @param editUrls            whether URLs can be edited
 * @param commenting          who can comment
 * @param moderation          moderation mode
 * @param registration        registration mode
 * @param defaultRole         the default role for new members
 * @param defaultNotification the default notification preference
 * @param indexVersion        the index version
 * @param message             the group message
 */
public record GroupSettings(@Nullable String visibility, @Nullable String template, @Nullable String detailsType,
                            @Nullable Boolean editUrls, @Nullable String commenting, @Nullable String moderation,
                            @Nullable String registration, GroupRole defaultRole,
                            NotificationPreference defaultNotification, @Nullable Integer indexVersion,
                            @Nullable String message) {

  /**
   * Creates group settings, normalizing missing enum values to {@code UNKNOWN}.
   *
   * @param visibility          the control group name
   * @param template            the owner directory or template group name
   * @param detailsType         the details form type
   * @param editUrls            whether URLs can be edited
   * @param commenting          who can comment
   * @param moderation          moderation mode
   * @param registration        registration mode
   * @param defaultRole         the default role for new members
   * @param defaultNotification the default notification preference
   * @param indexVersion        the index version
   * @param message             the group message
   */
  public GroupSettings {
    defaultRole = defaultRole == null ? GroupRole.UNKNOWN : defaultRole;
    defaultNotification = defaultNotification == null ? NotificationPreference.UNKNOWN : defaultNotification;
  }
}
