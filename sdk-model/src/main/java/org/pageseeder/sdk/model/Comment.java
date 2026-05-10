package org.pageseeder.sdk.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder comment.
 */
public final class Comment {

  private final long id;
  private final long discussionId;
  private final String contentRole;
  private final String type;
  private final OffsetDateTime created;
  private final String title;
  private final CommentUser author;
  private final StampedCommentUser modifiedBy;
  private final StampedCommentUser assignedTo;
  private final String status;
  private final String priority;
  private final OffsetDateTime due;
  private final List<Content> content;
  private final CommentContext context;
  private final List<ResourceUri> attachments;

  public Comment(long id, long discussionId, String contentRole, String type, OffsetDateTime created, String title,
                 CommentUser author, StampedCommentUser modifiedBy,
                 StampedCommentUser assignedTo, String status, String priority, OffsetDateTime due,
                 List<Content> content, CommentContext context, List<ResourceUri> attachments) {
    this(id, discussionId, contentRole, type, created, title, author, modifiedBy, assignedTo, status, priority, due,
        copyOf(content), context, copyOf(attachments), true);
  }

  private Comment(long id, long discussionId, String contentRole, String type, OffsetDateTime created, String title,
                  CommentUser author, StampedCommentUser modifiedBy,
                  StampedCommentUser assignedTo, String status, String priority, OffsetDateTime due,
                  List<Content> content, CommentContext context, List<ResourceUri> attachments, boolean trusted) {
    this.id = id;
    this.discussionId = discussionId;
    this.contentRole = contentRole;
    this.type = type;
    this.created = created;
    this.title = title;
    this.author = author;
    this.modifiedBy = modifiedBy;
    this.assignedTo = assignedTo;
    this.status = status;
    this.priority = priority;
    this.due = due;
    this.content = content;
    this.context = context;
    this.attachments = attachments;
  }

  public static Comment fromParsed(long id, long discussionId, String contentRole, String type, OffsetDateTime created,
                                   String title, CommentUser author, StampedCommentUser modifiedBy,
                                   StampedCommentUser assignedTo, String status, String priority, OffsetDateTime due,
                                   List<Content> content, CommentContext context, List<ResourceUri> attachments) {
    return new Comment(id, discussionId, contentRole, type, created, title, author, modifiedBy, assignedTo, status,
        priority, due, content, context, attachments, true);
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
