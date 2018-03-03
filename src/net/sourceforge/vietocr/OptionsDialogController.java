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
import javafx.scene.control.*;
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
    @FXML
    private ChoiceBox cbOutputFormat;
    @FXML
    private CheckBox chbEnable;
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
        if (event.getSource() == btnOK) {

            ((Stage) btnOK.getScene().getWindow()).close();
        } else if (event.getSource() == btnCancel) {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }
}
