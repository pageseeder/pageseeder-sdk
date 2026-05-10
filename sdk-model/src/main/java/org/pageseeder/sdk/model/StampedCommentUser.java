package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;

/**
 * Immutable PageSeeder stamped comment user.
 */
public final class StampedCommentUser {

  private final CommentUser user;
  private final OffsetDateTime date;

  public StampedCommentUser(CommentUser user, OffsetDateTime date) {
    this.user = user;
    this.date = date;
  }

  public CommentUser getUser() {
    return this.user;
  }

  public OffsetDateTime getDate() {
    return this.date;
  }
}
