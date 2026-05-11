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
public record ResourceUri(long id, String scheme, String host, int port, String path, String title, String docid,
                          String description, String mediaType, OffsetDateTime created, OffsetDateTime modified,
                          List<String> labels, boolean external, boolean folder) {

  /**
   * Creates resource URI metadata with an immutable copy of labels.
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
  public ResourceUri {
    labels = labels == null ? List.of() : List.copyOf(labels);
  }

  /**
   * Returns the resource URI ID.
   *
   * @return the resource URI ID
   */
  public long getId() {
    return this.id;
  }

  /**
   * Returns the URI scheme.
   *
   * @return the URI scheme
   */
  public String getScheme() {
    return this.scheme;
  }

  /**
   * Returns the URI host.
   *
   * @return the URI host
   */
  public String getHost() {
    return this.host;
  }

  /**
   * Returns the URI port.
   *
   * @return the URI port, or {@code -1} if unspecified
   */
  public int getPort() {
    return this.port;
  }

  /**
   * Returns the URI path.
   *
   * @return the URI path
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Returns the resource title.
   *
   * @return the resource title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Returns the document identifier.
   *
   * @return the document identifier
   */
  public String getDocid() {
    return this.docid;
  }

  /**
   * Returns the resource description.
   *
   * @return the resource description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Returns the resource media type.
   *
   * @return the resource media type
   */
  public String getMediaType() {
    return this.mediaType;
  }

  /**
   * Returns the creation timestamp.
   *
   * @return the creation timestamp
   */
  public OffsetDateTime getCreated() {
    return this.created;
  }

  /**
   * Returns the last modification timestamp.
   *
   * @return the last modification timestamp
   */
  public OffsetDateTime getModified() {
    return this.modified;
  }

  /**
   * Returns the resource labels.
   *
   * @return the resource labels
   */
  public List<String> getLabels() {
    return this.labels;
  }

  /**
   * Indicates whether the URI is external.
   *
   * @return {@code true} when the URI is external
   */
  public boolean isExternal() {
    return this.external;
  }

  /**
   * Indicates whether the URI represents a folder.
   *
   * @return {@code true} when the URI represents a folder
   */
  public boolean isFolder() {
    return this.folder;
  }
}
