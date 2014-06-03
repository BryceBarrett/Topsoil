/*
 * Copyright 2014 pfif.
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
package org.cirdles.jfxutils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.Property;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.cirdles.topsoil.Tools;

/**
 *
 * @author pfif
 */
public class NumberField extends TextField {
    
    private final DoubleProperty number = new DoublePropertyBase() {

        @Override
        public Object getBean() {
            return NumberField.this;
        }

        @Override
        public String getName() {
            return "number";
        }
    };
    
    public DoubleProperty numberProperty() {
        return number;
    }
    
    public double getNumber() {
        return number.get();
    }
    
    public void setNumber(double value) {
        number.set(value);
    }

    /**
     * Create a new number field with the default converter for converting from the number to text and vice versa.
     */
    public NumberField() {
        this(Tools.DYNAMIC_STRING_CONVERTER);
    }

    /**
     * Create a new number field that uses the given converter to equate its text contents to a decimal number.
     * 
     * @param converter 
     */
    public NumberField(StringConverter<Number> converter) {
        Bindings.bindBidirectional(textProperty(), number, converter);
    }
}
