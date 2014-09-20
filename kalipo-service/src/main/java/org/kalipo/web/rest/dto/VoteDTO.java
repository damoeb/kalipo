package org.kalipo.web.rest.dto;

import org.joda.time.DateTime;
import org.kalipo.domain.Vote;

import javax.validation.constraints.NotNull;

/**
 * A VoteDTO.
 */

public class VoteDTO extends BaseDTO<VoteDTO, Vote> {

    private String id;

    private String authorId;

    @NotNull
    private Long commentId;

    @NotNull
    private Boolean isLike;

    private DateTime createdDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getIsLike() {
        return isLike;
    }

    public void setIsLike(Boolean isLike) {
        this.isLike = isLike;
    }
}
