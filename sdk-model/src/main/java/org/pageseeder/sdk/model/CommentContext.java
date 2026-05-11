package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder comment context.
 *
 * @param group      the group associated with the comment
 * @param uri        the resource URI associated with the comment
 * @param fragmentId the document fragment identifier associated with the comment
 */
public record CommentContext(Group group, ResourceUri uri, String fragmentId) {

  public Group getGroup() {
    return this.group;
  }

  public ResourceUri getUri() {
    return this.uri;
  }

  public String getFragmentId() {
    return this.fragmentId;
  }
}
