(function () {

    'use strict';

    angular
        .module('fews')
        .controller('ArticlesListController', ArticlesListController);

    ArticlesListController.$inject = ['$q', 'Articles'];

    function ArticlesListController($q, Articles) {
        var self = this;

        self.loading = false;
        self.articles = [];
        self.popularArticles = [];
        self.recommendedArticles = [];

        activate();

        function activate() {
            self.loading = true;
            var articlesPromise = getArticles().then(function() {
                console.log('Articles retrieved!');
            });
            var popularArticlesPromise = getPopularArticles().then(function() {
                console.log('Popular articles retrieved!');
            });
            var recommendedArticlesPromise = getRecommendedArticles().then(function() {
                console.log('Recommended articles retrieved!');
            });
            return $q.all([articlesPromise, popularArticlesPromise, recommendedArticlesPromise]).then(function() {
                self.loading = false;
                console.log('All articles retrieved!');
            });
        }

        function getArticles() {
            return Articles.getArticles()
                .then(function(data) {
                    self.articles = data;
                    return self.articles;
                });
        }

        function getPopularArticles() {
            return Articles.getPopularArticles()
                .then(function(data) {
                    self.popularArticles = data;
                    return self.popularArticles;
                });
        }

        function getRecommendedArticles() {
            return Articles.getRecommendedArticles()
                .then(function(data) {
                    self.recommendedArticles = data;
                    return self.recommendedArticles;
                });
        }



    }

})();
