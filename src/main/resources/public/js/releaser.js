var app = angular.module('releaserHome', ['angularMoment', 'ui.router', 'ngResource'])
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
            data = {
                name: 'data',
                url: 'data',
                templateUrl: 'pages/data.html'
            },
            settings = {
                name: 'settings',
                url: 'settings',
                templateUrl: 'pages/settings.html'
            };
        $stateProvider.state(home);
        $stateProvider.state(data);
        $stateProvider.state(settings);
    }]);


app.service("settingsService", ["$http", function($http){
    var set = function (params){
        return $http
               .post("/settings", params)
               .then(function (response){
                  return response.data;
               });
    };

    return { set : set };
}]);

app.factory("Settings", function($resource){
   return $resource("/settings", {}, {
        query: { method: "GET", isArray: false}
    });
});

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

app.controller('settingsController', ["$scope","settingsService", function($scope, settingsService){
    $scope.numberOfTaps = 100;
    $scope.gallonsPerFullDump = 1.05;

    $scope.submit = function(){
        settingsService.set({"id":0, "numberOfTaps":$scope.numberOfTaps, "sapQuantityPerFullDump":$scope.gallonsPerFullDump})
                        .then(function(data){
                            console.log(data);
                        });
    }
}]);