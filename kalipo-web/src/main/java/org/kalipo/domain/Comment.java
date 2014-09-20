package org.kalipo.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateDeserializer;
import org.joda.time.DateTime;
import org.kalipo.domain.util.CustomLocalDateSerializer;
import org.joda.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A Comment.
 */

@Document(collection = "T_COMMENT")
public class Comment implements Serializable {

    public static final int LEN_TITLE = 256;
    public static final int LEN_TEXT = 2048;

    @Id
    private String id;

    @NotNull
    private Long threadId;

    private Long parentId;

    private Integer level;

    @NotNull
    private Integer reputation = 0;

//    @NotNull
    @Field("created_date")
    private DateTime createdDate = DateTime.now();

    @NotNull
    @Size(min = 1, max = LEN_TEXT)
    private String text;

    @NotNull
    @Size(min = 1, max = LEN_TITLE)
    private String title;

    @NotNull
    private String authorId;

    private Integer likes = 0;

    private Integer dislikes = 0;

    private Boolean deleted;

    @NotNull
    private Status status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
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
