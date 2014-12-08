package org.kalipo.domain;

import org.joda.time.DateTime;
import org.kalipo.validation.ModelExistsConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * A Achievement.
 */

@Document(collection = "T_ACHIEVEMENT")
public class Achievement {

    @Id
    private String id;

    @NotNull(message = "{constraint.notnull.userId}")
    @ModelExistsConstraint(User.class)
    private String userId;

    @CreatedDate
    private DateTime createdDate;

    @NotNull(message = "{constraint.notnull.type}")
    private Reputation.Type type;

    private String resourceRef;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public Achievement setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Reputation.Type getType() {
        return type;
    }

    public Achievement setType(Reputation.Type type) {
        this.type = type;
        return this;
    }

    public String getResourceRef() {
        return resourceRef;
    }

    public Achievement setResourceRef(String resourceRef) {
        this.resourceRef = resourceRef;
        return this;
    }

}
