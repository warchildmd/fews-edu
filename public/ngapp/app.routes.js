(function () {
    'use strict';

    angular
        .module('fews')
        .config(routes);

    routes.$inject = ['$stateProvider', '$urlRouterProvider'];

    function routes($stateProvider, $urlRouterProvider) {

        $urlRouterProvider.otherwise("/");

        $stateProvider
            .state('list', {
                url: "/",
                templateUrl: "/assets/ngapp/views/articles.list.view.html",
                controller: 'ArticlesListController',
                controllerAs: 'vm'
            })
            .state('detail', {
                url: "/articles/:articleId",
                templateUrl: "/assets/ngapp/views/articles.detail.view.html",
                controller: 'ArticlesDetailController',
                controllerAs: 'vm'
            });
    }
})();