package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Immutable PageSeeder member.
 */
public final class Member {

  private final long id;
  private final String username;
  private final String email;
  private final String firstname;
  private final String surname;
  private final MemberStatus status;
  private final boolean locked;
  private final boolean onVacation;
  private final boolean attachments;
  private final OffsetDateTime lastLogin;

  public Member(long id, String username, String email, String firstname, String surname, MemberStatus status,
                boolean locked, boolean onVacation, boolean attachments, OffsetDateTime lastLogin) {
    this.id = id;
    this.username = Objects.requireNonNull(username, "username");
    this.email = email;
    this.firstname = firstname == null ? "" : firstname;
    this.surname = surname == null ? "" : surname;
    this.status = status == null ? MemberStatus.UNKNOWN : status;
    this.locked = locked;
    this.onVacation = onVacation;
    this.attachments = attachments;
    this.lastLogin = lastLogin;
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
