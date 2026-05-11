package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Immutable PageSeeder comment context.
 *
 * @param group      the group associated with the comment
 * @param uri        the resource URI associated with the comment
 * @param fragmentId the document fragment identifier associated with the comment
 */
public record CommentContext(@Nullable Group group, @Nullable ResourceUri uri, @Nullable String fragmentId) {
}
