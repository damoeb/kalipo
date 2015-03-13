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

kalipoApp.constant('REPORT_IDS', [
    {
        id: 0, // custom must have id=0, cause this will enable the textarea
        name: 'Custom'
    },
    {
        id: 1,
        name: 'Spam / Advertisment'
    },
    {
        id: 2,
        name: 'Offensive Language'
    }
]);
