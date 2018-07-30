/**
 * Copyright @ 2008 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.vietocr;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class BulkDialogController implements Initializable {
    @FXML
    private Button btnRun;
    @FXML
    private Button btnCancel;
    @FXML
    private ComboBox cbOutputFormat;
    @FXML
    private CheckBox chbDeskew;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbOutputFormat.getItems().addAll("Text", "HTML", "PDF");
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnRun) {
            ((Stage) btnRun.getScene().getWindow()).close();
        } else if (event.getSource() == btnCancel) {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }
   
}
