define(function (require) {
    "use strict";
    var _ = require("underscore");
    var PreRouteHistory = require("common/auth/PreRouteHistory");
    var Promise = require("bluebird");
    var AuthController = require("todo/auth/AuthController");

    var authNotRequiredFragments = ["resetpassword", "verify/"];

    function authNotRequired(fragment) {
        return _.some(authNotRequiredFragments, function (notReqFragment) {
            return fragment.indexOf(notReqFragment) === 0;
        });
    }

    /**
     * Backbone.History that authenticates the user before allowing access to any routes.
     *
     * @class AuthenticatingHistory
     */
    return PreRouteHistory.extend({
        preRoute: function (fragment) {
            if (AuthController.getCurrentUser().get("isLoggedIn") || authNotRequired(fragment)) {
                return Promise.resolve();
            } else {
                return AuthController.attemptLogin(this.options.region);
            }
        }
    });
});