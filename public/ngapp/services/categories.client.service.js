(function() {

	'use strict';

	angular
		.module('fews')
		.factory('Categories', Categories);

	Categories.$inject = ['$http'];

	function Categories($http) {
		var categories = {
			getCategories: getCategories
		};
		return categories;

		function getCategories() {
			return $http.get('/api/categories')
				.then(getCategoriesComplete)
				.catch(getCategoriesFailed);

			function getCategoriesComplete(response) {
				return response.data;
			}

			function getCategoriesFailed(error) {
				console.log('XHR Failed for getCategories.' + error.data);
			}
		}

	}

})();
