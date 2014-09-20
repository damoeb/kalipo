package org.kalipo.web.rest.dto;

import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * A BaseDTO.
 */
public abstract class BaseDTO<DTO, SOURCE> implements Serializable {

    public DTO fields(SOURCE document) {
        BeanUtils.copyProperties(document, this);
        return (DTO) this;
    }
}
