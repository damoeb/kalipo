package org.kalipo.security;

/**
 * Privileges a user can obtain, depending on the users reputation
 * <p>
 * Created by damoeb on 16.09.14.
 */
public class Privileges {

    /**
     * Review all pending comments in this thread
     */
    public static final String CREATE_THREAD = "CREATE_THREAD";
    // todo the price charged for creating a comment may differ from thread to thread
    public static final String CREATE_COMMENT_SOLO = "CREATE_COMMENT_SOLO";
    public static final String CREATE_COMMENT_REPLY = "CREATE_COMMENT_REPLY";
    public static final String EDIT_COMMENT = "EDIT_COMMENT";

    /**
     * Review all pending comments
     */
    public static final String REVIEW_COMMENT = "REVIEW_COMMENT";

    public static final String VOTE_UP = "VOTE_UP";
    public static final String VOTE_DOWN = "VOTE_DOWN";

    public static final String CREATE_PRIVILEGE = "CREATE_PRIVILEGE";
    public static final String CREATE_REPORT = "CREATE_REPORT";
    public static final String CLOSE_REPORT = "CLOSE_REPORT";
    public static final String HOOK_THREAD_TO_URL = "HOOK_THREAD_TO_URL";
    public static final String BAN_USER = "BAN_USER";
}
