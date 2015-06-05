(function () {

    'use strict';

    angular
        .module('fews')
        .controller('ArticlesListController', ArticlesListController);

    ArticlesListController.$inject = ['$scope', 'Articles'];

    function ArticlesListController($scope, Articles) {
        var self = this;

        self.articles = [];

        activate();

        function activate() {
            return getArticles().then(function() {
                console.log('Articles retrieved!');
            });
        }

        function getArticles() {
            return Articles.getArticles()
                .then(function(data) {
                    self.articles = data;
                    return self.articles;
                });
        }



    }

})();
