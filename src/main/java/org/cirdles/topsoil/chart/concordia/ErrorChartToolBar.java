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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.DragEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import org.cirdles.jfxutils.NodeToSVGConverter;
import org.cirdles.jfxutils.NumberField;

/**
 * A ToolBar for use with ErrorCharts.
 * 
 * @author John Zeringue <john.joseph.zeringue@gmail.com>
 */

public class ErrorChartToolBar extends ToolBar {
    
    public ErrorChartToolBar(ConcordiaChart chart) {
        
        ErrorEllipseStyleContainer eeStyleAccessor = (ErrorEllipseStyleContainer) chart;
        ConcordiaChartStyleAccessor ccStyleAccessor = (ConcordiaChartStyleAccessor) chart;
        
        //Adding the buttons
        Button exportToSVG = new Button("Export to SVG");
        exportToSVG.setOnAction((ActionEvent event) -> {
            //start_turnNodeToText(chart); (Tool for developper/ should always be commented before commit)
            NodeToSVGConverter converter = new NodeToSVGConverter();
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export to SVG");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fileChooser.showSaveDialog(getScene().getWindow());
            
            converter.convert(chart, file);
        });
        
        //Color Picker for filling and stroking the errorellipses
        ColorPicker colorPickerStroke = new ColorPicker();
        colorPickerStroke.valueProperty().bindBidirectional(eeStyleAccessor.ellipseOutlineColorProperty());
        
        ColorPicker colorPickerFill = new ColorPicker();
        colorPickerFill.valueProperty().bindBidirectional(eeStyleAccessor.ellipseFillColorProperty());
        
        CheckBox showOutlineCheckBox = new CheckBox();
        showOutlineCheckBox.selectedProperty().bindBidirectional(eeStyleAccessor.ellipseOutlineShownProperty());
        
        Slider slider_opacity = new Slider(0,1,0.5);
        slider_opacity.valueProperty().bindBidirectional(eeStyleAccessor.ellipseFillOpacityProperty());
        
        NumberField tickXnf = new NumberField(ccStyleAccessor.axisXAnchorTickProperty());
        NumberField tickYnf = new NumberField(ccStyleAccessor.axisYAnchorTickProperty());
        
        NumberField tickUnitXnf = new NumberField(ccStyleAccessor.axisXTickUnitProperty());
        NumberField tickUnitYnf = new NumberField(ccStyleAccessor.axisYTickUnitProperty());
        
        CheckBox autoTickCheckBox = new CheckBox();
        autoTickCheckBox.selectedProperty().bindBidirectional(ccStyleAccessor.axisAutoTickProperty());
        
        getItems().add(exportToSVG);
        getItems().add(colorPickerStroke);
        getItems().add(colorPickerFill);
        getItems().add(showOutlineCheckBox);
        getItems().add(tickXnf);
        getItems().add(tickUnitXnf);
        getItems().add(tickYnf);
        getItems().add(tickUnitYnf);
        getItems().add(autoTickCheckBox);
        getItems().add(slider_opacity);
    }
    
    private void turnNodeToText(Node n, PrintWriter pw, String prefix){
        //pw.println(prefix+"--");
        pw.println(prefix+n.getClass().getName());
        /*pw.println(prefix+"Layout X: "+n.getLayoutX());
        pw.println(prefix+"Layout Y: "+n.getLayoutY());
        pw.println(prefix+"Transform X: "+n.getTranslateX());
        pw.println(prefix+"Transform Y: "+n.getTranslateY());
        pw.println(prefix+"Transform Z: "+n.getTranslateZ());
        pw.println(prefix+"LocalToParent: "+n.getLocalToParentTransform());
        pw.println(prefix+"LocalToScene: "+n.getLocalToSceneTransform());
        if(n instanceof Label){
            Label l = (Label) n;
            pw.println(prefix+"Text: "+l.getText());
            
        }
        if(!n.getTransforms().isEmpty()){
            pw.println(prefix+"Transforms :");
            for(Transform t : n.getTransforms()){
                pw.println(prefix+"..."+t.toString());
            }
        }
        pw.println(prefix+"--");*/
        
        if(n instanceof Parent){
            Parent parent = (Parent) n;
            String new_prefix = prefix+"\t";
            for(Node new_n : parent.getChildrenUnmodifiable()){
               turnNodeToText(new_n, pw, new_prefix); 
            }
        }
    }
    
    private void start_turnNodeToText(Node n){
        PrintWriter pw = null;
        try {
            File f = new File("/Users/pfif/Documents/beboptango");
            pw = new PrintWriter(f);
            turnNodeToText(n, pw, "");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ErrorChartToolBar.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }
}
