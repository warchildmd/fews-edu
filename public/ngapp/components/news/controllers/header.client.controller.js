angular.module('core').controller('HeaderController', ['$scope', '$sce', '$stateParams', '$location', 'Authentication', 'Categories',
	function($scope, $sce, $stateParams, $location, Authentication, Categories) {
		$scope.authentication = Authentication;

        $scope.today = new Date();

		$scope.initHeader = function() {
			$scope.categories = Categories.get();
		};
	}
]);