'use strict';

kalipoApp.factory('Notice', function ($resource, $rootScope) {
    return $resource('app/rest/notices/:id/:opt2', {}, {
            // todo fix retrieve username from $rootScope
        'query': { method: 'GET', isArray: true, params: {id: '@id', opt2: '@opt2'}},
            'get': { method: 'GET'}
        });
    });
