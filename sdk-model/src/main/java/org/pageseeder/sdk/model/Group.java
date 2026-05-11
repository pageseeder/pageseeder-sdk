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

  public Group(long id, String name, String title, String description, String owner, GroupRole defaultRole,
               NotificationPreference defaultNotification) {
    this(id, name, GroupType.UNKNOWN, title, description, owner, defaultRole, defaultNotification);
  }

  public Group {
    type = type == null ? GroupType.UNKNOWN : type;
    defaultRole = defaultRole == null ? GroupRole.UNKNOWN : defaultRole;
    defaultNotification = defaultNotification == null ? NotificationPreference.UNKNOWN : defaultNotification;
  }

  public long getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public GroupType getType() {
    return this.type;
  }

  public boolean isProject() {
    return this.type == GroupType.PROJECT;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDescription() {
    return this.description;
  }

  public String getOwner() {
    return this.owner;
  }

  public GroupRole getDefaultRole() {
    return this.defaultRole;
  }

  public NotificationPreference getDefaultNotification() {
    return this.defaultNotification;
  }
}
