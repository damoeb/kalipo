package org.kalipo.web.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateDeserializer;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiParam;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.kalipo.domain.Comment;
import org.kalipo.domain.util.CustomLocalDateSerializer;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Optional;

/**
 * A CommentDTO.
 */
@ApiModel(value = "A Comment")
public class CommentDTO implements Serializable {

    private String id;

    @NotNull
    @ApiModelProperty(required = true)
    private Long threadId;

    private Long parentId;

    private Integer level;

    private Integer reputation = 0;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = CustomLocalDateSerializer.class)
    private DateTime createdDate;

    @Size(min = 1, max = Comment.LEN_TEXT)
    @ApiModelProperty(required = true)
    private String text;

    @Size(min = 1, max = Comment.LEN_TITLE)
    @ApiModelProperty(required = true)
    private String title;

    private String authorId;

    private Integer likes = 0;

    private Integer dislikes = 0;

    private Boolean deleted;

    private Comment.Status status;

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

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Comment.Status getStatus() {
        return status;
    }

    public void setStatus(Comment.Status status) {
        this.status = status;
    }

    public static Comment convert(CommentDTO dto) {
        if(dto == null) {
            return null;
        } else {

            Comment c = new Comment();
            BeanUtils.copyProperties(dto, c);

            return c;
        }
    }
}
