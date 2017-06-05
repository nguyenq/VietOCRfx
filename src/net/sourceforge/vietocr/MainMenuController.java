/**
 * Copyright @ 2016 Quan Nguyen
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
import javafx.scene.control.MenuBar;


public class MainMenuController implements Initializable {

    @FXML
    private MenuBar menuBar;
    @FXML
    protected MenuFileController menuFileController;
    @FXML
    protected MenuCommandController menuCommandController;
    @FXML
    protected MenuImageController menuImageController;
    @FXML
    protected MenuSettingsController menuSettingsController;
    @FXML
    protected MenuToolsController menuToolsController;
    @FXML
    private MenuFormatController menuFormatController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menuFileController.setMenuBar(menuBar);
        menuCommandController.setMenuBar(menuBar);
        menuImageController.setMenuBar(menuBar);
        menuSettingsController.setMenuBar(menuBar);
        menuFormatController.setMenuBar(menuBar);
        menuToolsController.setMenuBar(menuBar);
    }

    @FXML
    private void handleAction(ActionEvent event) {

    }

    public void savePrefs() {
        menuFileController.savePrefs();
        //menuEditController.savePrefs();
        menuImageController.savePrefs();
        menuFormatController.savePrefs();
        menuSettingsController.savePrefs();
        menuToolsController.savePrefs();
    }
}
