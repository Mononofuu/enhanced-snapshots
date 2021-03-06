'use strict';

angular.module('web')
    .controller('modalSystemUninstallCtrl', function ($scope, $modalInstance, System) {
        $scope.state = 'ask';

        $scope.delete = function () {
            System.delete($scope.instanceId).then(function () {
                $scope.state = "done";
            }, function(e){
                $scope.delError = e;
                $scope.state = "failed";
            });

        }
    });