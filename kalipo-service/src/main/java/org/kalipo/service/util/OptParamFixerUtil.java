package org.kalipo.service.util;

/**
 * Created by damoeb on 05.12.14.
 */
public final class OptParamFixerUtil {

    public static Integer fixPage(Integer page) {
        final Integer MAX = 20;
        final Integer DEF = 0;

        if (page == null || page < 0) {
            page = DEF;
        }
        if (page > MAX) {
            page = MAX;
        }
        return page;
    }
}
