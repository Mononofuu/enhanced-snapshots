'use strict';

angular.module('web')
    .controller('HistoryController', function ($scope, $stateParams, $state, $modal, $filter, Backups, Tasks) {
        $scope.statusColorClass = {
            running: "primary",
            success: "success",
            failed: "danger"
        };

        $scope.isRunning = function (backup) {
            return backup.status == "running";
        };

        $scope.isSuccess = function (backup) {
            return backup.status == "success";
        };

        $scope.volumeID = $stateParams.volumeID;

        $scope.backups = [];
        var loadBackups = function () {
            Backups.getForVolume($scope.volumeID).then(function (data) {
                $scope.backups = data;
            });
        };
        loadBackups();

        $scope.restore = function (backup) {
            $scope.backupToRestore = backup;
            var confirmInstance = $modal.open({
                animation: true,
                templateUrl: './partials/modal.backup-restore.html',
                scope: $scope
            });

            confirmInstance.result.then(function () {
                var newTask = {
                    volume: $scope.backupToRestore.volume,
                    type: "restore",
                    status: "waiting",
                    scheduler: {
                        manual: true,
                        name: "admin", // TODO: Real user name should be used here
                        time: $filter('date')(new Date(), "yyyy-MM-dd HH:mm:ss") // TODO: Move time format to global setting
                    }
                };
                Tasks.insert(newTask).then(function () {
                    var successInstance = $modal.open({
                        animation: true,
                        templateUrl: './partials/modal.task-restore-created.html'
                    });

                    successInstance.result.then(function () {
                        $state.go('app.tasks');
                    });
                });
            });

        };

        $scope.remove = function (backup) {
            $scope.backupToDelete = backup;

            var rejectInstance = $modal.open({
                animation: true,
                templateUrl: './partials/modal.backup-delete.html',
                scope: $scope
            });

            rejectInstance.result.then(function () {
                Backups.delete(backup.id).then(function () {
                    loadBackups();
                });
            });
        };

    });