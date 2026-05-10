package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder comment user.
 *
 * <p>A comment user is either a registered member or an unregistered/deleted
 * user represented only by a full name.
 */
public final class CommentUser {

  private final Member member;
  private final String fullname;

  public CommentUser(Member member, String fullname) {
    this.member = member;
    this.fullname = fullname == null ? "" : fullname;
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
