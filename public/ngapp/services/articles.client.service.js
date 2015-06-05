(function() {

    'use strict';

    angular
        .module('fews')
        .factory('Articles', Articles);

    Articles.$inject = ['$http'];

    function Articles($http) {
        var articles = {
            getArticles: getArticles
        };
        return articles;

        function getArticles() {
            return $http.get('/api/articles')
                .then(getArticlesComplete)
                .catch(getArticlesFailed);

            function getArticlesComplete(response) {
                return response.data;
            }

            function getArticlesFailed(error) {
                console.log('XHR Failed for getArticles.' + error.data);
            }
        }

    }

})();
