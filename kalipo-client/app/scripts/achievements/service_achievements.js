'use strict';

kalipoApp.factory('Achievement', function ($resource) {
    return $resource('app/rest/achievements/:id', {}, {
        'latest': {method: 'GET', isArray: true}
    });
});
