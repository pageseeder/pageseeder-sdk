package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder group.
 *
 * @param id                  the group ID
 * @param name                the group name
 * @param type                the group type
 * @param title               the group title
 * @param description         the group description
 * @param owner               the group owner
 * @param access              the group access level
 * @param common              whether the group is common
 * @param relatedUrl          the related URL for the group
 * @param defaultRole         the default role for members
 * @param defaultNotification the default notification preference
 */
public record Group(long id, String name, GroupType type, String title, String description, String owner,
                    String access, boolean common, String relatedUrl,
                    GroupRole defaultRole, NotificationPreference defaultNotification) {

  /**
   * Creates a group with no core settings.
   *
   * @param id                  the group ID
   * @param name                the group name
   * @param type                the group type
   * @param title               the group title
   * @param description         the group description
   * @param owner               the group owner
   * @param defaultRole         the default role for members
   * @param defaultNotification the default notification preference
   */
  public Group(long id, String name, GroupType type, String title, String description, String owner,
               GroupRole defaultRole, NotificationPreference defaultNotification) {
    this(id, name, type, title, description, owner, null, false, null, defaultRole, defaultNotification);
  }

  /**
   * Creates a group with an unknown group type.
   *
   * @param id                  the group ID
   * @param name                the group name
   * @param title               the group title
   * @param description         the group description
   * @param owner               the group owner
   * @param defaultRole         the default role for members
   * @param defaultNotification the default notification preference
   */
  public Group(long id, String name, String title, String description, String owner, GroupRole defaultRole,
               NotificationPreference defaultNotification) {
    this(id, name, GroupType.UNKNOWN, title, description, owner, null, false, null, defaultRole, defaultNotification);
  }

  /**
   * Creates a group, normalizing missing enum values to {@code UNKNOWN}.
   *
   * @param id                  the group ID
   * @param name                the group name
   * @param type                the group type
   * @param title               the group title
   * @param description         the group description
   * @param owner               the group owner
   * @param access              the group access level
   * @param common              whether the group is common
   * @param relatedUrl          the related URL for the group
   * @param defaultRole         the default role for members
   * @param defaultNotification the default notification preference
   */
  public Group {
    type = type == null ? GroupType.UNKNOWN : type;
    defaultRole = defaultRole == null ? GroupRole.UNKNOWN : defaultRole;
    defaultNotification = defaultNotification == null ? NotificationPreference.UNKNOWN : defaultNotification;
  }
}
