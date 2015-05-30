'use strict';

//Articles service used for communicating with the articles REST endpoints
angular.module('core').factory('Articles', ['$resource',
	function($resource) {
		return $resource('/api/articles/:articleId', {
			articleId: '@_id'
		}, {
            query: {
                method:'GET', isArray:false
            },
            popular: {
                method: 'GET',
                isArray: false,
                url: '/api/articles/popular/'
            }
		});
	}
]);