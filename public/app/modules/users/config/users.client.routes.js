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
            state('password', {
                url: '/settings/password',
                templateUrl: '/assets/app/modules/users/views/settings/change-password.client.view.html'
            }).
            state('accounts', {
                url: '/settings/accounts',
                templateUrl: '/assets/app/modules/users/views/settings/social-accounts.client.view.html'
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
            }).
            state('forgot', {
                url: '/password/forgot',
                templateUrl: '/assets/app/modules/users/views/password/forgot-password.client.view.html'
            }).
            state('reset-invlaid', {
                url: '/password/reset/invalid',
                templateUrl: '/assets/app/modules/users/views/password/reset-password-invalid.client.view.html'
            }).
            state('reset-success', {
                url: '/password/reset/success',
                templateUrl: '/assets/app/modules/users/views/password/reset-password-success.client.view.html'
            }).
            state('reset', {
                url: '/password/reset/:token',
                templateUrl: '/assets/app/modules/users/views/password/reset-password.client.view.html'
            });
    }
]);
