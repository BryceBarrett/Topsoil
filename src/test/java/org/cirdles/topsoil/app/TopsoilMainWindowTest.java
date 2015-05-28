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
package org.cirdles.topsoil.app;

import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.Test;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.framework.junit.ApplicationTest;
/**
 *
 * @author John Zeringue
 */
public class TopsoilMainWindowTest extends ApplicationTest {
    
    private TopsoilMainWindow topsoilMainWindow;

    @Override
    public void start(Stage stage) throws Exception {
        topsoilMainWindow = new TopsoilMainWindow();
        
        Scene scene = new Scene(topsoilMainWindow);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testFocusOnNewTab() {
        // ensure there is at least one tab already open
        clickOn("Create Data Table");
        
        // create another tab
        // should be focused
        clickOn("Create Data Table");
        
        verifyThat(topsoilMainWindow.dataTableTabPane, (TabPane tabPane) -> {
            int numberOfTabs = tabPane.getTabs().size();
            int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
            
            return selectedIndex == numberOfTabs - 1;
        });
    }
    
}
