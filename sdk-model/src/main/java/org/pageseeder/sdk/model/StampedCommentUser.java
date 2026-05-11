package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;

/**
 * Immutable PageSeeder stamped comment user.
 *
 * @param user the comment user
 * @param date the timestamp associated with the user
 */
public record StampedCommentUser(CommentUser user, OffsetDateTime date) {

  /**
   * Returns the comment user.
   *
   * @return the comment user
   */
  public CommentUser getUser() {
    return this.user;
  }

  /**
   * Returns the timestamp associated with the user.
   *
   * @return the timestamp associated with the user
   */
  public OffsetDateTime getDate() {
    return this.date;
  }
}
