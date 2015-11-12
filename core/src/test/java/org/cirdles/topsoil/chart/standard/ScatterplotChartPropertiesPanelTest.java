/*
 * Copyright 2015 CIRDLES.
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
package org.cirdles.topsoil.chart.standard;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

/**
 * Created by johnzeringue on 11/9/15.
 */
public class ScatterplotChartPropertiesPanelTest extends ApplicationTest {

    private ScatterplotChart chart;

    @Override
    public void start(Stage stage) throws Exception {
        chart = new ScatterplotChart();

        Scene scene = new Scene((Parent) chart.displayAsNode());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testConstructor() {
        new ScatterplotChartPropertiesPanel(chart);
    }
}
