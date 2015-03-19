package org.kalipo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;
import org.kalipo.validation.ModelExistsConstraint;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * A Comment.
 */
@Document(collection = "T_COMMENT")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment implements Anonymizable<Comment> {

    public static final int LEN_TEXT = 2048; // todo centralize constant

    @Id
    private String id;

    @NotNull(message = "{constraint.notnull.threadId}")
    @ModelExistsConstraint(Thread.class)
    private String threadId;

    @ModelExistsConstraint(Comment.class)
    private String parentId;

//    @JsonIgnore
//    @NotNull(message = "{constraint.notnull.reputation}")
//    private Integer reputation = 0;

    @CreatedDate
    private DateTime createdDate;

    @JsonIgnore
    @LastModifiedDate
    private DateTime lastModifiedDate;

    @NotNull(message = "{constraint.notnull.body}")
    @Size(min = 2, max = LEN_TEXT, message = "{constraint.length.body}")
    private String body;

    private String bodyHtml;

    @JsonIgnore
    @NotNull(message = "{constraint.notnull.authorId}")
    private String authorId;

    private String displayName;

    // A related hash to order all comments close to their parents
    @JsonIgnore
    private String fingerprint;

    private Integer level;

    // generated - isolated comment quality
    @JsonIgnore
    private Double quality;

    // generated - representing importance in discussion
    private Double influence;

    private Integer likes;

    private Integer repliesCount;

    private Integer dislikes;

    // reported comments may be hidden
    private Boolean hidden;

    private Boolean createdByMod;

    /**
     * Stay on top of list in ui
     */
//   todo implement in ui
    private Boolean sticky;

    private Integer reportedCount;

    @NotNull(message = "{constraint.notnull.status}")
    private Status status;

    private String reviewerId;

    private String reviewMsg;

    @Transient
    private Set<Link> links;

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

    @Override
    public Comment anonymized() {
        Comment a = new Comment();
        BeanUtils.copyProperties(this, a);
        a.setCreatedDate(null);
        a.setAuthorId(null);
        a.setFingerprint(null);
        a.setCreatedByMod(null);
        return a;
    }

    public Boolean getCreatedByMod() {
        return createdByMod;
    }

    public void setCreatedByMod(Boolean createdByMod) {
        this.createdByMod = createdByMod;
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

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getRepliesCount() {
        return repliesCount;
    }

    public void setRepliesCount(Integer repliesCount) {
        this.repliesCount = repliesCount;
    }

    public Double getInfluence() {
        return influence;
    }

    public void setInfluence(Double influence) {
        this.influence = influence;
    }

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(DateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getReviewMsg() {
        return reviewMsg;
    }

    public void setReviewMsg(String reviewMsg) {
        this.reviewMsg = reviewMsg;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    /**
     * Created by damoeb on 7/28/14.
     */
    public static enum Status {
        APPROVED, PENDING, SPAM, REJECTED, DELETED
    }

    public static class Link {
        private String url;
        private int impression;

        public Link(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public int getImpression() {
            return impression;
        }

        public void incrImpression() {
            impression++;
        }
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", threadId='" + threadId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", body='" + body + '\'' +
                ", authorId='" + authorId + '\'' +
                ", status=" + status +
                ", reviewerId='" + reviewerId + '\'' +
                '}';
    }
}
