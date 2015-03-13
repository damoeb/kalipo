package org.kalipo.domain;

import org.joda.time.DateTime;
import org.kalipo.validation.ModelExistsConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A Report.
 */

@Document(collection = "T_REPORT")
public class Report implements Serializable {

    @Id
    private String id;

    @NotNull(message = "{constraint.notnull.authorId}")
    @Field("author_id")
    private String authorId;

    @NotNull(message = "{constraint.notnull.reportReason}")
    private Integer reasonId;

    @Size(min = 1, max = 148)
    private String customReason;

    private Status status;

    @NotNull(message = "{constraint.notnull.commentId}")
    @Field("comment_id")
    @ModelExistsConstraint(Comment.class)
    private String commentId;

    private boolean abused;

    @NotNull(message = "{constraint.notnull.threadId}")
    @Field("thread_id")
    private String threadId;

    @CreatedDate
    private DateTime createdDate;

    @LastModifiedDate
    private DateTime lastModifiedDate;

    @ModelExistsConstraint(User.class)
    private String reviewerId;

    private String reviewNote;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Integer getReasonId() {
        return reasonId;
    }

    public void setReasonId(Integer reasonId) {
        this.reasonId = reasonId;
    }

    public String getCustomReason() {
        return customReason;
    }

    public void setCustomReason(String customReason) {
        this.customReason = customReason;
    }

    public Status getStatus() {
        return status;
    }

    public Report setStatus(Status status) {
        this.status = status;
        return this;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public boolean isAbused() {
        return abused;
    }

    public void setAbused(boolean abused) {
        this.abused = abused;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(DateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    /**
     * Created by damoeb on 8/8/14.
     */
    public static enum Status {
        PENDING, APPROVED, REJECTED
    }
}
