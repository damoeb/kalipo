package org.kalipo.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A Tag. This entity conserves stats about a tag
 */
@Document(collection = "T_TAG")
public class Tag implements Serializable {

    @Id
    private String id;

    @NotNull
    @Size(min = 1, max = 50)
    @Field("name")
    private String name;

    /**
     * Global usage of this tag
     */
    private int usage;

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

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }
}
