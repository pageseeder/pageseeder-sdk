package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder comment context.
 *
 * @param group      the group associated with the comment
 * @param uri        the resource URI associated with the comment
 * @param fragmentId the document fragment identifier associated with the comment
 */
public record CommentContext(Group group, ResourceUri uri, String fragmentId) {

  /**
   * Returns the group associated with the comment.
   *
   * @return the comment group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * Returns the resource URI associated with the comment.
   *
   * @return the resource URI
   */
  public ResourceUri getUri() {
    return this.uri;
  }

  /**
   * Returns the document fragment identifier associated with the comment.
   *
   * @return the fragment identifier
   */
  public String getFragmentId() {
    return this.fragmentId;
  }
}
