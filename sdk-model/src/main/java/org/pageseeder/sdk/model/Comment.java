package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder comment.
 *
 * @param id           the comment ID
 * @param discussionId the discussion ID
 * @param contentRole  the content role
 * @param type         the comment type
 * @param created      the creation timestamp
 * @param title        the comment title
 * @param author       the comment author
 * @param modifiedBy   the user who last modified the comment
 * @param assignedTo   the user assigned to the comment
 * @param status       the comment status
 * @param priority     the comment priority
 * @param due          the due timestamp
 * @param content      the comment content blocks
 * @param context      the comment context
 * @param attachments  the comment attachments
 */
public record Comment(long id, long discussionId, String contentRole, String type, OffsetDateTime created,
                      String title, CommentUser author, StampedCommentUser modifiedBy,
                      StampedCommentUser assignedTo, String status, String priority, OffsetDateTime due,
                      List<Content> content, CommentContext context, List<ResourceUri> attachments) {

  /**
   * Creates a comment with immutable content and attachment lists.
   *
   * @param id           the comment ID
   * @param discussionId the discussion ID
   * @param contentRole  the content role
   * @param type         the comment type
   * @param created      the creation timestamp
   * @param title        the comment title
   * @param author       the comment author
   * @param modifiedBy   the user who last modified the comment
   * @param assignedTo   the user assigned to the comment
   * @param status       the comment status
   * @param priority     the comment priority
   * @param due          the due timestamp
   * @param content      the comment content blocks
   * @param context      the comment context
   * @param attachments  the comment attachments
   */
  public Comment {
    content = copyOf(content);
    attachments = copyOf(attachments);
  }

  /**
   * Creates a comment from parsed PageSeeder fields.
   *
   * @param id           the comment ID
   * @param discussionId the discussion ID
   * @param contentRole  the content role
   * @param type         the comment type
   * @param created      the creation timestamp
   * @param title        the comment title
   * @param author       the comment author
   * @param modifiedBy   the user who last modified the comment
   * @param assignedTo   the user assigned to the comment
   * @param status       the comment status
   * @param priority     the comment priority
   * @param due          the due timestamp
   * @param content      the comment content blocks
   * @param context      the comment context
   * @param attachments  the comment attachments
   * @return a comment instance
   */
  public static Comment fromParsed(long id, long discussionId, String contentRole, String type, OffsetDateTime created,
                                   String title, CommentUser author, StampedCommentUser modifiedBy,
                                   StampedCommentUser assignedTo, String status, String priority, OffsetDateTime due,
                                   List<Content> content, CommentContext context, List<ResourceUri> attachments) {
    return new Comment(id, discussionId, contentRole, type, created, title, author, modifiedBy, assignedTo, status,
        priority, due, content, context, attachments);
  }

  private static <T> List<T> copyOf(List<T> values) {
    //noinspection ConstantValue (Defensive check)
    return values == null ? List.of() : List.copyOf(values);
  }
}
