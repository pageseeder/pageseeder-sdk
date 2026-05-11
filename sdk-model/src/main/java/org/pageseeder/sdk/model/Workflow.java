package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder workflow summary for a URI.
 *
 * @param id            the workflow thread root ID
 * @param status        the latest workflow status
 * @param priority      the latest workflow priority
 * @param due           the latest workflow due date
 * @param statusChanged the date when the latest status started
 * @param assignedTo    the assigned member and assignment date
 * @param uri           the URI this workflow applies to
 * @param comments      the workflow comments, when requested
 */
public record Workflow(long id, @Nullable String status, @Nullable String priority, @Nullable OffsetDateTime due,
                       @Nullable OffsetDateTime statusChanged, @Nullable StampedCommentUser assignedTo,
                       @Nullable ResourceUri uri, List<Comment> comments) {

  /**
   * Creates a workflow with an immutable comments list.
   *
   * @param id            the workflow thread root ID
   * @param status        the latest workflow status
   * @param priority      the latest workflow priority
   * @param due           the latest workflow due date
   * @param statusChanged the date when the latest status started
   * @param assignedTo    the assigned member and assignment date
   * @param uri           the URI this workflow applies to
   * @param comments      the workflow comments, when requested
   */
  public Workflow {
    comments = comments == null ? List.of() : List.copyOf(comments);
  }
}
