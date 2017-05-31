/*
 * Copyright 2017 CIRDLES.
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

plot.dataKeys = ['x', 'sigma_x', 'y', 'sigma_y', 'rho', 'Selected'];
plot.propertiesKeys = [
    'Point Fill Color',
    'Ellipse Fill Color',
    'Cross Fill Color',
    'Point Opacity',
    'Ellipse Opacity',
    'Cross Opacity',
    'Title',
    'Uncertainty',
    'X Axis',
    'Y Axis',
    'Points',
    'Ellipses',
    'Crosses',
    'Concordia',
    'Evolution',
    'Isotope'];

/*
    Creates an SVG group for data elements like points and ellipses. Inserting other groups below this one ensures that
    the data is always the top layer.
 */
plot.dataGroup = plot.area.clipped.append("g")
    .attr("class", "dataGroup");

if (plot.initialized == null) {
    plot.initialized = false;
}

/*
    Initializes required values. This function handles operations that only need to be done once, or need to be done at the
    beginning.
 */
plot.initialize = function (data) {

    //create title
    plot.area.append("text")
        .attr("class", "titleText")
        .attr("font-family", "sans-serif")
        .attr("font-size", "20px")
        .attr("x", plot.width / 2)
        .attr("y", -25);

    //create x axis label
    plot.area.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + plot.height + ")")
        .append("text")
        .attr("class", "label")
        .style("font-size", "16px")
        .attr("x", plot.width / 2)
        .attr("y", 35);

    //create y axis label
    plot.area.append("g")
        .attr("class", "y axis")
        .append("text")
        .attr("class", "label")
        .attr("transform", "rotate(-90)")
        .style("font-size", "16px")
        .attr("x", -plot.height / 2)
        .attr("y", -50)
        .attr("dy", ".1em");

    // defaults if no data is provided
    plot.xDataMin = 0;
    plot.xDataMax = 1;
    plot.yDataMin = 0;
    plot.yDataMax = 1;

    // Initialize axis scales
    plot.xAxisScale = d3.scale.linear();
    plot.yAxisScale = d3.scale.linear();

    plot.t = d3.scale.linear();

    //draw the axes
    plot.xAxis = d3.svg.axis().orient("bottom");
    plot.yAxis = d3.svg.axis().orient("left");

    // Updates plot.xDataMin, plot.xDataMax, etc. based on the data.
    plot.updatePlotExtent(data);

    // Updates the scales for the x and y axes
    plot.xAxisScale
        .domain([plot.xDataMin, plot.xDataMax])
        .range([0, plot.width]);
    plot.yAxisScale
        .domain([plot.yDataMin, plot.yDataMax])
        .range([plot.height, 0]);

    // Applies the scales to the x and y axes.
    plot.xAxis.scale(plot.xAxisScale);
    plot.yAxis.scale(plot.yAxisScale);

    plot.initialized = true;
    plot.draw(data);

};

/*
    Draws and sets plot elements, defines special behaviors like topsoil.recenter() and d3.behavior.zoom(). This function
    handles operations that need to be re-performed whenever new data is entered.
 */
plot.draw = function (data) {

    // Makes sure that the plot has been initialized.
    if (!plot.initialized) {
        plot.initialize(data);
        return;
    }

    if(plot.currentIsotope == null) {
        plot.currentIsotope = plot.getProperty('Isotope');

        // Set minimum bounds for plot based on isotope type.
        switch (plot.currentIsotope) {
            case "Uranium Lead":
                plot.minimumX = -1;
                plot.minimumY = -1;
                break;
            // case "Uranium Thorium":
            //     plot.minimumX = 0;
            //     plot.minimumY = 0;
            //     break;
            default:
                plot.minimumX = 0;
                plot.minimumY = 0;
                break;
        }
    }

    //calculate constants used to draw ellipses
    if (plot.uncertainty == null) {
        plot.uncertainty = plot.getProperty("Uncertainty");
    }
    plot.ellipseData = plot.calcEllipses(data);

    // Updates plot.xDataMin, plot.xDataMax, etc. based on the data.
    plot.updatePlotExtent(data);

    // Updates the scales for the x and y axes
    plot.xAxisScale
        .domain([plot.xDataMin, plot.xDataMax])
        .range([0, plot.width]);
    plot.yAxisScale
        .domain([plot.yDataMin, plot.yDataMax])
        .range([plot.height, 0]);

    plot.xAxis.scale(plot.xAxisScale);
    plot.yAxis.scale(plot.yAxisScale);

    // Removes any existing elements so they can be redrawn with the new data.
    plot.removeDataFeatures()
    plot.removePlotFeatures();

    // add pan/zoom
    var zoom = plot.zoom = d3.behavior.zoom()
        .x(plot.xAxisScale)
        .y(plot.yAxisScale);
        // .scaleExtent([0.5, 2.5]);
    function zoomed() {
        plot.zoomed(data);
    }
    zoom.on("zoom", zoomed);
    plot.area.clipped.call(zoom);

    // function to recenter the plot to its original view
    topsoil.recenter = function() {
        d3.transition().duration(750).tween("zoom", function() {
            var ix = d3.interpolate(plot.xAxisScale.domain(), [plot.xDataMin, plot.xDataMax]);
            var iy = d3.interpolate(plot.yAxisScale.domain(), [plot.yDataMin, plot.yDataMax]);
            return function(t) {
                zoom.x(plot.xAxisScale.domain(ix(t))).y(plot.yAxisScale.domain(iy(t)));
                zoomed();
            };
        });
    };

    // call the axes
    plot.area.selectAll(".x.axis").call(plot.xAxis);
    plot.area.selectAll(".y.axis").call(plot.yAxis);

    plot.update(data);
};

