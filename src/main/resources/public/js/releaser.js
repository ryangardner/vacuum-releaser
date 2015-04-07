var app = angular.module('releaserHome', ['angularMoment', 'ui.router', 'angularDc'])
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


app.service("storageTankService", ["$http", function ($http) {
    var set = function (params) {
        return $http
            .post("/storageTank", params)
            .then(function (response) {
                return response.data;
            });
    };

    var get = function () {
        return $http
            .get("/storageTank")
            .then(function (response) {
                return response.data;
            });
    };

    return {set: set, get: get};
}]);

app.service("settingsService", ["$http", function ($http) {
    var set = function (params) {
        return $http
            .post("/settings", params)
            .then(function (response) {
                return response.data;
            });
    };

    var get = function () {
        return $http
            .get("/settings")
            .then(function (response) {
                return response.data;
            });
    };

    return {set: set, get: get};
}]);

app.service("dataService", ["$http", function ($http) {
    var get = function () {
        return $http
            .get("/releaserEvents")
            .then(function (response) {
                return response.data;
            });
    };

    return {get: get};
}]);

app.controller('statisticsOverview',
    function ($scope, $http) {
        $http.get('/basicStats').
            success(function (data) {
                $scope.releaserStats = data;
            });
    });

app.controller('todaysStats', ["$scope", "$http", "settingsService",
    function ($scope, $http, settingsService) {
        $http.get('/basicStats').
            success(function (data) {
                $scope.releaserStats = data;
            });

        settingsService.get().then(function (data) {
            $scope.releaserSettings = data;
        })

    }]);

app.controller('weatherSensors',
    function ($scope, $http) {
        $http.get('/sensors/weather').
            success(function (data) {
                $scope.weatherSensors = data;
                $scope.weatherSensors.boilingPoint = (49.161 * Math.log(data.pressure) + 44.932)
            });
    });


app.controller('dataVisController', ["$scope", "dataService", function ($scope, dataService) {
    dataService.get().then(function (data) {

        data.forEach(function (d) {
            d.dd = new Date(d.startTime * 1000);
            d.day = d3.time.day(d.dd);
            d.hour = d.dd.getHours();
        });


        var numberFormat = d3.format(".2f");
        var s = $scope;

        s.ndx = crossfilter(data);

        s.dateDimension = s.ndx.dimension(function (d) {
            return d.dd;
        });

        s.hourlyDimension = s.ndx.dimension(function (d) {
            return d.hour;
        });

        s.dailyDimension = s.ndx.dimension(function (d) {
            return d.day;
        });

        s.temperatureDimension = s.ndx.dimension(function (d) {
            return Math.round(d.temperature);
        });

        s.hourlyGroup = s.hourlyDimension.group().reduceSum(function (d) {
            return d.sapQuantity;
        });

        s.temperatureGroup = s.temperatureDimension.group().reduceSum(function (d) {
            return d.sapQuantity;
        });

        s.dailyGroup = s.dailyDimension.group().reduceSum(function (d) {
            return d.sapQuantity;
        });

        var _group = s.dateDimension.group().reduceSum(function(d) {return 1;});

        // this lets us do a cumulative gallons-per-tap graph
        s.cumulativePerTap = {
            all:function () {
             var cumulate = 0.0;
             var g = [];
             _group.all().forEach(function(d,i) {
               cumulate += d.value/107;
               g.push({key:d.key,value:cumulate})
             });
             return g;
            }
          };

        s.tableGroup = function (d) {
            var format = d3.format("02d");
            var dateFormat = d3.time.format('%B %Y');
            return dateFormat(d.dd);
            //return d.dd.getFullYear() + "/" + format((d.dd.getMonth() + 1));
        };
        s.timeFormat = d3.time.format('%a %b %e, %I:%M:%S %p');

        s.tablePostSetupChart = function (c) {
            // dynamic columns creation using an array of closures
            c.columns([
                function (d) {
                    return s.timeFormat(d.dd);
                },
                function (d) {
                    return numberFormat(d.sapQuantity);
                },
                function (d) {
                    return numberFormat(d.temperature);
                }
            ])
                // (optional) sort using the given field, :default = function(d){return d;}
                .sortBy(function (d) {
                    return d.dd;
                })
                // (optional) sort order, :default ascending
                .order(d3.ascending)
                // (optional) custom renderlet to post-process chart using D3
                .renderlet(function (table) {
                    table.selectAll(".dc-table-group").classed("info", true);
                });
        };
        s.resetAll = function () {
            dc.filterAll();
            dc.redrawAll();
        };

        s.adjustTickFormat = function (chart, options) {
            //chart.xAxis().tickFormat(function (x) { return (x.getMonth() + 1) + "/" + (x.getDate());})
        };

        s.hours = ["'12 AM'", "'1 AM'", '2 AM', '3 AM', '4 AM', '5 AM', '6 AM', '7 AM', '8 AM', '9 AM', '10 AM', '11 AM', '12 PM', '1 PM', '2 PM', '3 PM', '4 PM', '5 PM', '6 PM', '7 PM', '8 PM', '9 PM', '10 PM', '11 PM', '12 PM'];

        s.updateHourFormat = function (chart, options) {
            chart.xAxis().tickFormat(function (x) {
                return s.hours[x];
            });//(function(x) { return $scope.hours[x];});
        };

        s.minDate = s.dateDimension.bottom(1)[0].dd;
        s.maxDate = s.dateDimension.top(1)[0].dd;

        console.log(data);
    })
}]);

app.controller('storageTankController', ["$scope", "storageTankService", function ($scope, storageTankService) {
    storageTankService.get().then(function (data) {
        console.log(data);
        $scope.currentVolume = data.currentVolume;
        $scope.capacity = data.capacity;
        $scope.warningThreshold = data.warningThreshold;
    });

    $scope.drainTank = function () {
        storageTankService.set({
            "id": 0,
            "currentVolume": 0,
            "capacity": $scope.capacity,
            "warningThreshold": $scope.warningThreshold
        })
            .then(function (data) {
                $scope.currentVolume = 0;
                console.log(data);
            });
    }

    $scope.submit = function () {
        storageTankService.set({
            "id": 0,
            "currentVolume": $scope.currentVolume,
            "capacity": $scope.capacity,
            "warningThreshold": $scope.warningThreshold
        })
            .then(function (data) {
                console.log(data);
            });
    }
}]);

app.controller('settingsController', ["$scope", "settingsService", function ($scope, settingsService) {
    settingsService.get().then(function (data) {
        console.log(data);
        $scope.numberOfTaps = data.numberOfTaps;
        $scope.gallonsPerFullDump = data.gallonsPerFullDump;
    });

    //$scope.numberOfTaps = 100;
    //$scope.gallonsPerFullDump = 1.05;

    $scope.submit = function () {
        settingsService.set({
            "id": 0,
            "numberOfTaps": $scope.numberOfTaps,
            "gallonsPerFullDump": $scope.gallonsPerFullDump
        })
            .then(function (data) {
                console.log(data);
            });
    }
}]);