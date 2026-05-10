package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder membership.
 */
public final class Membership {

  private final long id;
  private final Member member;
  private final Group group;
  private final GroupRole role;
  private final MembershipStatus status;
  private final NotificationPreference notification;
  private final boolean listed;
  private final OffsetDateTime created;
  private final List<MembershipDetail> details;

  public Membership(long id, Member member, Group group, GroupRole role, MembershipStatus status,
                    NotificationPreference notification, boolean listed, OffsetDateTime created) {
    this(id, member, group, role, status, notification, listed, created, List.of());
  }

  public Membership(long id, Member member, Group group, GroupRole role, MembershipStatus status,
                    NotificationPreference notification, boolean listed, OffsetDateTime created,
                    List<MembershipDetail> details) {
    this.id = id;
    this.member = member;
    this.group = group;
    this.role = role == null ? GroupRole.UNKNOWN : role;
    this.status = status == null ? MembershipStatus.UNKNOWN : status;
    this.notification = notification == null ? NotificationPreference.UNKNOWN : notification;
    this.listed = listed;
    this.created = created;
    this.details = details == null ? List.of() : List.copyOf(details);
  }

  public long getId() {
    return this.id;
  }

  public Member getMember() {
    return this.member;
  }

  public Group getGroup() {
    return this.group;
  }

  public GroupRole getRole() {
    return this.role;
  }

  public MembershipStatus getStatus() {
    return this.status;
  }

  public NotificationPreference getNotification() {
    return this.notification;
  }

  public boolean isListed() {
    return this.listed;
  }

  public OffsetDateTime getCreated() {
    return this.created;
  }

  public List<MembershipDetail> getDetails() {
    return this.details;
  }
}
