package org.kalipo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Thread.
 */

@Document(collection = "T_THREAD")
public class Thread implements Serializable {

    @Id
    private String id;

    /**
     * todo the amount of url is defined via Roles
     */
    private Set<String> uriHooks = new HashSet<>();

    @NotNull(message = "{constraint.notnull.title}")
    @Size(min = 10, max = 64)
    private String title;

    @CreatedDate
    private DateTime createdDate;

    @JsonIgnore
    @LastModifiedDate
    private DateTime lastModifiedDate;

    private Integer commentCount = 0;
    private Integer pendingCount = 0;
    private Integer reportedCount = 0;

    @JsonIgnore
    @NotNull(message = "{constraint.notnull.initiatorId}")
    private String initiatorId;

    @NotNull
    private String displayName;

    /**
     * Bans on username level
     * Similar to IRC K-line see https://en.wikipedia.org/wiki/IRCd#K-line
     */
    private Set<String> bans = new HashSet<String>();

    /**
     * todo should be done on page level
     * Bans on IP range level
     * Similar to IRC Z-line see https://en.wikipedia.org/wiki/IRCd#Z-line
     */
//    private Set<String> zLine = new HashSet<String>();

    /**
     * Threads must reach 5 or more authors within 48h to avoid deletion (this can be seen as a garbage collection)
     */
    private DateTime uglyDucklingSurvivalEndDate;

    /**
     * Sum of all comment likes in discussion
     */
    private Integer likes = 0;

    /**
     * Sum of all comment dislikes in discussion
     */
    private Integer dislikes = 0;

    /**
     * Disable comments
     */
    @Field("read_only")
    private Boolean readOnly = false;

    private Status status;

    @NotNull
    private String body;

    private String bodyHtml;

    private String link;

    @NotNull
    private String siteId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Set<String> getUriHooks() {
        return uriHooks;
    }

    public void setUriHooks(Set<String> uriHooks) {
        this.uriHooks = uriHooks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
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

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Deprecated
    public Set<String> getBans() {
        return bans;
    }

    @Deprecated
    public void setBans(Set<String> bans) {
        this.bans = bans;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public DateTime getUglyDucklingSurvivalEndDate() {
        return uglyDucklingSurvivalEndDate;
    }

    public void setUglyDucklingSurvivalEndDate(DateTime uglyDucklingSurvivalEndDate) {
        this.uglyDucklingSurvivalEndDate = uglyDucklingSurvivalEndDate;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Integer getReportedCount() {
        return reportedCount;
    }

    public void setReportedCount(Integer reportedCount) {
        this.reportedCount = reportedCount;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    /**
     * Created by damoeb on 7/28/14.
     */
    public enum Status {
        OPEN, CLOSED
    }
}
