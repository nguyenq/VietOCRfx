/**
 * Copyright @ 2016 Quan Nguyen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package net.sourceforge.vietocr;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

public class ChangeCaseDialogController implements Initializable {

    @FXML
    private RadioButton rbS;
    @FXML
    private RadioButton rbL;
    @FXML
    private RadioButton rbU;
    @FXML
    private RadioButton rbT;
    @FXML
    private Button btnChange;
    @FXML
    private Button btnClose;

    private String selectedCase;

    private final static Logger logger = Logger.getLogger(ChangeCaseDialogController.class.getName());

    @FXML
    private ToggleGroup radioGroup;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Can set in Scene Builder?
        rbS.setUserData("Sentence_case");
        rbL.setUserData("lowercase");
        rbU.setUserData("UPPERCASE");
        rbT.setUserData("Title_Case");

        radioGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
            if (radioGroup.getSelectedToggle() != null) {
                selectedCase = radioGroup.getSelectedToggle().getUserData().toString();
            }
        });
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnChange) {
            MenuFormatController.getInstance().changeCase(selectedCase);
        } else if (event.getSource() == btnClose) {
            btnClose.getScene().getWindow().hide();
        }
    }

    /**
     * Sets the selected case.
     *
     * @param selectedCase
     */
    public void setSelectedCase(String selectedCase) {
        this.selectedCase = selectedCase;

        for (Toggle bt : radioGroup.getToggles()) {
            if (selectedCase.equals(bt.getUserData())) {
                bt.setSelected(true);
                break;
            }
        }
    }

    /**
     * Gets the selected case
     *
     * @return String selectedCase
     */
    public String getSelectedCase() {
        return selectedCase;
    }

}
