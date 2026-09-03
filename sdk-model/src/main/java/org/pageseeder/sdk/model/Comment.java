package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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
 * @param task         the task attributes (status, priority, due, assignedTo)
 * @param content      the comment content blocks
 * @param context      the comment context
 * @param attachments  the comment attachments
 * @param labels       the comment labels
 * @param properties   the comment properties
 */
public record Comment(long id, long discussionId, @Nullable String contentRole, @Nullable String type,
                      @Nullable OffsetDateTime created, @Nullable String title, @Nullable CommentUser author,
                      @Nullable StampedCommentUser modifiedBy, @Nullable CommentTask task, List<Content> content,
                      @Nullable CommentContext context, List<ResourceUri> attachments, List<String> labels,
                      Map<String, String> properties) {

  /**
   * Creates a comment with immutable content, attachment, label lists and properties map.
   *
   * @param id           the comment ID
   * @param discussionId the discussion ID
   * @param contentRole  the content role
   * @param type         the comment type
   * @param created      the creation timestamp
   * @param title        the comment title
   * @param author       the comment author
   * @param modifiedBy   the user who last modified the comment
   * @param task         the task attributes (status, priority, due, assignedTo)
   * @param content      the comment content blocks
   * @param context      the comment context
   * @param attachments  the comment attachments
   * @param labels       the comment labels
   * @param properties   the comment properties
   */
  public Comment {
    content = copyOf(content);
    attachments = copyOf(attachments);
    labels = copyOf(labels);
    properties = properties == null ? Map.of() : Map.copyOf(properties);
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
   * @param task         the task attributes (status, priority, due, assignedTo)
   * @param content      the comment content blocks
   * @param context      the comment context
   * @param attachments  the comment attachments
   * @param labels       the comment labels
   * @param properties   the comment properties
   * @return a comment instance
   */
  public static Comment fromParsed(long id, long discussionId, @Nullable String contentRole, @Nullable String type,
                                   @Nullable OffsetDateTime created, @Nullable String title,
                                   @Nullable CommentUser author, @Nullable StampedCommentUser modifiedBy,
                                   @Nullable CommentTask task, List<Content> content, @Nullable CommentContext context,
                                   List<ResourceUri> attachments, List<String> labels, Map<String, String> properties) {
    return new Comment(id, discussionId, contentRole, type, created, title, author, modifiedBy, task, content,
        context, attachments, labels, properties);
  }

  /**
   * @return the task status, or {@code null} if this comment has no task attributes
   * @deprecated use {@link #task()} and {@link CommentTask#status()} instead
   */
  @Deprecated
  public @Nullable String status() {
    return this.task == null ? null : this.task.status();
  }

  /**
   * @return the task priority, or {@code null} if this comment has no task attributes
   * @deprecated use {@link #task()} and {@link CommentTask#priority()} instead
   */
  @Deprecated
  public @Nullable String priority() {
    return this.task == null ? null : this.task.priority();
  }

  /**
   * @return the task due timestamp, or {@code null} if this comment has no task attributes
   * @deprecated use {@link #task()} and {@link CommentTask#due()} instead
   */
  @Deprecated
  public @Nullable OffsetDateTime due() {
    return this.task == null ? null : this.task.due();
  }

  /**
   * @return the user assigned to the task, or {@code null} if this comment has no task attributes
   * @deprecated use {@link #task()} and {@link CommentTask#assignedTo()} instead
   */
  @Deprecated
  public @Nullable StampedCommentUser assignedTo() {
    return this.task == null ? null : this.task.assignedTo();
  }

  private static <T> List<T> copyOf(List<T> values) {
    //noinspection ConstantValue (Defensive check)
    return values == null ? List.of() : List.copyOf(values);
  }
}
