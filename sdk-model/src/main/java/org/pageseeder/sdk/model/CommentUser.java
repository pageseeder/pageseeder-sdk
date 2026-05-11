package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder comment user.
 *
 * <p>A comment user is either a registered member or an unregistered/deleted
 * user represented only by a full name.
 *
 * @param member   the registered member, or {@code null} for an unregistered/deleted user
 * @param fullname the user's full name
 */
public record CommentUser(Member member, String fullname) {

  public CommentUser {
    fullname = fullname == null ? "" : fullname;
  }

  public Member getMember() {
    return this.member;
  }

  public String getFullname() {
    return this.fullname;
  }

  public boolean isMember() {
    return this.member != null;
  }
}
