/**
 * Copyright @ 2022 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.vietocr;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 */
public class StatusDialogController extends Dialog {
    @FXML
    private TextArea textArea;
    
    public StatusDialogController(Window owner) throws IOException  {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StatusDialog.fxml"));
        fxmlLoader.setController(this);
        DialogPane pane = fxmlLoader.load();
        pane.getButtonTypes().addAll(ButtonType.CLOSE);
        pane.lookupButton(ButtonType.CLOSE).setVisible(false);
        setDialogPane(pane);
        initOwner(owner);
        initModality(Modality.NONE);
        setResizable(false);
        setTitle("Bulk Status");
    }
       
    public TextArea getTextArea() {
        return textArea;
    }
}