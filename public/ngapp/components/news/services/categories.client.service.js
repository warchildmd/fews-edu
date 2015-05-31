'use strict';

//Articles service used for communicating with the articles REST endpoints
angular.module('core').factory('Categories', ['$resource',
	function($resource) {
		return $resource('/api/categories/:categoryId', {
			articleId: '@_id'
		}, {
            query: {
                method:'GET', isArray:false
            }
		});
	}
]);