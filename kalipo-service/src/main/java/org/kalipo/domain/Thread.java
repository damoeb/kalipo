package org.kalipo.domain;

import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
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
     * todo should be a set of urls, the amount of url is defined via Roles
     */
    @Size(min = 1, max = 512)
    @URL
    private String uriHook;

    @NotNull
    @Size(min = 10, max = 64)
    private String title;

    @CreatedDate
    private DateTime createdDate;

    @LastModifiedDate
    private DateTime lastModifiedDate;

    private Integer commentCount = 0;

    /**
     * Number of distinct authors in the discussion
     */
    private Integer authorCount = 0;
    private Integer views = 0;

    @NotNull
    private Set<String> modIds = new HashSet<String>();

    private String leadCommentId;

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

    /**
     * used to transfer text for the mandatory lead comment
     */
    @Transient
    private String text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUriHook() {
        return uriHook;
    }

    public void setUriHook(String uriHook) {
        this.uriHook = uriHook;
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

    public Set<String> getModIds() {
        return modIds;
    }

    public void setModIds(Set<String> modIds) {
        this.modIds = modIds;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLeadCommentId() {
        return leadCommentId;
    }

    public void setLeadCommentId(String leadCommentId) {
        this.leadCommentId = leadCommentId;
    }

    /**
     * Created by damoeb on 7/28/14.
     */
    public static enum Status {
        OPEN, CLOSED
    }
}
