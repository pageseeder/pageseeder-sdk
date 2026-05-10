package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder resource URI metadata.
 */
public final class ResourceUri {

  private final long id;
  private final String scheme;
  private final String host;
  private final int port;
  private final String path;
  private final String title;
  private final String docid;
  private final String description;
  private final String mediaType;
  private final OffsetDateTime created;
  private final OffsetDateTime modified;
  private final List<String> labels;
  private final boolean external;
  private final boolean folder;

  public ResourceUri(long id, String scheme, String host, int port, String path, String title, String docid,
                     String description, String mediaType, OffsetDateTime created, OffsetDateTime modified,
                     List<String> labels, boolean external, boolean folder) {
    this.id = id;
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.path = path;
    this.title = title;
    this.docid = docid;
    this.description = description;
    this.mediaType = mediaType;
    this.created = created;
    this.modified = modified;
    this.labels = labels == null ? List.of() : List.copyOf(labels);
    this.external = external;
    this.folder = folder;
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
