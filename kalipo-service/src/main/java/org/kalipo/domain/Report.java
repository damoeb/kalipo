package org.kalipo.domain;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
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

    @NotNull
    @Field("author_id")
    private String authorId;

    @NotNull
    @Size(min = 1, max = 512)
    private String reason;

    private Status status;

    @NotNull
    @Field("comment_id")
//    todo fix test to support constraint
//    @ModelExistsConstraint(Comment.class)
    private String commentId;

    private boolean abused;

    @NotNull
    @Field("thread_id")
    private Long threadId;

    private DateTime createdDate;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Created by damoeb on 8/8/14.
     */
    public static enum Status {
        PENDING, APPROVED, REJECTED
    }
}
