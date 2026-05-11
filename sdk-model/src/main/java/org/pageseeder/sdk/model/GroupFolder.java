package org.pageseeder.sdk.model;

/**
 * Immutable PageSeeder group folder.
 *
 * @param id           the group folder URI ID
 * @param scheme       the URI scheme
 * @param host         the URI host name
 * @param port         the URI port
 * @param path         the URI path
 * @param external     whether the URI is external
 * @param publicAccess whether the folder belongs to the public group
 * @param sharing      the folder sharing status
 */
public record GroupFolder(long id, String scheme, String host, int port, String path, boolean external,
                          GroupFolderPublicAccess publicAccess, GroupFolderSharing sharing) {

  /**
   * Creates a group folder, normalizing missing enum values to {@code UNKNOWN}.
   *
   * @param id           the group folder URI ID
   * @param scheme       the URI scheme
   * @param host         the URI host name
   * @param port         the URI port
   * @param path         the URI path
   * @param external     whether the URI is external
   * @param publicAccess whether the folder belongs to the public group
   * @param sharing      the folder sharing status
   */
  public GroupFolder {
    publicAccess = publicAccess == null ? GroupFolderPublicAccess.UNKNOWN : publicAccess;
    sharing = sharing == null ? GroupFolderSharing.UNKNOWN : sharing;
  }
}
