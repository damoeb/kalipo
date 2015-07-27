package org.kalipo.config;

/**
 * Application constants.
 */
public final class Constants {

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_CREATED_DATE = "createdDate";
    public static final String PARAM_SORT_FIELD = "sortField";
    public static final String PARAM_SORT_ORDER = "sortOrder";
    public static final int LIM_MAX_LEN_BODY = 8184;
    public static final int LIM_MAX_LEN_URL = 400;
    public static final int LIM_MAX_LEN_DISPLAYNAME = 50;
    public static final int LIM_MAX_LEN_USERID = 20;

    private Constants() {
    }

    public static final String SPRING_PROFILE_DEVELOPMENT = "dev";
    public static final String SPRING_PROFILE_PRODUCTION = "prod";
    public static final String SYSTEM_ACCOUNT = "system";

    public static final int PAGE_SIZE = 10;

}
