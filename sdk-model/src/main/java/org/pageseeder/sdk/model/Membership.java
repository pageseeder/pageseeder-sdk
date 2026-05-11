package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder membership.
 *
 * @param id           the membership ID
 * @param member       the member associated with the membership
 * @param group        the group associated with the membership
 * @param role         the member role
 * @param status       the membership status
 * @param notification the notification preference
 * @param listed       whether the membership is listed
 * @param created      the creation timestamp
 * @param details      the membership detail fields
 */
public record Membership(long id, Member member, Group group, GroupRole role, MembershipStatus status,
                         NotificationPreference notification, boolean listed, OffsetDateTime created,
                         List<MembershipDetail> details) {

  public Membership(long id, Member member, Group group, GroupRole role, MembershipStatus status,
                    NotificationPreference notification, boolean listed, OffsetDateTime created) {
    this(id, member, group, role, status, notification, listed, created, List.of());
  }

  public Membership {
    role = role == null ? GroupRole.UNKNOWN : role;
    status = status == null ? MembershipStatus.UNKNOWN : status;
    notification = notification == null ? NotificationPreference.UNKNOWN : notification;
    details = details == null ? List.of() : List.copyOf(details);
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
