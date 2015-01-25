'use strict';

kalipoApp.factory('Achievement', function ($resource) {
    return $resource('app/rest/achievements/:id', {}, {
        'list': {method: 'GET', isArray: true}
    });
});
