package org.pageseeder.sdk.model;

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
public record GroupSettings(String visibility, String template, String detailsType, Boolean editUrls,
                            String commenting, String moderation, String registration,
                            GroupRole defaultRole, NotificationPreference defaultNotification,
                            Integer indexVersion, String message) {

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
