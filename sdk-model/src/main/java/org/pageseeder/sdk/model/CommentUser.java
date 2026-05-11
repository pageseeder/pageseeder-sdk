package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Immutable PageSeeder comment user.
 *
 * <p>A comment user is either a registered member or an unregistered/deleted
 * user represented only by a full name.
 *
 * @param member   the registered member, or {@code null} for an unregistered/deleted user
 * @param fullname the user's full name
 */
public record CommentUser(@Nullable Member member, String fullname) {

  /**
   * Creates a comment user, defaulting a missing full name to an empty string.
   *
   * @param member   the registered member, or {@code null} for an unregistered/deleted user
   * @param fullname the user's full name
   */
  public CommentUser {
    fullname = fullname == null ? "" : fullname;
  }

  /**
   * Returns the registered member for this comment user.
   *
   * @return the member, or {@code null} for an unregistered/deleted user
   */
  public @Nullable Member getMember() {
    return this.member;
  }

  /**
   * Returns the user's full name.
   *
   * @return the user's full name
   */
  public String getFullname() {
    return this.fullname;
  }

  /**
   * Indicates whether this comment user represents a registered member.
   *
   * @return {@code true} when a member is present
   */
  public boolean isMember() {
    return this.member != null;
  }
}
