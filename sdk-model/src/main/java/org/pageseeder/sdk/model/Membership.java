package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
 * @param deleted      whether the membership is deleted
 * @param subgroups    the subgroup names this membership applies to
 * @param overrides    the fields overridden from inherited subgroup defaults
 * @param created      the creation timestamp
 * @param details      the membership detail fields
 */
public record Membership(long id, Member member, Group group, GroupRole role, MembershipStatus status,
                         NotificationPreference notification, boolean listed, boolean deleted, List<String> subgroups,
                         Set<MembershipOverride> overrides, OffsetDateTime created, List<MembershipDetail> details) {

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
    this(id, member, group, role, status, notification, listed, false, List.of(), Set.of(), created, List.of());
  }

  /**
   * Creates a membership without deleted, subgroup, and override metadata.
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
  public Membership(long id, Member member, Group group, GroupRole role, MembershipStatus status,
                    NotificationPreference notification, boolean listed, OffsetDateTime created,
                    List<MembershipDetail> details) {
    this(id, member, group, role, status, notification, listed, false, List.of(), Set.of(), created, details);
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
   * @param deleted      whether the membership is deleted
   * @param subgroups    the subgroup names this membership applies to
   * @param overrides    the fields overridden from inherited subgroup defaults
   * @param created      the creation timestamp
   * @param details      the membership detail fields
   */
  public Membership {
    Objects.requireNonNull(member, "member");
    Objects.requireNonNull(group, "group");
    role = role == null ? GroupRole.UNKNOWN : role;
    status = status == null ? MembershipStatus.UNKNOWN : status;
    notification = notification == null ? NotificationPreference.UNKNOWN : notification;
    subgroups = subgroups == null ? List.of() : List.copyOf(subgroups);
    overrides = overrides == null ? Set.of() : Set.copyOf(overrides);
    details = details == null ? List.of() : List.copyOf(details);
  }

}
