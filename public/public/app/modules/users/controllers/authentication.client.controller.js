'use strict';

angular.module('users').controller('AuthenticationController', ['$scope', '$http', '$location', 'Authentication',
    '$cookieStore',
    function ($scope, $http, $location, Authentication, $cookieStore) {
        $scope.authentication = Authentication;

        // If user is signed in then redirect back home
        if ($scope.authentication.user) $location.path('/');

        $scope.getCookie = function (name) {
            var value = "; " + document.cookie;
            var parts = value.split("; " + name + "=");
            if (parts.length == 2) return parts.pop().split(";").shift();
        }

        $scope.signup = function () {
            $http.post('/auth/signup', $scope.credentials).success(function (response) {
                // If successful we assign the response to the global user model
                $scope.authentication.user = response;
                $scope.authentication.token = $scope.getCookie('sent_token');
                $cookieStore.put('token', $scope.authentication.token);
                $cookieStore.put('user', $scope.authentication.user);
                // And redirect to the index page
                $location.path('/');
            }).error(function (response) {
                $scope.error = response.message;
            });
        };

        $scope.signin = function () {
            $http.post('/signin', $scope.credentials).success(function (response) {
                // If successful we assign the response to the global user model
                $scope.authentication.user = response;
                $scope.authentication.token = $scope.getCookie('sent_token');
                $cookieStore.put('token', $scope.authentication.token);
                $cookieStore.put('user', $scope.authentication.user);
                // And redirect to the index page
                $location.path('/');
            }).error(function (response) {
                $scope.error = response.message;
            });
        };
    }
]);