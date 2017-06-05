/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.vietocr;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Quan
 */
public class OptionsDialogController implements Initializable {

    @FXML
    private Button btnOK;
    @FXML
    private Button btnCancel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnOK) {

            ((Stage) btnOK.getScene().getWindow()).close();
        } else if (event.getSource() == btnCancel) {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }
}
