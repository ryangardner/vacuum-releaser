var app = angular.module('releaserHome', ['angularMoment', 'ui.router'])
    .run(['$rootScope', '$state', '$stateParams',
        function ($rootScope, $state, $stateParams) {

            // Add references to $state and $stateParams to the $rootScope
            // so that we can access them from any scope within your applications.
            // For example,
            // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
            // to active whenever 'contacts.list' or one of its decendents is active.
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
        }
    ])
    .config(['$stateProvider', function ($stateProvider) {
        var home = {
                name: 'home',
                url: '/',
                templateUrl: 'pages/main.html'
            },
            settings = {
                name: 'settings',
                url: 'settings',
                templateUrl: 'pages/settings.html'
            };
        $stateProvider.state(home);
        $stateProvider.state(settings);
    }]);

app.controller('statisticsOverview',
    function ($scope, $http) {
        $http.get('/basicStats').
            success(function (data) {
                $scope.releaserStats = data;
            });
    });

app.controller('todaysStats',
    function ($scope, $http) {
        $http.get('/basicStats').
            success(function (data) {
                $scope.releaserStats = data;
            });
    });


