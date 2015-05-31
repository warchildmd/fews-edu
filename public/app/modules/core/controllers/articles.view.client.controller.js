'use strict';

angular.module('core').controller('ArticlesViewController', ['$scope', '$sce', '$stateParams', '$location', 'Authentication', 'Articles',
	function($scope, $sce, $stateParams, $location, Authentication, Articles) {
		$scope.authentication = Authentication;

        $scope.bold = function(input, needle) {
            if (needle.indexOf(' ') ==  -1) {
                return input.replace(new RegExp('(\\w*' + needle + '\\w*)', 'ig'), '<b>$1</b>');
            } else {
                var left = needle.substring(0, needle.indexOf(' '));
                var right = needle.substring(needle.indexOf(' ') + 1);
                // return input.replace(new RegExp('(^|\\s)(\\s*' + left + '\\s* \\s*' + right + '\\s*)(\\s|$)', 'ig'), '$1<b>$2</b>$3');
                return input.replace(new RegExp('(\\w*' + left + '\\w* \\w*' + right + '\\w*)', 'ig'), '<b>$1</b>');
            }
        }

		$scope.findOne = function() {
			$scope.article = Articles.get({
				articleId: $stateParams.articleId
			});
            $scope.article.$promise.then(function(data) {
                $scope.article.content = $scope.article.content.replace(/(.*)(?:\r\n|\r|\n)/g, '<p>$1</p>');
                for (var i = 0; i < $scope.article.keywords.length; i += 1) {
                    $scope.article.content = $scope.bold($scope.article.content
                        , $scope.article.keywords[i].keyword.content);
                }
                $scope.article.content = $sce.trustAsHtml($scope.article.content);
            });
            $scope.similarArticles = Articles.similar({articleId: $stateParams.articleId});
		};
	}
]);