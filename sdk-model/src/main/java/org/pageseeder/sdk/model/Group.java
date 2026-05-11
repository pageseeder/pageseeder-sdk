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
 * @param defaultRole         the default role for members
 * @param defaultNotification the default notification preference
 */
public record Group(long id, String name, GroupType type, String title, String description, String owner,
                    GroupRole defaultRole, NotificationPreference defaultNotification) {

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
    this(id, name, GroupType.UNKNOWN, title, description, owner, defaultRole, defaultNotification);
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
   * @param defaultRole         the default role for members
   * @param defaultNotification the default notification preference
   */
  public Group {
    type = type == null ? GroupType.UNKNOWN : type;
    defaultRole = defaultRole == null ? GroupRole.UNKNOWN : defaultRole;
    defaultNotification = defaultNotification == null ? NotificationPreference.UNKNOWN : defaultNotification;
  }

  /**
   * Returns the group ID.
   *
   * @return the group ID
   */
  public long getId() {
    return this.id;
  }

  /**
   * Returns the group name.
   *
   * @return the group name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the group type.
   *
   * @return the group type
   */
  public GroupType getType() {
    return this.type;
  }

  /**
   * Indicates whether this group is a project.
   *
   * @return {@code true} when the group type is {@link GroupType#PROJECT}
   */
  public boolean isProject() {
    return this.type == GroupType.PROJECT;
  }

  /**
   * Returns the group title.
   *
   * @return the group title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Returns the group description.
   *
   * @return the group description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Returns the group owner.
   *
   * @return the group owner
   */
  public String getOwner() {
    return this.owner;
  }

  /**
   * Returns the default role for members.
   *
   * @return the default member role
   */
  public GroupRole getDefaultRole() {
    return this.defaultRole;
  }

  /**
   * Returns the default notification preference.
   *
   * @return the default notification preference
   */
  public NotificationPreference getDefaultNotification() {
    return this.defaultNotification;
  }
}
