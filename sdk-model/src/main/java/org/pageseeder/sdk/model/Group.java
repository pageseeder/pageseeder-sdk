package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder group.
 */
public final class Group {

  private final long id;
  private final String name;
  private final GroupType type;
  private final String title;
  private final String description;
  private final String owner;
  private final GroupRole defaultRole;
  private final NotificationPreference defaultNotification;

  public Group(long id, String name, String title, String description, String owner, GroupRole defaultRole,
               NotificationPreference defaultNotification) {
    this(id, name, GroupType.UNKNOWN, title, description, owner, defaultRole, defaultNotification);
  }

  public Group(long id, String name, GroupType type, String title, String description, String owner,
               GroupRole defaultRole, NotificationPreference defaultNotification) {
    this.id = id;
    this.name = name;
    this.type = type == null ? GroupType.UNKNOWN : type;
    this.title = title;
    this.description = description;
    this.owner = owner;
    this.defaultRole = defaultRole == null ? GroupRole.UNKNOWN : defaultRole;
    this.defaultNotification = defaultNotification == null ? NotificationPreference.UNKNOWN : defaultNotification;
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
