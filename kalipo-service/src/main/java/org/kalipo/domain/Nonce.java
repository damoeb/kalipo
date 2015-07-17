package org.kalipo.domain;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * A Nonce.
 * Created by damoeb on 17.07.15.
 */
@Document(collection = "T_NONCE")
public class Nonce {

    @Id
    private String phrase;

    @NotNull
    private DateTime validUntil;

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public DateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(DateTime validUntil) {
        this.validUntil = validUntil;
    }
}
