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
package org.cirdles.topsoil.chart.concordia;

import Jama.Matrix;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.cirdles.topsoil.chart.Plotter;

/**
 *
 * @author John Zeringue <john.joseph.zeringue@gmail.com>
 */
public class ErrorEllipseFiller extends Plotter<ErrorEllipse, ErrorEllipseChart> {

    public ErrorEllipseFiller(ErrorEllipseChart chart) {
        super(chart);
    }
    
    @Override
    public Node plot(ErrorEllipse errorEllipse) {
        Path ellipse = new Path(new MoveTo(),
                                new CubicCurveTo(),
                                new CubicCurveTo(),
                                new CubicCurveTo(),
                                new CubicCurveTo());
        ellipse.setStroke(Color.TRANSPARENT);

        ellipse.fillProperty().bind(chart.ellipseFillColorProperty());
        ellipse.opacityProperty().bind(chart.ellipseFillOpacityProperty());

        ellipse.getStyleClass().add("error-ellipse-fill");

        Matrix controlPoints = errorEllipse.getControlPoints(chart.getConfidenceLevel());

        MoveTo moveTo = (MoveTo) ellipse.getElements().get(0);
        moveTo.setX(mapXToDisplay(controlPoints.get(0, 0)));
        moveTo.setY(mapYToDisplay(controlPoints.get(0, 1)));

        for (int i = 1; i <= 4; i++) {
            CubicCurveTo cubicCurveTo = (CubicCurveTo) ellipse.getElements().get(i);

            // set control points
            cubicCurveTo.setControlX1(mapXToDisplay(controlPoints.get(i * 3 - 2, 0)));
            cubicCurveTo.setControlY1(mapYToDisplay(controlPoints.get(i * 3 - 2, 1)));
            cubicCurveTo.setControlX2(mapXToDisplay(controlPoints.get(i * 3 - 1, 0)));
            cubicCurveTo.setControlY2(mapYToDisplay(controlPoints.get(i * 3 - 1, 1)));

            // set final point
            cubicCurveTo.setX(mapXToDisplay(controlPoints.get(i * 3, 0)));
            cubicCurveTo.setY(mapYToDisplay(controlPoints.get(i * 3, 1)));
        }

        return ellipse;
    }
}
