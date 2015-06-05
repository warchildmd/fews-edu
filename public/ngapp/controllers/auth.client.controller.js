(function () {
    'use strict';

    angular
        .module('fews')
        .controller('AuthController', AuthController);

    AuthController.$inject = ['$rootScope', 'Authentication'];

    function AuthController($rootScope, Authentication) {
        var vm = this;
        vm.credentials = {};
        vm.login = login;

        /////////////////////////////////////////////////////////

        function login(credentials) {
            Authentication.login(credentials).then(function (user) {
                $rootScope.$broadcast('EVENT_SIGN_IN_SUCCESS');
            }, function () {
                $rootScope.$broadcast('EVENT_SIGN_IN_FAILED');
            });
        }

        function register(credentials) {
            Authentication.register(credentials).then(function (user) {
                $rootScope.$broadcast('EVENT_SIGN_IN_SUCCESS');
            }, function () {
                $rootScope.$broadcast('EVENT_SIGN_UP_FAILED');
            });
        }
    }
})();