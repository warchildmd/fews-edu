'use strict';

// Setting up route
angular.module('core').config(['$stateProvider', '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {
        // Redirect to home view when route not found
        $urlRouterProvider.otherwise('/');

        // Home state routing
        $stateProvider.
            state('listArticles', {
                url: '/',
                templateUrl: '/assets/app/modules/core/views/list-articles.client.view.html'
            }).
            state('viewArticle', {
                url: '/articles/:articleId',
                templateUrl: '/assets/app/modules/core/views/view-article.client.view.html'
            })
            .state('viewCategory', {
                url: '/category/:categoryId',
                templateUrl: '/assets/app/modules/core/views/list-articles.client.view.html'
            });
    }
]);
