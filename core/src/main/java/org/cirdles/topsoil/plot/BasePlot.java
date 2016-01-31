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
package org.cirdles.topsoil.plot;

import org.cirdles.topsoil.dataset.Dataset;

import java.util.Optional;

/**
 * A partial implementation of {@link Plot} that stores and retrieves set data.
 *
 * @author John Zeringue
 */
public abstract class BasePlot implements Plot {

    private Optional<Dataset> dataset = Optional.empty();
    private Optional<PlotContext> variableBindings = Optional.empty();

    @Override
    public Optional<Dataset> getDataset() {
        return dataset;
    }

    @Override
    public Optional<PlotContext> getContext() {
        return variableBindings;
    }

    @Override
    public void setContext(PlotContext plotContext) {
        this.dataset = Optional.ofNullable(plotContext.getDataset());
        this.variableBindings = Optional.ofNullable(plotContext);
    }

}
