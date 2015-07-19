package org.kalipo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;
import org.kalipo.config.Constants;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Thread.
 */

@Document(collection = "T_THREAD")
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonIgnore
    @NotNull(message = "{constraint.notnull.initiatorId}")
    private String initiatorId;

    @NotNull
    private String displayName;

    /**
     * Sum of all comment likes in discussion
     */
    private Integer likes = 0;

    /**
     * Sum of all comment dislikes in discussion
     */
    private Integer dislikes = 0;

    /**
     * minimal user-reputation required to participate the discussion
     * todo impl
     */
    private Integer reputationChallenge = 0;

    /**
     * hide thread from listings/search
     */
    private Boolean hidden;

    /**
     * score represents the current hotness
     */
    private double score;

    private Status status;

    private Set<String> tags;

    @NotNull(message = "{constraint.notnull.body}")
    @Size(min = 2, max = Constants.LIM_MAX_LEN_TEXT, message = "{constraint.length.body}")
    private String body;

    private String bodyHtml;

    @Size(max = Constants.LIM_MAX_LEN_URL, message = "{constraint.length.link}")
    private String link;

    private String domain;

    @NotNull
    private String siteId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
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

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public Integer getReputationChallenge() {
        return reputationChallenge;
    }

    public void setReputationChallenge(Integer reputationChallenge) {
        this.reputationChallenge = reputationChallenge;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * Created by damoeb on 7/28/14.
     */
    public enum Status {
        OPEN, LOCKED, CLOSED
    }
}
