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

package org.cirdles.topsoil;

import java.util.function.Consumer;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 * Shortcut tools used anywhere in the program.
 * @author pfif
 */
public class TinkeringTools {
       public static void yesNoPrompt(String message, Consumer<Boolean> callback) {
        Action response = Dialogs.create()
                .title(Topsoil.APP_NAME)
                .message(message)
                .showConfirm();

        if (response != Dialog.Actions.CANCEL) {
            callback.accept(response == Dialog.Actions.YES);
        }
    } 
}
