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

  public ResourceUri {
    labels = labels == null ? List.of() : List.copyOf(labels);
  }

  public long getId() {
    return this.id;
  }

  public String getScheme() {
    return this.scheme;
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  public String getPath() {
    return this.path;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDocid() {
    return this.docid;
  }

  public String getDescription() {
    return this.description;
  }

  public String getMediaType() {
    return this.mediaType;
  }

  public OffsetDateTime getCreated() {
    return this.created;
  }

  public OffsetDateTime getModified() {
    return this.modified;
  }

  public List<String> getLabels() {
    return this.labels;
  }

  public boolean isExternal() {
    return this.external;
  }

  public boolean isFolder() {
    return this.folder;
  }
}
