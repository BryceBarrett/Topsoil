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
package org.cirdles.topsoil.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.cirdles.topsoil.dataset.Dataset;
import org.cirdles.topsoil.dataset.field.Field;

public class SimpleVariableContext implements VariableContext {

    private final Collection<VariableBinding> bindings = new ArrayList<>();
    private final Dataset dataset;

    public SimpleVariableContext(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Collection<Variable> getVariables() {
        return getBindings().stream()
                .map(VariableBinding::getVariable)
                .collect(Collectors.toList());
    }
    
    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public Collection<VariableBinding> getBindings() {
        return bindings;
    }

    @Override
    public <T> void addBinding(Variable<T> variable, Field<T> field, VariableFormat<T> format) {
        bindings.add(new SimpleVariableBinding(variable, field, format, this));
    }

}
