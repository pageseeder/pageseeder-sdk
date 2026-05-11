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

  public Comment {
    content = copyOf(content);
    attachments = copyOf(attachments);
  }

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

  public long getId() {
    return this.id;
  }

  public long getDiscussionId() {
    return this.discussionId;
  }

  public String getContentRole() {
    return this.contentRole;
  }

  public String getType() {
    return this.type;
  }

  public OffsetDateTime getCreated() {
    return this.created;
  }

  public String getTitle() {
    return this.title;
  }

  public CommentUser getAuthor() {
    return this.author;
  }

  public StampedCommentUser getModifiedBy() {
    return this.modifiedBy;
  }

  public StampedCommentUser getAssignedTo() {
    return this.assignedTo;
  }

  public String getStatus() {
    return this.status;
  }

  public String getPriority() {
    return this.priority;
  }

  public OffsetDateTime getDue() {
    return this.due;
  }

  public List<Content> getContent() {
    return this.content;
  }

  public CommentContext getContext() {
    return this.context;
  }

  public List<ResourceUri> getAttachments() {
    return this.attachments;
  }
}
