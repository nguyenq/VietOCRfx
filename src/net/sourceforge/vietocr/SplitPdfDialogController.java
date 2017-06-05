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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javax.swing.JOptionPane;

public class SplitPdfDialogController implements Initializable {

    @FXML
    private Button btnBrowseInput;
    @FXML
    private TextField tfInputFile;
    @FXML
    private Button btnBrowseOutput;
    @FXML
    private TextField tfOutputFile;
    @FXML
    private Button btnSplit;
    @FXML
    private Button btnCancel;
    @FXML
    private RadioButton radioButtonFiles;
    @FXML
    private RadioButton radioButtonPages;
    @FXML
    private TextField tfFrom;
    @FXML
    private TextField tfNumOfPages;
    @FXML
    private TextField tfTo;

    protected ResourceBundle bundle;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N 
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnBrowseInput) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("Open"));
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
            fc.getExtensionFilters().add(pdfFilter);
            File selectedFile = fc.showOpenDialog(btnBrowseInput.getScene().getWindow());
            if (selectedFile != null) {
                this.tfInputFile.setText(selectedFile.getPath());
            }
        } else if (event.getSource() == btnBrowseOutput) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("Save"));
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
            fc.getExtensionFilters().add(pdfFilter);
            File selectedFile = fc.showSaveDialog(btnBrowseOutput.getScene().getWindow());
            if (selectedFile != null) {
                this.tfOutputFile.setText(selectedFile.getPath());

                if (!this.tfOutputFile.getText().endsWith(".pdf")) {
                    this.tfOutputFile.setText(this.tfOutputFile.getText() + ".pdf");
                }
            }
        } else if (event.getSource() == btnSplit) {
            SplitPdfArgs args = new SplitPdfArgs();
            args.setInputFilename(this.tfInputFile.getText());
            args.setOutputFilename(this.tfOutputFile.getText());
            args.setFromPage(this.tfFrom.getText());
            args.setToPage(this.tfTo.getText());
            args.setNumOfPages(this.tfNumOfPages.getText());
            args.setPages(this.radioButtonPages.isSelected());

            if (!new File(args.getInputFilename()).exists()) {
//                JOptionPane.showMessageDialog(this, bundle.getString("File_not_exist"), bundle.getString("Error"), JOptionPane.ERROR_MESSAGE);
            } else if (args.getInputFilename().length() > 0 && args.getOutputFilename().length() > 0
                    && ((this.radioButtonPages.isSelected() && args.getFromPage().length() > 0)
                    || (this.radioButtonFiles.isSelected() && args.getNumOfPages().length() > 0))) {

                Pattern regexNums = Pattern.compile("^\\d+$");

                if ((this.radioButtonPages.isSelected() && regexNums.matcher(args.getFromPage()).matches() && (args.getToPage().length() > 0 ? regexNums.matcher(args.getToPage()).matches() : true)) || (this.radioButtonFiles.isSelected() && regexNums.matcher(args.getNumOfPages()).matches())) {
//                    this.args = args;
//                    actionSelected = JOptionPane.OK_OPTION;
//                    this.setVisible(false);
                } else {
//                    JOptionPane.showMessageDialog(this, bundle.getString("Input_invalid"), bundle.getString("Error"), JOptionPane.ERROR_MESSAGE);
//                    actionSelected = JOptionPane.DEFAULT_OPTION;
                }
            } else {
//                JOptionPane.showMessageDialog(this, bundle.getString("Input_incomplete"), bundle.getString("Error"), JOptionPane.ERROR_MESSAGE);
//                actionSelected = JOptionPane.DEFAULT_OPTION;
            }

//            MenuToolsController.getInstance().splitPdf(args);
        } else if (event.getSource() == btnCancel) {
            btnCancel.getScene().getWindow().hide();
        }
    }
}
