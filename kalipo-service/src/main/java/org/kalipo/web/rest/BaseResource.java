package org.kalipo.web.rest;


import org.springframework.beans.BeanUtils;

/**
 * Created by damoeb on 21.09.14.
 */
public abstract class BaseResource<MODEL, DTO> {

    /**
     * Convert from DTO to Model
     *
     * @param from the DTO
     * @return
     */
    MODEL toOrigin(DTO from) {
        MODEL m = newOriginInstance();
        BeanUtils.copyProperties(from, m);
        return m;
    }

    /**
     * Convert from Model to DTO
     *
     * @param from the Model
     * @return
     */
    DTO fromOrigin(MODEL from) {
        DTO m = newDTOInstance();
        BeanUtils.copyProperties(from, m);
        return m;
    }

    protected abstract DTO newDTOInstance();

    protected abstract MODEL newOriginInstance();
}
