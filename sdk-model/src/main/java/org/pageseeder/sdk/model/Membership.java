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

  /**
   * Creates a membership without detail fields.
   *
   * @param id           the membership ID
   * @param member       the member associated with the membership
   * @param group        the group associated with the membership
   * @param role         the member role
   * @param status       the membership status
   * @param notification the notification preference
   * @param listed       whether the membership is listed
   * @param created      the creation timestamp
   */
  public Membership(long id, Member member, Group group, GroupRole role, MembershipStatus status,
                    NotificationPreference notification, boolean listed, OffsetDateTime created) {
    this(id, member, group, role, status, notification, listed, created, List.of());
  }

  /**
   * Creates a membership, normalizing missing enum values and detail lists.
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
  public Membership {
    role = role == null ? GroupRole.UNKNOWN : role;
    status = status == null ? MembershipStatus.UNKNOWN : status;
    notification = notification == null ? NotificationPreference.UNKNOWN : notification;
    details = details == null ? List.of() : List.copyOf(details);
  }

  /**
   * Returns the membership ID.
   *
   * @return the membership ID
   */
  public long getId() {
    return this.id;
  }

  /**
   * Returns the member associated with the membership.
   *
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * Returns the group associated with the membership.
   *
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * Returns the member role.
   *
   * @return the member role
   */
  public GroupRole getRole() {
    return this.role;
  }

  /**
   * Returns the membership status.
   *
   * @return the membership status
   */
  public MembershipStatus getStatus() {
    return this.status;
  }

  /**
   * Returns the notification preference.
   *
   * @return the notification preference
   */
  public NotificationPreference getNotification() {
    return this.notification;
  }

  /**
   * Indicates whether the membership is listed.
   *
   * @return {@code true} when the membership is listed
   */
  public boolean isListed() {
    return this.listed;
  }

  /**
   * Returns the membership creation timestamp.
   *
   * @return the creation timestamp
   */
  public OffsetDateTime getCreated() {
    return this.created;
  }

  /**
   * Returns the membership detail fields.
   *
   * @return the membership detail fields
   */
  public List<MembershipDetail> getDetails() {
    return this.details;
  }
}
