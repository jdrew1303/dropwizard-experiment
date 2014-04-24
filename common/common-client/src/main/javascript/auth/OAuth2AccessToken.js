define(function (require) {
    "use strict";
    var $ = require("jquery");
    var _ = require("underscore");
    var TBoneModel = require("common/TboneModel");

    var prefixes = {};

    $.ajaxPrefilter(function (options) {
        options.headers = options.headers || {};

        _.each(prefixes, function (token, prefix) {
            if (options.url.indexOf(prefix) === 0) {
                options.headers.Authorization = "Bearer " + token;
            }
        });
    });

    /**
     * @class OAuth2AccessToken
     */
    return TBoneModel.extend({
        defaults: {
            accessToken: null
        },

        addToRequestsFor: function (urlPrefix) {
            prefixes[urlPrefix] = this.get("accessToken");
        }
    }, {
        fetchByLogin: function (username, password) {
            return this.fetchById(null, {
                url: "http://localhost:8080/api/token",
                data: {
                    username: username,
                    password: password,
                    grant_type: "password"
                },
                type: "POST"
            });
        }
    });
});