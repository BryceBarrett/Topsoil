/* 
 * Copyright 2014 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * TOPSOIL
 */

// "use strict" here causes an error
// not sure why

// top level containers
var topsoil = ts = {data: []}, chart = {};

// adds a row of data
topsoil.addData = function (data) {
    var convertedData = {};

    // store the data in the 
    convertedData.x = data[0];
    convertedData.sigmaX = convertedData.sigma_x = convertedData.σx = data[1];
    convertedData.y = data[2];
    convertedData.sigmaY = convertedData.sigma_y = convertedData.σy = data[3];
    convertedData.rho = convertedData.ρ = data[4];

    ts.data.push(convertedData);
};

// clears all stored data
topsoil.clearData = function () {
    ts.data = [];
};

topsoil.showData = function () {
    // build settings
    var settings = {};
    ts.settingsManager.getSettings().forEach(function (setting) {
        Object.defineProperty(settings, setting.getName(), {
            get: function () {
                return setting.getValue();
            },
            set: function (value) {
                listener.block();
                setting.setValue(value);
                listener.unblock();
            }
        });
    });

    chart.settings = settings;
    chart.draw(ts.data);
};

/*
 * CHART
 */

// an acknoledged (seemingly arbitrary) fudge factor for the window dimensions
// without this (which has been minimized!) scrollbars show (at least on OS X)
var magicNumber = 3.0000375;

chart.margin = {top: 75, right: 75, bottom: 75, left: 75};
chart.width = window.innerWidth - magicNumber - chart.margin.left - chart.margin.right;
chart.height = window.innerHeight - magicNumber - chart.margin.top - chart.margin.bottom;

// set up for the chart
chart.area = d3.select("body")
        // create the svg element
        .append("svg")
        // somewhat confusing locally, but this element should be considered
        // to be the chart externally
        .attr("id", "chart")
        .attr("width", chart.width + chart.margin.left + chart.margin.right)
        .attr("height", chart.height + chart.margin.top + chart.margin.bottom)
        // create a new coordinate space that accounts for the margins
        .append("g")
        .attr("transform", "translate(" + chart.margin.left + "," + chart.margin.top + ")");

// create a clip path and backing for the plot area
// the visible (white) backing is necessary for mouse events
chart.area.clipped = chart.area.append("g")
        .attr("clip-path", "url(#clipBox)");

chart.plotArea = chart.area.clipped.append("rect")
        .attr("id", "plotArea")
        .attr("width", chart.width)
        .attr("height", chart.height)
        .attr("fill", "white")
        .attr("stroke", "none");

chart.area.append("defs")
        .append("clipPath")
        .attr("id", "clipBox")
        .append("use")
        .attr("xlink:href", "#" + chart.plotArea.attr("id"));

// creates a buffer until the settings manager is set up
chart.settings = [];
chart.settings.addSetting = function (name, value) {
    chart.settings.push([name, value]);

    // so that this method can be chained
    return chart.settings;
};

/*
 * SETTINGS
 */

// constants for setting names
var X_MAX = "X Max";
var X_MIN = "X Min";
var Y_MAX = "Y Max";
var Y_MIN = "Y Min";

var listener;

topsoil.transferSettings = function (settingsManager) {
    // store the settings manager for later
    ts.settingsManager = settingsManager;
    settingsManager.addJSListener(listener = {
        blocked: false,
        onUpdate: function (setting) {
            if (!blocked) {
                topsoil.showData();
            }
        },
        block: function () {
            blocked = true;
        },
        unblock: function () {
            blocked = false;
        }
    });

    chart.settings.forEach(function (setting) {
        // setting is a 2-array in the form [name, value]
        settingsManager.addSetting(setting[0], setting[1]);
    });
};