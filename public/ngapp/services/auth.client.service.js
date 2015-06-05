(function() {
   'use strict';

    angular
        .module('fews')
        .factory('Authentication', Authentication);

    Authentication.$inject = ['$cookieStore', '$http'];

    function Authentication($cookieStore, $http) {
        var authService = {};

        authService.login = login;
        authService.logout = logout;
        authService.register = register;
        authService.isAuthenticated = isAuthenticated;
        authService.getSession = getSession;
        authService.getUser = getUser;

        return authService;

        function getSession() {
            return $cookieStore.get('session');
        }

        function getUser() {
            return $cookieStore.get('user');
        }

        function login(credentials) {
            return $http
                .post('/api/auth/signin', credentials)
                .then(function (res) {
                    $cookieStore.put('session', res.data.token);
                    $cookieStore.put('user', res.data.user);
                    return res.data.user;
                });
        }

        function logout() {
            $cookieStore.remove('user');
            $cookieStore.remove('session');
        }

        function register(credentials) {
            return $http
                .post('/api/auth/signup', credentials)
                .then(function (res) {
                    $cookieStore.put('session', res.data.token);
                    $cookieStore.put('user', res.data.user);
                    return res.data.user;
                });
        }

        function isAuthenticated() {
            return !!(($cookieStore.get('user') !== undefined) && ($cookieStore.get('session') !== undefined));
        }
    }
})();