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
    criticalLineCount: 5
});

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
        id: 0, // custom must have id=0, cause this will enable the textarea
        name: 'Other'
    },
    {
        id: 1,
        name: 'Offensive Language'
    },
    {
        id: 2,
        name: 'Personal abuse'
    },
    {
        id: 3,
        name: 'Off topic'
    },
    {
        id: 4,
        name: 'Legal issue'
    },
    {
        id: 5,
        name: 'Trolling'
    },
    {
        id: 6,
        name: 'Hate speech'
    },
    {
        id: 7,
        name: 'Offensive/Threatening language'
    },
    {
        id: 8,
        name: 'Copyright'
    },
    {
        id: 9,
        name: 'Spam'
    }
]);
