(function () {
    'use strict';

    angular
        .module('fews')
        .config(locationConfig);

    // Config HTTP Error Handling
    angular
        .module('fews')
        .config(configurator);

    locationConfig.$inject = ['$locationProvider'];

    function locationConfig($locationProvider) {
        $locationProvider.hashPrefix('!');
    }

    configurator.$inject = ['$httpProvider'];

    function configurator($httpProvider) {
        $httpProvider.interceptors.push(interceptor);

        interceptor.$inject = ['$q', '$rootScope', '$cookieStore'];

        function interceptor($q, $rootScope, $cookieStore) {
            return {
                responseError: function (rejection) {
                    switch (rejection.status) {
                        case 401:
                            $cookieStore.remove('session');
                            $cookieStore.remove('user');
                            $rootScope.$broadcast('EVENT_SIGN_OUT_SUCCESS');
                            break;
                    }
                    return $q.reject(rejection);
                },
                request: function (config) {
                    if ($cookieStore.get('session') !== undefined) {
                        config.headers['Auth-Token'] = $cookieStore.get('session');
                    }
                    return config;
                }
            };
        }
    }

})();