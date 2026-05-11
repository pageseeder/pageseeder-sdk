package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder document URI version.
 *
 * @param id            the version ID
 * @param name          the version name
 * @param created       the creation timestamp
 * @param publicationId the publication identifier this version belongs to
 * @param author        the version author
 * @param descriptions  the version descriptions
 * @param labels        the version labels
 */
public record DocumentVersion(long id, String name, OffsetDateTime created, @Nullable String publicationId,
                              @Nullable CommentUser author, List<String> descriptions, List<String> labels) {

  /**
   * Creates a document version with immutable description and label lists.
   *
   * @param id            the version ID
   * @param name          the version name
   * @param created       the creation timestamp
   * @param publicationId the publication identifier this version belongs to
   * @param author        the version author
   * @param descriptions  the version descriptions
   * @param labels        the version labels
   */
  public DocumentVersion {
    descriptions = descriptions == null ? List.of() : List.copyOf(descriptions);
    labels = labels == null ? List.of() : List.copyOf(labels);
  }
}
