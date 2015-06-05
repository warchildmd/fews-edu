(function () {

    'use strict';

    angular
        .module('fews')
        .controller('ArticlesDetailController', ArticlesDetailController);

    ArticlesDetailController.$inject = ['$stateParams', '$q', '$sce', 'Articles'];

    function ArticlesDetailController($stateParams, $q, $sce, Articles) {
        var self = this;

        self.article = {};
        self.keywords = [];
        self.similars = [];

        activate();

        function activate() {
            var articlePromise = getArticle($stateParams.articleId)
                .then(function() {
                    console.log('Article retrieved!');
                });
            var similarsPromise = getSimilarArticles($stateParams.articleId)
                .then(function() {
                    console.log('Similar articles retrieved!');
                });
            return $q.all([articlePromise, similarsPromise]).then(function() {
                console.log('All articles retrieved!');
            });
        }

        function getArticle(articleId) {
            return Articles.getArticle(articleId)
                .then(function(data) {
                    self.article = data;

                    self.article.content = self.article.content.replace(/(.*)(?:\r\n|\r|\n)/g, '<p>$1</p>');
                    for (var i = 0; i < self.article.keywords.length; i += 1) {
                        self.article.content = bolden(self.article.content, self.article.keywords[i].keyword.content);

                        var keywords = getKeywords(self.article.content, self.article.keywords[i].keyword.content);
                        if (keywords === null) continue;
                        for (var j = 0; j < keywords.length; j++) {
                            if (self.keywords.indexOf(keywords[j]) == -1) {
                                self.keywords.push(keywords[j]);
                            }
                        }
                    }
                    self.article.content = $sce.trustAsHtml(self.article.content);

                    return self.article;
                });
        }

        function getSimilarArticles(articleId) {
            return Articles.getSimilarArticles(articleId)
                .then(function(data) {
                    self.similars = data;
                    return self.similars;
                });
        }

        function bolden(input, needle) {
            if (needle.indexOf(' ') ==  -1) {
                return input.replace(new RegExp('(\\w*' + needle + '\\w*)', 'ig'), '<b>$1</b>');
            } else {
                var left = needle.substring(0, needle.indexOf(' '));
                var right = needle.substring(needle.indexOf(' ') + 1);
                return input.replace(new RegExp('(\\w*' + left + '\\w* \\w*' + right + '\\w*)', 'ig'), '<b>$1</b>');
            }
        }

        function getKeywords(input, needle) {
            if (needle.indexOf(' ') ==  -1) {
                return input.match(new RegExp('(\\w*' + needle + '\\w*)', 'ig'));
            } else {
                var left = needle.substring(0, needle.indexOf(' '));
                var right = needle.substring(needle.indexOf(' ') + 1);
                return input.match(new RegExp('(\\w*' + left + '\\w* \\w*' + right + '\\w*)', 'ig'));
            }
        }

    }

})();
