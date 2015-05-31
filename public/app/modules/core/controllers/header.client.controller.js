angular.module('core').controller('HeaderController', ['$scope', '$sce', '$stateParams', '$location', 'Authentication', 'Categories',
	function($scope, $sce, $stateParams, $location, Authentication, Categories) {
		$scope.authentication = Authentication;

        $scope.today = new Date();

		$scope.user = $scope.authentication.user;
		$scope.authenticated = (Authentication.user) ? true : false;

		$scope.initHeader = function() {
			$scope.categories = Categories.get();
		};
	}
]);