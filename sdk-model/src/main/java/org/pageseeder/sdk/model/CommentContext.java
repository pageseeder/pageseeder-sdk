package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder comment context.
 *
 * @param group      the group associated with the comment
 * @param uri        the resource URI associated with the comment
 * @param fragmentId the document fragment identifier associated with the comment
 */
public record CommentContext(Group group, ResourceUri uri, String fragmentId) {
}
