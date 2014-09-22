package org.kalipo.domain;

import org.joda.time.DateTime;
import org.kalipo.validation.ModelExistsConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A Comment.
 */

@Document(collection = "T_COMMENT")
public class Comment {

    public static final int LEN_TITLE = 256;
    public static final int LEN_TEXT = 2048;

    @Id
    private String id;

    @NotNull(message = "{constraint.notnull.threadId}")
    @ModelExistsConstraint(Thread.class)
    private String threadId;

    @ModelExistsConstraint(Comment.class)
    private String parentId;

    private Integer level;

    @NotNull(message = "{constraint.notnull.reputation}")
    private Integer reputation = 0;

    @CreatedDate
    private DateTime createdDate;

    @NotNull(message = "{constraint.notnull.text}")
    @Size(min = 1, max = LEN_TEXT, message = "{constraint.length.text}")
    private String text;

    @NotNull(message = "{constraint.notnull.title}")
    @Size(min = 1, max = LEN_TITLE)
    private String title;

    @NotNull(message = "{constraint.notnull.authorId}")
    private String authorId;

    private Integer likes = 0;

    private Integer dislikes = 0;

    private Boolean deleted;

    @NotNull(message = "{constraint.notnull.status}")
    private Status status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getReputation() {
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Created by damoeb on 7/28/14.
     */
    public static enum Status {
        APPROVED, PENDING, REJECTED, SPAM, DELETED
    }
}
