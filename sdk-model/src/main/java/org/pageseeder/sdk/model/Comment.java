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
    return values == null ? List.of() : List.copyOf(values);
  }

  /**
   * Returns the comment ID.
   *
   * @return the comment ID
   */
  public long getId() {
    return this.id;
  }

  /**
   * Returns the discussion ID.
   *
   * @return the discussion ID
   */
  public long getDiscussionId() {
    return this.discussionId;
  }

  /**
   * Returns the content role.
   *
   * @return the content role
   */
  public String getContentRole() {
    return this.contentRole;
  }

  /**
   * Returns the comment type.
   *
   * @return the comment type
   */
  public String getType() {
    return this.type;
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
   * Returns the comment title.
   *
   * @return the comment title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Returns the comment author.
   *
   * @return the comment author
   */
  public CommentUser getAuthor() {
    return this.author;
  }

  /**
   * Returns the user who last modified the comment.
   *
   * @return the modifier details
   */
  public StampedCommentUser getModifiedBy() {
    return this.modifiedBy;
  }

  /**
   * Returns the user assigned to the comment.
   *
   * @return the assignee details
   */
  public StampedCommentUser getAssignedTo() {
    return this.assignedTo;
  }

  /**
   * Returns the comment status.
   *
   * @return the comment status
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * Returns the comment priority.
   *
   * @return the comment priority
   */
  public String getPriority() {
    return this.priority;
  }

  /**
   * Returns the due timestamp.
   *
   * @return the due timestamp
   */
  public OffsetDateTime getDue() {
    return this.due;
  }

  /**
   * Returns the comment content blocks.
   *
   * @return the comment content blocks
   */
  public List<Content> getContent() {
    return this.content;
  }

  /**
   * Returns the comment context.
   *
   * @return the comment context
   */
  public CommentContext getContext() {
    return this.context;
  }

  /**
   * Returns the comment attachments.
   *
   * @return the comment attachments
   */
  public List<ResourceUri> getAttachments() {
    return this.attachments;
  }
}
