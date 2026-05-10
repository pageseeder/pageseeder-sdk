package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder comment context.
 */
public final class CommentContext {

  private final Group group;
  private final ResourceUri uri;
  private final String fragmentId;

  public CommentContext(Group group, ResourceUri uri, String fragmentId) {
    this.group = group;
    this.uri = uri;
    this.fragmentId = fragmentId;
  }

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
