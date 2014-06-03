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
package org.cirdles.topsoil.chart;

import static java.lang.Math.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.chart.XYChart;
import org.cirdles.topsoil.Tools;

/**
 * <p>
 * A <code>XYChart</code>, supporting to be moved by drag and drop, in which only numbers are accepted.</p>
 * <p>
 * In addition to supporting drag and drop, it also support zoom via right clicking and scrolling.
 *
 * @author John
 * @see XYChart
 */
public abstract class NumberChart extends XYChart<Number, Number> {

    public final NumberAxis xAxis;
    public final NumberAxis yAxis;
    private final Rectangle dragSelect;

    private static final int MINSIDE_DRAGWINDOW = 3;

    private final BooleanProperty lockToQ1 = new SimpleBooleanProperty(true);

    public NumberChart() {
        super(new NumberAxis(), new NumberAxis());
        setData(FXCollections.<Series<Number, Number>>observableArrayList());

        xAxis = (NumberAxis) getXAxis();
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(Tools.DYNAMIC_STRING_CONVERTER);

        yAxis = (NumberAxis) getYAxis();
        yAxis.setForceZeroInRange(false);
        yAxis.setTickLabelFormatter(Tools.DYNAMIC_STRING_CONVERTER);

        dragSelect = new Rectangle();
        dragSelect.getStyleClass().add("drag-select");
        dragSelect.setVisible(false);
        getChartChildren().add(dragSelect);

        final DoubleProperty mousePressedX = new SimpleDoubleProperty();
        final DoubleProperty mousePressedY = new SimpleDoubleProperty();
        final DoubleProperty mouseDraggedX = new SimpleDoubleProperty();
        final DoubleProperty mouseDraggedY = new SimpleDoubleProperty();

        setOnKeyReleased((KeyEvent keyEvent) -> {
            System.out.println("Hello!");
        });

        setOnKeyTyped((KeyEvent keyEvent) -> {
            System.out.printf("'%s'\n", keyEvent.getCharacter());
            if (keyEvent.getCharacter().equals(" ")) {
                xAxis.setAutoRanging(true);
                yAxis.setAutoRanging(true);

                updateAxisRange();
            }
        });

        setOnMouseDragged((MouseEvent mouseEvent) -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                shiftPlotWindowConstraint((mouseDraggedX.get() - mouseEvent.getX()) / xAxis.getWidth() * getXRange(),
                                          -(mouseDraggedY.get() - mouseEvent.getY()) / yAxis.getHeight() * getYRange());

                mouseDraggedX.set(mouseEvent.getX());
                mouseDraggedY.set(mouseEvent.getY());
            } else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                double mouseX = mouseEvent.getX();
                double mouseY = mouseEvent.getY();

                double minX = min(mouseX, mousePressedX.get());
                double maxX = max(mouseX, mousePressedX.get());
                double minY = min(mouseY, mousePressedY.get());
                double maxY = max(mouseY, mousePressedY.get());

                dragSelect.setX(minX);
                dragSelect.setY(minY);
                dragSelect.setWidth(maxX - minX);
                dragSelect.setHeight(maxY - minY);

                dragSelect.setVisible(true);
            }
        });

        setOnMousePressed((MouseEvent mouseEvent) -> {
            mousePressedX.set(mouseEvent.getX());
            mousePressedY.set(mouseEvent.getY());

            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                ((Node) mouseEvent.getSource()).setCursor(Cursor.HAND);

                mouseDraggedX.set(mouseEvent.getX());
                mouseDraggedY.set(mouseEvent.getY());
            }
        });

        setOnMouseReleased((MouseEvent mouseEvent) -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                ((Node) mouseEvent.getSource()).setCursor(Cursor.DEFAULT);
            } else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                dragSelect.setVisible(false);

                double minXValue_ondisplay = dragSelect.getX() - xAxis.getLayoutX();
                double maxXValue_ondisplay = dragSelect.getX() + dragSelect.getWidth() - xAxis.getLayoutX();
                double minYValue_ondisplay = dragSelect.getY() + dragSelect.getHeight() - yAxis.getLayoutY();
                double maxYValue_ondisplay = dragSelect.getY() - yAxis.getLayoutY();

                double minXValue = xAxis.getValueForDisplay(minXValue_ondisplay).doubleValue();
                double maxXValue = xAxis.getValueForDisplay(maxXValue_ondisplay).doubleValue();
                double minYValue = yAxis.getValueForDisplay(minYValue_ondisplay).doubleValue();
                double maxYValue = yAxis.getValueForDisplay(maxYValue_ondisplay).doubleValue();

                if (abs(maxXValue_ondisplay - minXValue_ondisplay) > MINSIDE_DRAGWINDOW && abs(maxYValue_ondisplay - minYValue_ondisplay) > MINSIDE_DRAGWINDOW) {
                    setPlotWindow(minXValue, maxXValue, minYValue, maxYValue);
                }
                
                dragSelect.setX(0);
                dragSelect.setY(0);
                dragSelect.setWidth(0);
                dragSelect.setHeight(0);
            }
        });

        setOnScroll((ScrollEvent scrollEvent) -> {
            double zoomX = xAxis.getValueForDisplay(scrollEvent.getX() - xAxis.getLayoutX()).doubleValue();
            double zoomY = yAxis.getValueForDisplay(scrollEvent.getY() - yAxis.getLayoutY()).doubleValue();

            shiftPlotWindowFree(-zoomX, -zoomY);
            scalePlotWindow(1 - scrollEvent.getDeltaY() / 400);
            shiftPlotWindowFree(zoomX, zoomY);

            if (lockToQ1.get()) {
                moveBackToQ1();
            }
        });

        lockToQ1.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            moveBackToQ1();
        });

    }

    private double getXRange() {
        return xAxis.getUpperBound() - xAxis.getLowerBound();
    }

    private double getYRange() {
        return yAxis.getUpperBound() - yAxis.getLowerBound();
    }

    protected final void setPlotWindow(double minXValue, double maxXValue, double minYValue, double maxYValue) {
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);

        xAxis.setLowerBound(minXValue);
        xAxis.setUpperBound(maxXValue);
        yAxis.setLowerBound(minYValue);
        yAxis.setUpperBound(maxYValue);
    }

    /**
     * Some functions need to be able to move the plot window anywhere for a very short amount of time (like the scaling
     * operation) However, the user input that move directly the window must be sometime constrain. To do so, we use the
     * function.
     *
     * @param xAmount
     * @param yAmount
     */
    protected final void shiftPlotWindowConstraint(double xAmount, double yAmount) {
        //Determining bounds
        if (lockToQ1.get()) {
            if (xAxis.getLowerBound() + xAmount < 0) {
                xAmount = -xAxis.getLowerBound();
            }

            if (yAxis.getLowerBound() + yAmount < 0) {
                yAmount = -yAxis.getLowerBound();
            }
        }

        shiftPlotWindowFree(xAmount, yAmount);
    }

    /**
     * Mode the plot window from a certain number of pixel in two directions.
     *
     * @param xAmount
     * @param yAmount
     */
    protected final void shiftPlotWindowFree(double xAmount, double yAmount) {
        setPlotWindow(xAxis.getLowerBound() + xAmount,
                      xAxis.getUpperBound() + xAmount,
                      yAxis.getLowerBound() + yAmount,
                      yAxis.getUpperBound() + yAmount);
    }

    protected final void scalePlotWindow(double factor) {
        setPlotWindow(xAxis.getLowerBound() * factor,
                      xAxis.getUpperBound() * factor,
                      yAxis.getLowerBound() * factor,
                      yAxis.getUpperBound() * factor);
    }

    @Override
    public ObservableList<Node> getChartChildren() {
        return super.getChartChildren();
    }

    public BooleanProperty lockToQ1Property() {
        return lockToQ1;
    }

    private void moveBackToQ1() {
        if (xAxis.getLowerBound() < 0) {
            shiftPlotWindowFree(-xAxis.getLowerBound(), 0);
        }

        if (yAxis.getLowerBound() < 0) {
            shiftPlotWindowFree(0, -yAxis.getLowerBound());
        }
    }

    public void resetView() {
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);

        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
    }
}
