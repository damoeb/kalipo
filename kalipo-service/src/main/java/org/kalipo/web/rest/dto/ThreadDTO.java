package org.kalipo.web.rest.dto;

import org.joda.time.DateTime;
import org.kalipo.domain.Thread;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A ThreadDTO.
 */

public class ThreadDTO implements Serializable {

    private String id;

    private String uri;

    @NotNull
    private String title;

    private DateTime createdDate;

    private DateTime lastModifiedDate;

    private Integer commentCount;

    private String authorId;

    private Integer likes;

    private Integer dislikes;

    private Boolean readOnly;

    private Thread.Status status;

    public ThreadDTO() {
    }

    public ThreadDTO(Thread thread) {
        BeanUtils.copyProperties(thread, this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Thread.Status getStatus() {
        return status;
    }

    public void setStatus(Thread.Status status) {
        this.status = status;
    }

}
