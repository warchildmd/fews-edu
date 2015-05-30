'use strict';

angular.module('core').controller('ArticlesListController', ['$scope',
    '$state', '$stateParams', '$location', 'Authentication', 'Articles',
    function ($scope, $state, $stateParams, $location, Authentication, Articles) {
        $scope.authentication = Authentication;

        $scope.find = function () {
            $scope.page = 1;
            var data  = {};
            if ($state.current.name === 'viewCategory') {
                data.category_id = $state.params.categoryId;
            }
            $scope.articles = Articles.query(data);
            $scope.popular_articles = Articles.popular(data);
        };

        $scope.loadMore = function () {
            $scope.page += 1;
            var promise = Articles.query({
                page: $scope.page
            }).$promise;
            promise.then(function (data) {
                $scope.articles.results = $scope.articles.results.concat(data.results);
                console.log($scope.articles);
            });
        }


    }
]);