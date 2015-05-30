'use strict';

// Setting up route
angular.module('articles').config(['$stateProvider',
    function ($stateProvider) {
        // Articles state routing
        $stateProvider.
            state('listArticles', {
                url: '/articles',
                templateUrl: '/assets/app/modules/articles/views/list-articles.client.view.html'
            }).
            state('createArticle', {
                url: '/articles/create',
                templateUrl: '/assets/app/modules/articles/views/create-article.client.view.html'
            }).
            state('viewArticle', {
                url: '/articles/:articleId',
                templateUrl: '/assets/app/modules/articles/views/view-article.client.view.html'
            }).
            state('editArticle', {
                url: '/articles/:articleId/edit',
                templateUrl: '/assets/app/modules/articles/views/edit-article.client.view.html'
            });
    }
]);
