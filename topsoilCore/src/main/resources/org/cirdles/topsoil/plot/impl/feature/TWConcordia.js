
if (plot.twconcordiaVisible == null) {
    plot.twconcordiaVisible = false;
}

plot.drawTWConcordia = function() {

    // Removes the concordia before redrawing it
    if (plot.twconcordiaVisible) {
        plot.removeTWConcordia();
    }

    plot.twconcordiaGroup = plot.area.clipped.insert("g", ".dataGroup")
        .attr("class", "twconcordiaGroup");

    // initialize the concordia envelope
    plot.twconcordiaGroup.append("path")
        .attr("class", "twuncertaintyEnvelope")
        .attr("stroke", "none")
        .attr("shape-rendering", "geometricPrecision");

    // initialize the concordia
    plot.twconcordiaGroup.append("path")
        .attr("class", "twconcordia")
        .attr("fill", "none")
        .attr("shape-rendering", "geometricPrecision");

    plot.twconcordiaVisible = true;
    plot.updateTWConcordia();
};

plot.updateTWConcordia = function() {
    if (plot.twconcordiaVisible) {

        initializeWasserburg({
            LAMBDA_235: plot.lambda.U235,
            LAMBDA_238: plot.lambda.U238,
            R238_235S: plot.lambda.R238_235S
        });


        var minX_T = (Math.log1p(plot.xAxisScale.domain()[1]) - Math.log(plot.xAxisScale.domain()[1]))
            / plot.lambda.U238;
        var minY_T = calculateDate(plot.yAxisScale.domain()[0], 0.0);
        var minT = Math.max(minX_T, minY_T);


        var maxX_T = (Math.log1p(plot.xAxisScale.domain()[0]) - Math.log(plot.xAxisScale.domain()[0]))
            / plot.lambda.U238;
        var maxY_T = calculateDate(plot.yAxisScale.domain()[1], 0.0);
        var maxT = Math.min(maxX_T, maxY_T);

        // This the value that will split the concordia line
        var n = 5;
        var segmentSize = (maxT - minT) / n;


        // build the concordia line
        plot.twconcordia = plot.twconcordiaGroup.select(".twconcordia")

            .attr("d", function () {
                var path = []
                moveTo(path, wasserburg(minT).scaleBy(plot.xAxisScale, plot.yAxisScale));

                var trackingSegVal = minT;
                for (var i = 0; i < n; i++){

                    var nextSegVal = trackingSegVal + segmentSize;

                    var p1 = wasserburg(trackingSegVal);
                    var p2 = wasserburg(nextSegVal);
                    var m1 = wasserburg.y.primeResX(p1[0]);
                    var m2 = wasserburg.y.primeResX(p2[0]);

                    alert(m1);
                    alert(m2);
                    var controlPointX = ((m1 * p1[0]) - (m2 * p2[0]) - p1[1] + p2[1]) / (m1 - m2);
                    var controlPointY = ((m1 * ((m2 * (p1[0] - p2[0])) + p2[1])) - (m2 * p1[1])) / (m1 - m2);
                    var controls = new Vector2D(controlPointX, controlPointY);

                    plot.cubicBezier(path, p1.scaleBy(plot.xAxisScale, plot.yAxisScale),
                        controls.scaleBy(plot.xAxisScale, plot.yAxisScale),
                        p2.scaleBy(plot.xAxisScale, plot.yAxisScale));

                    //plot.cubicBezier(path, p1, controls, p2);
                    alert("StepVal: " + i);
                    alert("p1X: " + p1[0] + " p1Xscale: " + p1.scaleBy(plot.xAxisScale, plot.yAxisScale)[0]);
                    alert("controlsX: " + controls[0] + " controlsXscale: " + controls.scaleBy(plot.xAxisScale, plot.yAxisScale)[0]);
                    alert("p2X: " + p2[0] + " p2Xscale: " + p2.scaleBy(plot.xAxisScale, plot.yAxisScale)[0]);

                    trackingSegVal = nextSegVal;



                }
                return path.join("");
                /*var approximateSegment = function (path, minT, maxT) {
                    var p1 = wasserburg(minT).plus(
                        wasserburg.prime(minT).times((maxT - minT) / 3))
                        .scaleBy(plot.xAxisScale, plot.yAxisScale);
                    var p2 = wasserburg(maxT).minus(
                        wasserburg.prime(maxT).times((maxT - minT) / 3))
                        .scaleBy(plot.xAxisScale, plot.yAxisScale);
                    var p3 = wasserburg(maxT).scaleBy(plot.xAxisScale, plot.yAxisScale);

                    // append a cubic bezier to the path
                    plot.cubicBezier(path, p1, p2, p3);
                };

                // initialize path
                var path = [];
                moveTo(path, wasserburg(minT).scaleBy(plot.xAxisScale, plot.yAxisScale));

                // determine the step size using the number of pieces
                var pieces = 60;
                var stepSize = (maxT - minT) / pieces;

                // build the pieces
                for (var i = 0; i < pieces; i++) {
                    approximateSegment(path, minT + stepSize * i, minT + stepSize * (i + 1));
                }

                return path.join("");*/
            })
            .attr("stroke", plot.getProperty(Property.CONCORDIA_LINE_FILL))
            .attr("stroke-width", 2);

        if (plot.getProperty(Property.CONCORDIA_ENVELOPE)) {
            plot.twconcordiaGroup.select(".twuncertaintyEnvelope")
                .attr("d", function () {
                    var path = []
                    moveTo(path, wasserburg(minT).scaleBy(plot.xAxisScale, plot.yAxisScale));

                    var trackingSegVal = minT;
                    for (var i = 0; i < n; i++){

                        var nextSegVal = trackingSegVal + segmentSize;

                        var p1 = wasserburg.upperEnvelope(trackingSegVal);
                        var p2 = wasserburg.upperEnvelope(nextSegVal);
                        var m1 = wasserburg.y.primeResX(p1[0]);
                        var m2 = wasserburg.y.primeResX(p2[0]);
                        var controlPointX = ((m1 * p1[0]) - (m2 * p2[0]) - p1[1] + p2[1]) / (m1 - m2);
                        var controlPointY = ((m1 * ((m2 * (p1[0] - p2[0])) + p2[1])) - (m2 * p1[1])) / (m1 - m2);
                        var controls = new Vector2D(controlPointX, controlPointY);

                        plot.cubicBezier(path, p1.scaleBy(plot.xAxisScale, plot.yAxisScale),
                            controls.scaleBy(plot.xAxisScale, plot.yAxisScale),
                            p2.scaleBy(plot.xAxisScale, plot.yAxisScale));
                    }

                    moveTo(path, wasserburg(minT).scaleBy(plot.xAxisScale, plot.yAxisScale));

                    var trackingSegVal = minT;
                    for (var i = 0; i < n; i++){

                        var nextSegVal = trackingSegVal + segmentSize;

                        var p1 = wasserburg.lowerEnvelope(trackingSegVal);
                        var p2 = wasserburg.lowerEnvelope(nextSegVal);
                        var m1 = wasserburg.y.primeResX(p1[0]);
                        var m2 = wasserburg.y.primeResX(p2[0]);
                        var controlPointX = ((m1 * p1[0]) - (m2 * p2[0]) - p1[1] + p2[1]) / (m1 - m2);
                        var controlPointY = ((m1 * ((m2 * (p1[0] - p2[0])) + p2[1])) - (m2 * p1[1])) / (m1 - m2);
                        var controls = new Vector2D(controlPointX, controlPointY);

                        plot.cubicBezier(path, p1.scaleBy(plot.xAxisScale, plot.yAxisScale),
                            controls.scaleBy(plot.xAxisScale, plot.yAxisScale),
                            p2.scaleBy(plot.xAxisScale, plot.yAxisScale));

                    }

                /*.attr("d", function () {
                    var approximateUpperSegment = function (path, minT, maxT) {
                        var p1 = wasserburg.upperEnvelope(minT).plus(
                            wasserburg.prime(minT).times((maxT - minT) / 3))
                            .scaleBy(plot.xAxisScale, plot.yAxisScale);
                        var p2 = wasserburg.upperEnvelope(maxT).minus(
                            wasserburg.prime(maxT).times((maxT - minT) / 3))
                            .scaleBy(plot.xAxisScale, plot.yAxisScale);
                        var p3 = wasserburg.upperEnvelope(maxT).scaleBy(plot.xAxisScale, plot.yAxisScale);

                        // append a cubic bezier to the path
                        plot.cubicBezier(path, p1, p2, p3);
                    };

                    var approximateLowerSegment = function (path, minT, maxT) {
                        var p1 = wasserburg.lowerEnvelope(minT).plus(
                            wasserburg.prime(minT).times((maxT - minT) / 3))
                            .scaleBy(plot.xAxisScale, plot.yAxisScale);
                        var p2 = wasserburg.lowerEnvelope(maxT).minus(
                            wasserburg.prime(maxT).times((maxT - minT) / 3))
                            .scaleBy(plot.xAxisScale, plot.yAxisScale);
                        var p3 = wasserburg.lowerEnvelope(maxT).scaleBy(plot.xAxisScale, plot.yAxisScale);

                        // append a cubic bezier to the path
                        plot.cubicBezier(path, p1, p2, p3);
                    };

                    var minT = Math.min(
                        newtonMethod(wasserburg.upperEnvelope.x, plot.xAxisScale.domain()[0]),
                        newtonMethod(wasserburg.upperEnvelope.y, plot.yAxisScale.domain()[0]));

                    var maxT = Math.min(
                        newtonMethod(wasserburg.upperEnvelope.x, plot.xAxisScale.domain()[1]),
                        newtonMethod(wasserburg.upperEnvelope.y, plot.yAxisScale.domain()[1]));


                    // initialize path
                    var path = [];
                    moveTo(path, wasserburg.upperEnvelope(minT).scaleBy(plot.xAxisScale, plot.yAxisScale));

                    // determine the step size using the number of pieces
                    var pieces = 30;
                    var stepSize = (maxT - minT) / pieces;

                    // build the pieces
                    for (var i = 0; i < pieces; i++) {
                        approximateUpperSegment(path, minT + stepSize * i, minT + stepSize * (i + 1));
                    }

                    lineTo(path, [plot.xAxisScale.range()[1], plot.yAxisScale.range()[1]]);

                    minT = Math.min(
                        newtonMethod(wasserburg.lowerEnvelope.x, plot.xAxisScale.domain()[0]),
                        newtonMethod(wasserburg.lowerEnvelope.y, plot.yAxisScale.domain()[0]));

                    maxT = Math.min(
                        newtonMethod(wasserburg.lowerEnvelope.x, plot.xAxisScale.domain()[1]),
                        newtonMethod(wasserburg.lowerEnvelope.y, plot.yAxisScale.domain()[1]));

                    lineTo(path, wasserburg.lowerEnvelope(maxT).scaleBy(plot.xAxisScale, plot.yAxisScale));

                    stepSize = (maxT - minT) / pieces;

                    // build the pieces
                    for (i = 0; i < pieces; i++) {
                        approximateLowerSegment(path, maxT - stepSize * i, maxT - stepSize * (i + 1));
                    }

                    lineTo(path, [plot.xAxisScale.range()[0], plot.yAxisScale.range()[0]]);
                    close(path);

                    return path.join("");*/
                })
                .attr("fill", plot.getProperty(Property.CONCORDIA_ENVELOPE_FILL));
        }

        plot.t.domain([minT, maxT]);

        var concordiaTicks;
        (concordiaTicks = plot.concordiaTicks = plot.twconcordiaGroup.selectAll(".concordiaTicks")
            .data(plot.t.ticks()))
            .enter()
            .append("circle")
            .attr("class", "concordiaTicks")
            .attr("r", 5)
            .style('stroke-width', 2)
            .style("stroke", "black")
            .style("fill", "white");;

        concordiaTicks
            .attr("cx", function (t) {
                return plot.xAxisScale(wasserburg.x(t));
            })
            .attr("cy", function (t) {
                return plot.yAxisScale(wasserburg.y(t));
            });

        var tickLabels;
        (tickLabels = plot.tickLabels = plot.twconcordiaGroup.selectAll(".tickLabel")
            .data(plot.t.ticks()))
            .enter()
            .append("text")
            .attr("font-family", "sans-serif")
            .attr("class", "tickLabel");

        tickLabels
            .attr("x", function (t) {
                return plot.xAxisScale(wasserburg.x(t)) + 12;
            })
            .attr("y", function (t) {
                return plot.yAxisScale(wasserburg.y(t)) + 5;
            })
            .text(function (t) {
                return t / 1000000;
            });

        plot.concordiaTicks.exit().remove();
        plot.tickLabels.exit().remove();
    }
};

plot.removeTWConcordia = function() {
    if (plot.twconcordiaVisible) {
        plot.twconcordiaGroup.remove();
        plot.twconcordiaVisible = false;
    }
};