package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Immutable PageSeeder member.
 *
 * @param id          the member ID
 * @param username    the member username
 * @param email       the member email address
 * @param firstname   the member first name
 * @param surname     the member surname
 * @param status      the member status
 * @param locked      whether the member is locked
 * @param onVacation  whether the member is on vacation
 * @param attachments whether the member accepts attachments
 * @param lastLogin   the member's last login timestamp
 */
public record Member(long id, String username, String email, String firstname, String surname, MemberStatus status,
                     boolean locked, boolean onVacation, boolean attachments, OffsetDateTime lastLogin) {

  public Member {
    username = Objects.requireNonNull(username, "username");
    firstname = firstname == null ? "" : firstname;
    surname = surname == null ? "" : surname;
    status = status == null ? MemberStatus.UNKNOWN : status;
  }

  public long getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getEmail() {
    return this.email;
  }

  public String getFirstname() {
    return this.firstname;
  }

  public String getSurname() {
    return this.surname;
  }

  public String getFullname() {
    return (this.firstname + " " + this.surname).trim();
  }

  public MemberStatus getStatus() {
    return this.status;
  }

  public boolean isLocked() {
    return this.locked;
  }

  public boolean isOnVacation() {
    return this.onVacation;
  }

  public boolean hasAttachments() {
    return this.attachments;
  }

  public OffsetDateTime getLastLogin() {
    return this.lastLogin;
  }
}
