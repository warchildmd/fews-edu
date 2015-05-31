'use strict';

// Authentication service for user variables
angular.module('users').factory('Authentication', ['$cookieStore', '$location',
    function ($cookieStore, $location) {
        var _this = this;

        _this._data = {};
        if ($cookieStore.get('user') !== undefined) {
            _this._data.user = $cookieStore.get('user');
        }
        if ($cookieStore.get('token') !== undefined) {
            _this._data.token = $cookieStore.get('token');
        }

        _this._data.clear = function () {
            $cookieStore.remove('user');
            $cookieStore.remove('token');
            delete _this._data.user;
            delete _this._data.token;
            $location.path('/signin');
        }

        return _this._data;
    }
]);