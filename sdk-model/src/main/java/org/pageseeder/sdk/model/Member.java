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

  /**
   * Creates a member, normalizing missing optional text fields and status values.
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
  public Member {
    username = Objects.requireNonNull(username, "username");
    firstname = firstname == null ? "" : firstname;
    surname = surname == null ? "" : surname;
    status = status == null ? MemberStatus.UNKNOWN : status;
  }

  /**
   * Returns the member ID.
   *
   * @return the member ID
   */
  public long getId() {
    return this.id;
  }

  /**
   * Returns the member username.
   *
   * @return the member username
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Returns the member email address.
   *
   * @return the member email address
   */
  public String getEmail() {
    return this.email;
  }

  /**
   * Returns the member first name.
   *
   * @return the member first name
   */
  public String getFirstname() {
    return this.firstname;
  }

  /**
   * Returns the member surname.
   *
   * @return the member surname
   */
  public String getSurname() {
    return this.surname;
  }

  /**
   * Returns the member full name.
   *
   * @return the member full name
   */
  public String getFullname() {
    return (this.firstname + " " + this.surname).trim();
  }

  /**
   * Returns the member status.
   *
   * @return the member status
   */
  public MemberStatus getStatus() {
    return this.status;
  }

  /**
   * Indicates whether the member account is locked.
   *
   * @return {@code true} when the member is locked
   */
  public boolean isLocked() {
    return this.locked;
  }

  /**
   * Indicates whether the member is on vacation.
   *
   * @return {@code true} when the member is on vacation
   */
  public boolean isOnVacation() {
    return this.onVacation;
  }

  /**
   * Indicates whether the member accepts attachments.
   *
   * @return {@code true} when the member accepts attachments
   */
  public boolean hasAttachments() {
    return this.attachments;
  }

  /**
   * Returns the member's last login timestamp.
   *
   * @return the last login timestamp
   */
  public OffsetDateTime getLastLogin() {
    return this.lastLogin;
  }
}
