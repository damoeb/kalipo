package org.kalipo.domain;

import org.joda.time.DateTime;
import org.kalipo.validation.ModelExistsConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * A RepRevision.
 */

@Document(collection = "T_REP_REVISION")
public class RepRevision {

    @Id
    private String id;

    @NotNull(message = "{constraint.notnull.userId}")
    @ModelExistsConstraint(User.class)
    private String userId;

    @CreatedDate
    private DateTime createdDate;

    @NotNull(message = "{constraint.notnull.type}")
    private ReputationDefinition.Type type;

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

    public RepRevision setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public ReputationDefinition.Type getType() {
        return type;
    }

    public RepRevision setType(ReputationDefinition.Type type) {
        this.type = type;
        return this;
    }

    public String getResourceRef() {
        return resourceRef;
    }

    public RepRevision setResourceRef(String resourceRef) {
        this.resourceRef = resourceRef;
        return this;
    }

}
