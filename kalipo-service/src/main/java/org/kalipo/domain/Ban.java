package org.kalipo.domain;

import org.joda.time.DateTime;
import org.kalipo.config.Constants;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A Ban of a user.
 */
@Document(collection = "T_BAN")
public class Ban implements Serializable {

    @Id
    private String id;

    /**
     *
     */
    @NotNull
    @Size(min = 0, max = Constants.LIM_MAX_LEN_USERID)
    private String userId;

    @NotNull
    private String siteId;

    @CreatedDate
    private DateTime createdDate;

    @NotNull
    private DateTime validUntil;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public DateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(DateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
