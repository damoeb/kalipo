package org.kalipo.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A Privilege.
 */

@Document(collection = "T_PRIVILEGE")
public class Privilege implements Serializable {

    @Id
    private String id;

    /**
     *
     */
    @NotNull(message = "{constraint.notnull.name}")
    @Size(min = 1, max = 50)
    private String name;

    /**
     * the minimal reputation required to obtain this privilege
     */
    @NotNull
    private Integer reputation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getReputation() {
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }
}
