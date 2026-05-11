package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;

/**
 * Immutable PageSeeder stamped comment user.
 *
 * @param user the comment user
 * @param date the timestamp associated with the user
 */
public record StampedCommentUser(CommentUser user, OffsetDateTime date) {

  public CommentUser getUser() {
    return this.user;
  }

  public OffsetDateTime getDate() {
    return this.date;
  }
}
