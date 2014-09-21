package org.kalipo.domain;

import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * A BaseDTO.
 */
// todo replace by aspect
public abstract class Convertible<FROM, TO> implements Serializable {

    public TO from(FROM document) {
        BeanUtils.copyProperties(document, this);
        return (TO) this;
    }
}
