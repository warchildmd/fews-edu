'use strict';

angular
    .module('fews', ['ngResource', 'ngAnimate', 'ui.router', 'ngCookies'])
    .config(function ($locationProvider) {
        $locationProvider.hashPrefix('!');
    });