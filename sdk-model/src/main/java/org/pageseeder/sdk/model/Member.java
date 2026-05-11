package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Immutable PageSeeder member.
 *
 * @param id                 the member ID
 * @param username           the member username
 * @param email              the member email address
 * @param firstname          the member first name
 * @param surname            the member surname
 * @param status             the member status
 * @param locked             whether the member is locked
 * @param onVacation         whether the member is on vacation
 * @param attachments        whether the member accepts attachments
 * @param externalId         the external user ID
 * @param created            the member creation timestamp
 * @param activated          the member activation timestamp
 * @param lastPasswordChange the last password change timestamp
 * @param lastLogin          the member's last login timestamp
 * @param admin              whether the member is an administrator
 * @param date               the contextual member timestamp
 */
public record Member(long id, String username, @Nullable String email, String firstname, String surname,
                     MemberStatus status, boolean locked, boolean onVacation, boolean attachments,
                     @Nullable String externalId, @Nullable OffsetDateTime created,
                     @Nullable OffsetDateTime activated, @Nullable OffsetDateTime lastPasswordChange,
                     @Nullable OffsetDateTime lastLogin, boolean admin, @Nullable OffsetDateTime date) {


  /**
   * Creates a member without extended metadata.
   *
   * @param id          the member ID
   * @param username    the member username
   * @param email       the member email address
   * @param firstname   the member first name
   * @param surname     the member surname
   */
  public Member(long id, String username, @Nullable String email, String firstname, String surname) {
    this(id, username, email, firstname, surname, MemberStatus.UNKNOWN, false, false, false, null, null, null, null,
        null, false, null);
  }

  /**
   * Creates a member without extended metadata.
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
  public Member(long id, String username, @Nullable String email, String firstname, String surname,
                MemberStatus status, boolean locked, boolean onVacation, boolean attachments,
                @Nullable OffsetDateTime lastLogin) {
    this(id, username, email, firstname, surname, status, locked, onVacation, attachments, null, null, null, null,
        lastLogin, false, null);
  }

  /**
   * Creates a member, normalizing missing optional text fields and status values.
   *
   * @param id                 the member ID
   * @param username           the member username
   * @param email              the member email address
   * @param firstname          the member first name
   * @param surname            the member surname
   * @param status             the member status
   * @param locked             whether the member is locked
   * @param onVacation         whether the member is on vacation
   * @param attachments        whether the member accepts attachments
   * @param externalId         the external user ID
   * @param created            the member creation timestamp
   * @param activated          the member activation timestamp
   * @param lastPasswordChange the last password change timestamp
   * @param lastLogin          the member's last login timestamp
   * @param admin              whether the member is an administrator
   * @param date               the contextual member timestamp
   */
  public Member {
    Objects.requireNonNull(username, "username");
    firstname = Objects.toString(firstname, "");
    surname = Objects.toString(surname, "");
    status = status == null ? MemberStatus.UNKNOWN : status;
  }

  /**
   * Returns the member full name.
   *
   * @return the member full name
   */
  public String fullname() {
    return (this.firstname + " " + this.surname).trim();
  }
}
