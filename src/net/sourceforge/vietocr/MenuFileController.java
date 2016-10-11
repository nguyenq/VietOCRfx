/**
 * Copyright @ 2016 Quan Nguyen
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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class MenuFileController implements Initializable {

    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem miOpen;
    @FXML
    private MenuItem miSave;
    @FXML
    private MenuItem miSaveAs;
    @FXML
    private MenuItem miScan;
    @FXML
    private MenuItem miExit;
    @FXML
    protected MenuRecentFilesController menuRecentFilesController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == miOpen) {
            ((Button) menuBar.getScene().lookup("#btnOpen")).fire();
        } else if (event.getSource() == miSave) {
            ((Button) menuBar.getScene().lookup("#btnSave")).fire();
        } else if (event.getSource() == miSaveAs) {
            //MainController.getInstance().saveFileDlg();
        } else if (event.getSource() == miScan) {

        } else if (event.getSource() == miExit) {
            GuiController.getInstance().quit();
        }
    }

    public void savePrefs() {
        menuRecentFilesController.savePrefs();
    }
}
