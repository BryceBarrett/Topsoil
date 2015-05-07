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
package org.cirdles.topsoil.app.chart;

import java.util.List;
import javafx.scene.control.ButtonType;
import static javafx.scene.control.ButtonType.OK;
import javafx.scene.control.Dialog;
import org.cirdles.topsoil.chart.Chart;
import org.cirdles.topsoil.chart.Variable;
import org.cirdles.topsoil.chart.VariableContext;
import org.cirdles.topsoil.data.Dataset;

public class VariableBindingDialog extends Dialog<VariableContext> {

    private final List<Variable> variables;
    private final Dataset dataset;

    public VariableBindingDialog(List<Variable> variables, Dataset dataset) {
        this.variables = variables;
        this.dataset = dataset;

        configureThis();
    }

    private void configureThis() {
        setDialogPane(new VariableBindingDialogPane(variables, dataset));
        setResizable(false);
        setResultConverter(this::convertResult);
    }

    VariableContext extractVariableContextFromDialogPane() {
        return ((VariableBindingDialogPane) getDialogPane())
                .getVariableContext();
    }

    VariableContext convertResult(ButtonType type) {
        if (type == OK) {
            return extractVariableContextFromDialogPane();
        } else {
            return null;
        }
    }

}
