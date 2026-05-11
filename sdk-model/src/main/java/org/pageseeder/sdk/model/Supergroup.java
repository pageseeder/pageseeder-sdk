package org.pageseeder.sdk.model;

import java.util.Objects;

/**
 * Immutable PageSeeder supergroup relationship.
 *
 * @param id           the supergroup relationship ID
 * @param role         the inherited or explicit role for the supergroup
 * @param notification the inherited or explicit notification preference for the supergroup
 * @param listed       the inherited or explicit listed status for the supergroup
 * @param group        the supergroup
 */
public record Supergroup(long id, GroupRole role, NotificationPreference notification, GroupRelationListing listed,
                         Group group) {

  /**
   * Creates a supergroup relationship, normalizing missing enum values.
   *
   * @param id           the supergroup relationship ID
   * @param role         the inherited or explicit role for the supergroup
   * @param notification the inherited or explicit notification preference for the supergroup
   * @param listed       the inherited or explicit listed status for the supergroup
   * @param group        the supergroup
   */
  public Supergroup {
    role = role == null ? GroupRole.UNKNOWN : role;
    notification = notification == null ? NotificationPreference.UNKNOWN : notification;
    listed = listed == null ? GroupRelationListing.UNKNOWN : listed;
    Objects.requireNonNull(group, "group");
  }
}