/*
    Updates plot elements. This function handles operations that need to be re-performed every time there is a change made
    to the plot.
 */
plot.update = function (data) {

    // Makes sure that the plot has been initialized.
    if (!plot.initialized) {
        plot.initialize(data);
        return;
    }

    //if the isotope type has changed, alert Java
    if (plot.currentIsotope != plot.getProperty('Isotope')) {
        plot.currentIsotope = plot.getProperty('Isotope');

        // Set minimum bounds for plot based on isotope type.
        switch (plot.currentIsotope) {
            case "Uranium Lead":
                plot.minimumX = -1;
                plot.minimumY = -1;
                break;
            default:
                plot.minimumX = 0;
                plot.minimumY = 0;
                break;
        }
    }

    // If the uncertainty has changed, the plot extent and ellipse data have to be re-calculated, and the ellipses
    // redrawn. Removes ellipses to be later re-drawn by plot.manageEllipses().
    if (plot.uncertainty != plot.getProperty("Uncertainty")) {
        plot.uncertainty = plot.getProperty("Uncertainty");
        plot.updatePlotExtent(data);
        plot.ellipseData = plot.calcEllipses(data);
        plot.removeEllipses();
    }

    //draw title and axis labels
    d3.select(".titleText")
        .text(plot.getProperty("Title"))
        .attr("x", (plot.width / 2) - (d3.select(".titleText").node().getBBox().width) / 2);
    d3.select(".x.axis .label")
        .text(plot.getProperty("X Axis"))
        .attr("x", (plot.width) - (d3.select(".x.axis .label").node().getBBox().width));

    d3.select(".y.axis .label")
        .text(plot.getProperty("Y Axis"))
        .attr("x",  -(d3.select(".y.axis .label").node().getBBox().width));

    // axis styling
    plot.area.selectAll(".axis text")
        .attr("font-family", "sans-serif")
        .attr("font-size", "10px");
    plot.area.selectAll(".axis path, .axis line")
        .attr("fill", "none")
        .attr("stroke", "black")
        .attr("shape-rendering", "geometricPrecision"); // see SVG docs

    // Manage the plot elements
    plot.managePoints(data);
    plot.manageEllipses(plot.ellipseData);
    plot.manageCrosses(data);
    plot.managePlotFeatures();
};

/*
    Custom zoom behavior function, required for d3.behavior.zoom(). Re-calls the axes for the new translation and scale,
    then updates all plot elements.
 */
plot.zoomed = function(data) {

    var newXMin = plot.xAxisScale.domain()[0];
    var newXMax = plot.xAxisScale.domain()[1];
    var newYMin = plot.yAxisScale.domain()[0];
    var newYMax = plot.yAxisScale.domain()[1];

    // keep the user from panning past minimum bounds
    // TODO Right now, the user can pan past minimum bounds, it just won't show. Fix that.
    if (newXMin <= plot.minimumX) {
        plot.xAxisScale.domain([plot.minimumX, (plot.minimumX + (newXMax - newXMin))]);
    }
    if (newYMin <= plot.minimumY) {
        plot.yAxisScale.domain([plot.minimumY, (plot.minimumY + (newYMax - newYMin))]);
    }

    // re-tick the axes
    plot.area.selectAll(".x.axis").call(plot.xAxis);
    plot.area.selectAll(".y.axis").call(plot.yAxis);

    // evolution.ar08lim = [zoom.x().domain()[0], zoom.x().domain()[1]];
    // evolution.ar48lim = [zoom.y().domain()[0], zoom.y().domain()[1]];

    plot.update(data);
};

/*
    Updates the global variables plot.xDataMin, plot.yDataMin, plot.xDataMax, and plot.yDataMax based on the data provided. If
    plot.uncertainty is unspecified, the default value 2 is used.
 */
