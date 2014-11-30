package org.kalipo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;
import org.kalipo.validation.ModelExistsConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A Comment.
 */
@Document(collection = "T_COMMENT")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {

    public static final int LEN_TEXT = 2048;

    @Id
    private String id;

    @NotNull(message = "{constraint.notnull.threadId}")
    @ModelExistsConstraint(Thread.class)
    private String threadId;

    @ModelExistsConstraint(Comment.class)
    private String parentId;

    @JsonIgnore
    @NotNull(message = "{constraint.notnull.reputation}")
    private Integer reputation = 0;

    // todo rename to createdOn
    @CreatedDate
    private DateTime createdDate;

    @NotNull(message = "{constraint.notnull.text}")
    @Size(min = 2, max = LEN_TEXT, message = "{constraint.length.text}")
    private String text;

    @NotNull(message = "{constraint.notnull.authorId}")
    private String authorId;

    // todo add displayName to support Anonymous posts, make authorId @JsonIgnore
    private String displayName;

    private Integer likes = 0;

    private Integer dislikes = 0;

    private Boolean hidden;

    /**
     * Stay on top of list in ui
     */
//   todo implement in ui
    private Boolean sticky;

    private Integer reportedCount = 0;

    //    @JsonIgnore
    @NotNull(message = "{constraint.notnull.status}")
    private Status status;

    private String reviewerId;

    // Publish post as anonymous
    @Transient
    private Boolean anonymous;

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Integer getReportedCount() {
        return reportedCount;
    }

    public void setReportedCount(Integer reportedCount) {
        this.reportedCount = reportedCount;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getSticky() {
        return sticky;
    }

    public void setSticky(Boolean sticky) {
        this.sticky = sticky;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Created by damoeb on 7/28/14.
     */
    public static enum Status {
        APPROVED, PENDING, SPAM, DELETED
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", threadId='" + threadId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", text='" + text + '\'' +
                ", authorId='" + authorId + '\'' +
                ", status=" + status +
                ", reviewerId='" + reviewerId + '\'' +
                '}';
    }
}
