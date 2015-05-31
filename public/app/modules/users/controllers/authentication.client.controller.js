'use strict';

angular.module('users').controller('AuthenticationController', ['$scope', '$http', '$location', 'Authentication',
    '$cookieStore', '$window',
    function ($scope, $http, $location, Authentication, $cookieStore, $window) {
        $scope.authentication = Authentication;

        // If user is signed in then redirect back home
        if ($scope.authentication.user) $location.path('/');

        $scope.getCookie = function (name) {
            var value = "; " + document.cookie;
            var parts = value.split("; " + name + "=");
            if (parts.length == 2) return parts.pop().split(";").shift();
        };

        $scope.signup = function () {
            $http.post('/api/auth/signup', $scope.credentials).success(function (response) {
                // If successful we assign the response to the global user model
                $scope.authentication.user = response.user;
                $scope.authentication.token = response.token;
                $cookieStore.put('token', $scope.authentication.token);
                $cookieStore.put('user', $scope.authentication.user);
                // And redirect to the index page
                // $location.path('/');
                $window.location.reload();
            }).error(function (response) {
                $scope.error = response.message;
            });
        };

        $scope.signin = function () {
            $http.post('/api/auth/signin', $scope.credentials).success(function (response) {
                // If successful we assign the response to the global user model
                $scope.authentication.user = response.user;
                $scope.authentication.token = response.token;
                $cookieStore.put('token', $scope.authentication.token);
                $cookieStore.put('user', $scope.authentication.user);
                // And redirect to the index page
                // $location.path('/');
                $window.location.reload();
            }).error(function (response) {
                $scope.error = response.message;
            });
        };
    }
]);