package org.kalipo.web.rest.dto;

import org.joda.time.DateTime;
import org.kalipo.domain.*;
import org.kalipo.domain.Thread;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    private Status status;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static Thread convert(ThreadDTO dto) {
        if(dto == null) {
            return null;
        } else {

            Thread c = new Thread();
            BeanUtils.copyProperties(dto, c);

            return c;
        }
    }

    /**
     * Created by damoeb on 7/28/14.
     */
    public static enum Status {
        OPEN, CLOSED
    }
}
