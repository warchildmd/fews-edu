(function () {

    angular
        .module('fews')
        .controller('CoreController', CoreController);

    CoreController.$inject = ['$scope', 'Authentication', '$mdSidenav', '$mdDialog', '$window', 'Categories'];

    function CoreController($scope, Authentication, $mdSidenav, $mdDialog, $window, Categories) {
        var self = this;
        self.user = Authentication.getUser();
        self.categories = [];
        self.toggleSidenav = toggleSidenav;

        self.showLoginDialog = showLoginDialog;
        self.showRegisterDialog = showRegisterDialog;

        activate();

        function activate() {
            return getCategories().then(function () {
                console.log('Categories retrieved!');
            });
        }

        $scope.$watch('Authentication.isAuthenticated()', function(current, original) {
            self.user = Authentication.getUser();
        });

        function showLoginDialog(ev) {
            $mdDialog.show({
                controller: 'AuthController',
                controllerAs: 'vm',
                templateUrl: '/assets/ngapp/views/auth.login.view.html',
                targetEvent: ev
            }).then(function (answer) {
                console.log('You said the information was "' + answer + '".');
                if (answer === true) {
                    $window.location.reload();
                }
            }, function () {
                console.log('You cancelled the dialog.');
            });
        }

        function showRegisterDialog(ev) {
            $mdDialog.show({
                controller: 'AuthController',
                controllerAs: 'vm',
                templateUrl: '/assets/ngapp/views/auth.register.view.html',
                targetEvent: ev
            }).then(function (answer) {
                console.log('You said the information was "' + answer + '".');
                if (answer === true) {
                    $window.location.reload();
                }
            }, function () {
                console.log('You cancelled the dialog.');
            });
        }

        function toggleSidenav() {
            $mdSidenav('left').toggle();
        }

        function getCategories() {
            return Categories.getCategories()
                .then(function (data) {
                    self.categories = data;
                    return self.categories;
                });
        }

    }

})();