(function() {

    'use strict';

    angular
        .module('fews')
        .factory('Articles', Articles);

    Articles.$inject = ['$http'];

    function Articles($http) {
        var articles = {
            getArticles: getArticles,
            getArticle: getArticle,
            getPopularArticles: getPopularArticles,
            getRecommendedArticles: getRecommendedArticles,
            getSimilarArticles: getSimilarArticles
        };
        return articles;

        function getArticle(articleId) {
            return $http.get('/api/articles/' + articleId)
                .then(getComplete)
                .catch(getFailed);

            function getComplete(response) {
                return response.data;
            }

            function getFailed(error) {
                console.log('XHR Failed for getArticles.' + error.data);
            }
        }

        function getArticles() {
            return $http.get('/api/articles')
                .then(getComplete)
                .catch(getFailed);

            function getComplete(response) {
                return response.data;
            }

            function getFailed(error) {
                console.log('XHR Failed for getArticles.' + error.data);
            }
        }

        function getPopularArticles() {
            return $http.get('/api/articles/popular')
                .then(getComplete)
                .catch(getFailed);

            function getComplete(response) {
                return response.data;
            }

            function getFailed(error) {
                console.log('XHR Failed for getPopularArticles.' + error.data);
            }
        }

        function getRecommendedArticles() {
            return $http.get('/api/articles/recommended')
                .then(getComplete)
                .catch(getFailed);

            function getComplete(response) {
                return response.data;
            }

            function getFailed(error) {
                console.log('XHR Failed for getRecommendedArticles.' + error.data);
            }
        }

        function getSimilarArticles(articleId) {
            return $http.get('/api/articles/' + articleId + '/similar')
                .then(getComplete)
                .catch(getFailed);

            function getComplete(response) {
                return response.data;
            }

            function getFailed(error) {
                console.log('XHR Failed for getSimilarArticles.' + error.data);
            }
        }

    }

})();
