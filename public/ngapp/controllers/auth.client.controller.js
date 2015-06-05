(function () {
    'use strict';

    angular
        .module('fews')
        .controller('AuthController', AuthController);

    AuthController.$inject = ['$rootScope', '$mdDialog', '$mdToast', 'Authentication'];

    function AuthController($rootScope, $mdDialog, $mdToast, Authentication) {
        var self = this;
        self.credentials = {};
        self.login = login;
        self.register = register;
        self.cancel = cancel;

        function cancel() {
            $mdDialog.cancel();
        }

        function login() {
            Authentication.login(self.credentials).then(function (user) {
                $mdDialog.hide(true);
                // $rootScope.$broadcast('EVENT_SIGN_IN_SUCCESS');
            }, function () {
                $mdToast.show(
                    $mdToast.simple()
                        .content('Autentificare esuata!')
                        .position('top right')
                        .hideDelay(3000)
                );
                // $rootScope.$broadcast('EVENT_SIGN_IN_FAILED');
            });
        }

        function register() {
            Authentication.register(self.credentials).then(function (user) {
                $mdDialog.hide(true);
                // $rootScope.$broadcast('EVENT_SIGN_IN_SUCCESS');
            }, function () {
                $mdToast.show(
                    $mdToast.simple()
                        .content('Inregistrare esuata!')
                        .position('top right')
                        .hideDelay(3000)
                );
                // $rootScope.$broadcast('EVENT_SIGN_UP_FAILED');
            });
        }
    }
})();