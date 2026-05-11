package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Immutable PageSeeder stamped comment user.
 *
 * @param user the comment user
 * @param date the timestamp associated with the user
 */
public record StampedCommentUser(CommentUser user, @Nullable OffsetDateTime date) {
}
