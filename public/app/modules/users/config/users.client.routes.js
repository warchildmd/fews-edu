'use strict';

// Setting up route
angular.module('users').config(['$stateProvider',
    function ($stateProvider) {
        // Users state routing
        $stateProvider.
            state('profile', {
                url: '/settings/profile',
                templateUrl: '/assets/app/modules/users/views/settings/edit-profile.client.view.html'
            }).
            state('signup', {
                url: '/signup',
                templateUrl: '/assets/app/modules/users/views/authentication/signup.client.view.html'
            }).
            state('signin', {
                url: '/signin',
                templateUrl: '/assets/app/modules/users/views/authentication/signin.client.view.html'
            }).
            state('signout', {
                url: '/signout',
                controller: function ($scope, $http, Authentication) {
                    $http.post('/signout').success(function (response) {
                        // If successful we clear the session
                        Authentication.clear();
                    }).error(function (response) {
                        $scope.error = response.message;
                    });
                }
            });
    }
]);