plot.updatePlotExtent = function (data) {
    //find the extent of the points
    if (data.length > 0) {
        var dataXMin = d3.min(data, function (d) {
            return d.x - (d.sigma_x * (plot.uncertainty != null ? plot.uncertainty : 2));
        });
        var dataYMin = d3.min(data, function (d) {
            return d.y - (d.sigma_y * (plot.uncertainty != null ? plot.uncertainty : 2));
        });
        var dataXMax = d3.max(data, function (d) {
            return d.x + (d.sigma_x * (plot.uncertainty != null ? plot.uncertainty : 2));
        });
        var dataYMax = d3.max(data, function (d) {
            return d.y + (d.sigma_y * (plot.uncertainty != null ? plot.uncertainty : 2));
        });

        var xRange = dataXMax - dataXMin;
        var yRange = dataYMax - dataYMin;

        plot.xDataMin = dataXMin - 0.05 * xRange;
        plot.yDataMin =  dataYMin - 0.05 * yRange;
        plot.xDataMax = dataXMax + 0.05 * xRange;
        plot.yDataMax = dataYMax + 0.05 * yRange;
    }
};

/*
    Manages point elements in the plot based on whether or not they should be visible, and whether or not they are visible.
 */
plot.managePoints = function (data) {

    // If points should be visible...
    if (plot.getProperty("Points")) {

        // If points should be visible, but aren't...
        if (!plot.pointsVisible) {
            plot.drawPoints(data);
        }

        // If points should be visible, and already are...
        else {
            plot.updatePoints();
        }
    }

    // If points should NOT be visible, but are...
    else if (plot.pointsVisible) {
        plot.removePoints();
    }
};

/*
 Manages ellipse elements in the plot based on whether or not they should be visible, and whether or not they are visible.
 */
plot.manageEllipses = function (data) {

    // If ellipses should be visible...
    if (plot.getProperty("Ellipses")) {

        // If the ellipses simply need to be updated...
        if (plot.ellipsesVisible) {
            plot.updateEllipses();
        }

        // If ellipses need to be drawn...
        else {
            plot.drawEllipses(plot.ellipseData);
        }
    }

    // If ellipses should NOT be visible, but are...
    else if (plot.ellipsesVisible) {
        plot.removeEllipses();
    }
};

plot.manageCrosses = function (data) {

    // If crosses should be visible...
    if (plot.getProperty("Crosses")) {

        // If the crosses simply need to be updated...
        if (plot.crossesVisible) {
            plot.updateCrosses();
        }

        // If crosses need to be drawn...
        else {
            plot.drawCrosses(data);
        }
    }

    // If crosses should NOT be visible, but are...
    else if (plot.crossesVisible) {
        plot.removeCrosses();
    }

};

/*
 Manages plot feature elements in the plot based on whether or not they should be visible, and whether or not they are visible.
 */
plot.managePlotFeatures = function () {

    // If the isotope system is UPb...
    if (plot.currentIsotope == "Uranium Lead") {

        // If the concordia line should be visible...
        if (plot.getProperty("Concordia")) {

            // If the concordia line should be visible, but isn't...
            if (!plot.concordiaVisible) {
                plot.drawConcordia();
            }
            // If the concordia line should be visible, and already is...
            else {
                plot.updateConcordia();
            }
        }

        // If the concordia line should NOT be visible, and is...
        else if (plot.concordiaVisible) {
            plot.removeConcordia();
        }
    }

    // If the isotope system is not UPb, but the concordia line is visible...
    else if (plot.concordiaVisible) {
        plot.removeConcordia();
    }

    // If the isotope system is UTh...
    if (plot.currentIsotope == "Uranium Thorium" ) {

        // If the evolution matrix should be visible...
        if (plot.getProperty("Evolution")) {

            // If the evolution matrix should be visible, but isn't...
            if (!plot.evolutionMatrixVisible) {
                plot.drawEvolutionMatrix();
            }

            // If the evolution matrix should be visible, and already is...
            else {
                plot.updateEvolutionMatrix();
            }
        }

        // If the evolution matrix should NOT be visible, but is...
        else if (plot.evolutionMatrixVisible) {
            plot.removeEvolutionMatrix();
        }
    }

    // If the isotope system is not UTh, but the evolution matrix is visible...
    else if (plot.evolutionMatrixVisible) {
        plot.removeEvolutionMatrix();
    }
};

plot.removeDataFeatures = function () {
    plot.removeCrosses();
    plot.removeEllipses();
    plot.removePoints();
};

/*
    Removes all plot features from the plot.
 */
plot.removePlotFeatures = function () {
    plot.removeConcordia();
    plot.removeEvolutionMatrix();
};