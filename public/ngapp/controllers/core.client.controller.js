(function() {

	angular
		.module('fews')
		.controller('CoreController', CoreController);

	CoreController.$inject = ['$scope', '$mdSidenav', 'Authentication', 'Categories'];

	function CoreController($scope, $mdSidenav, Authentication, Categories) {
        var self = this;

        self.categories = [];
        self.toggleSidenav = toggleSidenav;

        activate();

        function activate() {
            return getCategories().then(function() {
                console.log('Categories retrieved!');
            });
        }

        function toggleSidenav() {
            $mdSidenav('left').toggle();
        }

        function getCategories() {
            return Categories.getCategories()
                .then(function(data) {
                    self.categories = data;
                    return self.categories;
                });
        }

	}

})();