package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder resource URI metadata.
 *
 * @param id          the resource URI ID
 * @param scheme      the URI scheme
 * @param host        the URI host
 * @param port        the URI port, or {@code -1} if unspecified
 * @param path        the URI path
 * @param decodedPath the decoded URI path
 * @param title       the resource title
 * @param displayTitle the resource display title
 * @param docid       the document identifier
 * @param description the resource description
 * @param mediaType   the resource media type
 * @param documentType the internal document type
 * @param urlType     the external URL type
 * @param created     the creation timestamp
 * @param modified    the last modification timestamp
 * @param size        the resource size in bytes
 * @param labels      the resource labels
 * @param external    whether the URI is external
 * @param sharing     the folder sharing status
 */
public record ResourceUri(long id, String scheme, String host, int port, String path, String decodedPath, String title,
                          String displayTitle, String docid, String description, String mediaType, String documentType,
                          String urlType, OffsetDateTime created, OffsetDateTime modified, long size,
                          List<String> labels, boolean external, GroupFolderSharing sharing) {

  /**
   * Creates resource URI metadata without the newer server fields.
   *
   * @param id          the resource URI ID
   * @param scheme      the URI scheme
   * @param host        the URI host
   * @param port        the URI port, or {@code -1} if unspecified
   * @param path        the URI path
   * @param title       the resource title
   * @param docid       the document identifier
   * @param description the resource description
   * @param mediaType   the resource media type
   * @param created     the creation timestamp
   * @param modified    the last modification timestamp
   * @param labels      the resource labels
   * @param external    whether the URI is external
   * @param folder      whether the URI represents a folder
   */
  public ResourceUri(long id, String scheme, String host, int port, String path, String title, String docid,
                     String description, String mediaType, OffsetDateTime created, OffsetDateTime modified,
                     List<String> labels, boolean external, boolean folder) {
    this(id, scheme, host, port, path, null, title, null, docid, description, mediaType,
        folder && !external ? "folder" : null, null, created, modified, 0L, labels, external,
        GroupFolderSharing.UNKNOWN);
  }

  /**
   * Creates resource URI metadata with an immutable copy of labels.
   *
   * @param id          the resource URI ID
   * @param scheme      the URI scheme
   * @param host        the URI host
   * @param port        the URI port, or {@code -1} if unspecified
   * @param path        the URI path
   * @param decodedPath the decoded URI path
   * @param title       the resource title
   * @param displayTitle the resource display title
   * @param docid       the document identifier
   * @param description the resource description
   * @param mediaType   the resource media type
   * @param documentType the internal document type
   * @param urlType     the external URL type
   * @param created     the creation timestamp
   * @param modified    the last modification timestamp
   * @param size        the resource size in bytes
   * @param labels      the resource labels
   * @param external    whether the URI is external
   * @param sharing     the folder sharing status
   */
  public ResourceUri {
    labels = labels == null ? List.of() : List.copyOf(labels);
    sharing = sharing == null ? GroupFolderSharing.UNKNOWN : sharing;
  }

  /**
   * Returns whether the URI represents a folder.
   *
   * @return {@code true} when the media type or document type is {@code folder}
   */
  public boolean folder() {
    return "folder".equals(this.mediaType) || "folder".equals(this.documentType);
  }
}
