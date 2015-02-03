package org.kalipo.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;
import org.kalipo.validation.ModelExistsConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A Vote.
 */

@Document(collection = "T_VOTE")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Vote implements Serializable {

    @Id
    private String id;

    @NotNull(message = "{constraint.notnull.authorId}")
    private String authorId;

    @NotNull(message = "{constraint.notnull.commentId}")
    @Field("comment_id")
    @ModelExistsConstraint(Comment.class)
    private String commentId;

    private String threadId;

    private boolean like;

    @CreatedDate
    private DateTime createdDate;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

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

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }
}
