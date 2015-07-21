'use strict';

/* Constants */

kalipoApp.constant('USER_ROLES', {
        all: '*',
        admin: 'ROLE_ADMIN',
        user: 'ROLE_USER'
    });

/*
Languages codes are ISO_639-1 codes, see http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
They are written in English to avoid character encoding issues (not a perfect solution)
*/
kalipoApp.constant('LANGUAGES', {
        ca: 'Catalan',
        da: 'Danish',
        en: 'English',
        es: 'Spanish',
        fr: 'French',
        de: 'German',
        kr: 'Korean',
        pl: 'Polish',
        pt: 'Portuguese',
        ru: 'Russian',
        tr: 'Turkish'
    });

kalipoApp.constant('COMMENT_SETTINGS', {
    lineHeight: 20,
    criticalLineCount: 15
});


/*
 3 rule sets
 .) huge discussions > 200 comments attributes.totalElementCount
 - only level 0 comments
 - level 1: show best 2, rest is hidden
 - drop bad comments

 .) normal discussions
 - hide index > 4
 - level 0 are not hiddenreplies

 .) general rules
 - minimize bad comments
 - show full comment if user is the author
 - show path to authors comment at least onelined

 -----

 vollstandig
 einzeilig
 Antworten anzeigen

 comment has replies
 reply.$obligatory = yes|no
 reply.$onelined = yes|no

 a reply can be optional -> show 4 comments

 */
kalipoApp.constant('DISCUSSION_SHAPE_RULES', {

    _isObligatory: function (comment, level, index, properties) {
//      console.log('level', level, 'index', index);
        if (level == 0) {
            return true;
        }
        if (level == 1) {
            return index < 5;
        }
        //return false;
        if (level > 3) {
            return false;
        }
        return properties.totalElementCount < 600;
    },

    _isOneLine: function (comment, level, properties) {
        var controversial = comment.likes > 2 && comment.dislikes > 2;
        var downVoted = comment.likes > 1 && comment.likes > 1 && (comment.likes - comment.dislikes) < 2;
        return downVoted && !controversial;
    },

    apply: function (comment, level, index, properties) {
        comment.$oneline = this._isOneLine(comment, level, index, properties);
        comment.$obligatory = this._isObligatory(comment, level, index, properties);
    }
});

kalipoApp.constant('COMMENT_STATUS', {
    APPROVED:'APPROVED', PENDING:'PENDING', SPAM:'SPAM', REJECTED:'REJECTED', DELETED:'DELETED'
});

kalipoApp.constant('THREAD_STATUS', ['OPEN','LOCKED','CLOSED'
]);

kalipoApp.constant('REPORT_STATUS', {
    APPROVED:'APPROVED', PENDING:'PENDING', REJECTED:'REJECTED'
});

kalipoApp.constant('DISCUSSION_TYPES', [
    {id:'NORMAL', name:'Normal'},
    {id:'LIVE', name:'Live Stream'}
]);

kalipoApp.constant('ACHIEVEMENTS', {
    LIKED: {text: 'Cash -- You like a comment'},
    LIKE: {text: 'Someone likes your comment'},
    DISLIKED: {text: 'Cash -- You dislike a comment'},
    DISLIKE: {text: 'Someone dislikes your comment'},
    RM_COMMENT: {text: 'You removed your comment'},
    REPORTED: {text: 'Cash -- You reported a comment'},
    REPORT: {text: 'Someone reported youre comment'},
    ABUSED_REPORT: {text: 'You abused reporting'}
});

kalipoApp.constant('REPORT_IDS', [
    {
        id: 'Other',
        name: 'Other'
    },
    {
        id: 'Offensive_Language',
        name: 'Offensive Language'
    },
    {
        id: 'Personal_Abuse',
        name: 'Personal abuse'
    },
    {
        id: 'Off_Topic',
        name: 'Off topic'
    },
    {
        id: 'Legal_Issue',
        name: 'Legal issue'
    },
    {
        id: 'Trolling',
        name: 'Trolling'
    },
    {
        id: 'Hate_Speech',
        name: 'Hate speech'
    },
    {
        id: 'Offensive_Threatening_Language',
        name: 'Offensive/Threatening language'
    },
    {
        id: 'Copyright',
        name: 'Copyright'
    },
    {
        id: 'Spam',
        name: 'Spam'
    }
]);
