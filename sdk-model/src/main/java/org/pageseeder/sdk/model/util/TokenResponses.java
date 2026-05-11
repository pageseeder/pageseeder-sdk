package org.pageseeder.sdk.model.util;

import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.MemberStatus;
import org.pageseeder.sdk.oauth.TokenResponse;

import org.jspecify.annotations.Nullable;

/**
 * Utility methods for converting {@link TokenResponse} into SDK model types.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TokenResponses {

  private TokenResponses() {
  }

  /**
   * Converts the JWT identity claims in a {@link TokenResponse} to a {@link Member}.
   *
   * <p>Returns {@code null} if the response contains no valid {@code id_token} or if
   * the {@code sub} claim is absent or not a valid long.
   *
   * @param response The token response.
   * @return A {@code Member} built from the JWT claims, or {@code null}.
   */
  public static @Nullable Member toMember(TokenResponse response) {
    String subject = response.jwtSubject();
    if (subject == null) {
      return null;
    }
    try {
      long id = Long.parseLong(subject);
      String username = response.jwtPreferredUsername();
      return new Member(
          id,
          username != null ? username : "",
          response.jwtEmail(),
          response.jwtGivenName(),
          response.jwtFamilyName(),
          MemberStatus.UNKNOWN,
          false,
          false,
          false,
          null
      );
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
