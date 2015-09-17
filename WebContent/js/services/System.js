'use strict';

angular.module('web')
    .service('System', function ($q, $http, BASE_URL) {
        var url = BASE_URL + 'rest/system';

        var _get = function () {
            var deferred = $q.defer();
            $http.get(url).then(function (result) {
                deferred.resolve(result.data);
            }, function (e) {
                deferred.reject(e);
            });
            return deferred.promise;
        };

        var _delete = function (instanceId) {
            var deferred = $q.defer();
            $http.post(url + '/delete', {instanceId: instanceId}).then(function (result) {
                deferred.resolve(result.data);
            }, function (e) {
                deferred.reject(e);
            });
            return deferred.promise;
        };

        return {
            get: function () {
                return _get();
            },
            delete: function (instanceId) {
                return _delete(instanceId);
            }
        }
    });