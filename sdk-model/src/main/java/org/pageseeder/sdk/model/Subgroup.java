package org.pageseeder.sdk.model;

import java.util.Objects;

/**
 * Immutable PageSeeder subgroup relationship.
 *
 * @param id           the subgroup relationship ID
 * @param role         the inherited or explicit role for the subgroup
 * @param notification the inherited or explicit notification preference for the subgroup
 * @param listed       the inherited or explicit listed status for the subgroup
 * @param group        the subgroup
 */
public record Subgroup(long id, GroupRole role, NotificationPreference notification, GroupRelationListing listed,
                       Group group) {

  /**
   * Creates a subgroup relationship, normalizing missing enum values.
   *
   * @param id           the subgroup relationship ID
   * @param role         the inherited or explicit role for the subgroup
   * @param notification the inherited or explicit notification preference for the subgroup
   * @param listed       the inherited or explicit listed status for the subgroup
   * @param group        the subgroup
   */
  public Subgroup {
    role = role == null ? GroupRole.UNKNOWN : role;
    notification = notification == null ? NotificationPreference.UNKNOWN : notification;
    listed = listed == null ? GroupRelationListing.UNKNOWN : listed;
    Objects.requireNonNull(group, "group");
  }
}
