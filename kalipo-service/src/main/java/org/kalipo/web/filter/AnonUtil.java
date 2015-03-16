package org.kalipo.web.filter;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by markus on 16.03.15.
 */
public final class AnonUtil {
    public static String maskIp(String ip) {
        String[] parts = StringUtils.split(ip, ".");
        parts[parts.length -1] = "0";

        return StringUtils.join(parts, ".");
    }
}
