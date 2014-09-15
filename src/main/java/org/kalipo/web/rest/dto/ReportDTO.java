package org.kalipo.web.rest.dto;

import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A ReportDTO.
 */

public class ReportDTO implements Serializable {

    private String id;

    private String authorId;

    @NotNull
    private String reason;

    private Status status;

    @NotNull
    private Long commentId;

    private boolean abused;

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

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
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
