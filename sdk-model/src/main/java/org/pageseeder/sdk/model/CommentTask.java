package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Immutable task attributes of a PageSeeder comment.
 *
 * @param status     the task status
 * @param priority   the task priority
 * @param due        the due timestamp
 * @param assignedTo the user assigned to the task
 */
public record CommentTask(@Nullable String status, @Nullable String priority, @Nullable OffsetDateTime due,
                          @Nullable StampedCommentUser assignedTo) {
}
