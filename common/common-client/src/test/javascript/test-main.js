/*global require, beforeEach, jasmine*/
// RequireJS main file to start Karma test runs.
(function () {
    "use strict";

    // Override the baseUrl from require.config.js with Karma's base directory.
    require.config({
        baseUrl: "/base"
    });

    // Karma has a list of all the files it serves, process those so we can require all the tests.
    var allTests = Object.keys(window.__karma__.files).filter(function (file) {
        // Tests end with Spec.js
        // But so do some of the files we're serving from bower_components, so check that they come from our tests directory as well.
        return (/\/src\/test\/javascript\/.*?Spec\.js$/).test(file);
    });

    require(["test/JasmineMatchers", "common/util/Logger", "jasmine-jquery"].concat(allTests), function (JasmineMatchers, Logger) {
        beforeEach(function () {
            jasmine.addMatchers(JasmineMatchers);
        });

        Logger.setAllLogLevels(Logger.LogLevel.OFF);

        // Start the test run.
        window.__karma__.start();
    });
}());