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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;

public class SplitPdfDialogController extends Dialog<SplitPdfArgs> implements Initializable {

    @FXML
    private Button btnBrowseInput;
    @FXML
    private TextField tfInputFile;
    @FXML
    private Button btnBrowseOutput;
    @FXML
    private TextField tfOutputFile;
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
    @FXML
    private ButtonType okButtonType;
    @FXML
    private ButtonType cancelButtonType;

    private SplitPdfArgs args;
    private File currentDirectory;

    protected ResourceBundle bundle;

    public SplitPdfDialogController(Window owner) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SplitPdfDialog.fxml"));
        fxmlLoader.setController(this);
        DialogPane pane = fxmlLoader.load();
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        setTitle("Split PDF");
        setDialogPane(pane);
        args = new SplitPdfArgs();

        Button okButton = (Button) getDialogPane().lookupButton(okButtonType);
        okButton.addEventFilter(ActionEvent.ACTION, e -> {
            args.setInputFilename(this.tfInputFile.getText());
            args.setOutputFilename(this.tfOutputFile.getText());
            args.setFromPage(this.tfFrom.getText());
            args.setToPage(this.tfTo.getText());
            args.setNumOfPages(this.tfNumOfPages.getText());
            args.setPages(this.radioButtonPages.isSelected());
            if (!new File(args.getInputFilename()).exists()) {
                new Alert(Alert.AlertType.ERROR, bundle.getString("File_not_exist")).show();
            } else if (args.getInputFilename().length() > 0 && args.getOutputFilename().length() > 0
                    && ((this.radioButtonPages.isSelected() && args.getFromPage().length() > 0)
                    || (this.radioButtonFiles.isSelected() && args.getNumOfPages().length() > 0))) {

                Pattern regexNums = Pattern.compile("^\\d+$");

                if ((this.radioButtonPages.isSelected() && regexNums.matcher(args.getFromPage()).matches() && (args.getToPage().length() > 0 ? regexNums.matcher(args.getToPage()).matches() : true)) || (this.radioButtonFiles.isSelected() && regexNums.matcher(args.getNumOfPages()).matches())) {
                    return;
                } else {
                    new Alert(Alert.AlertType.ERROR, bundle.getString("Input_invalid")).show();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, bundle.getString("Input_incomplete")).show();
            }
            
            e.consume();
        });
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N 
        btnBrowseInput.setOnAction(e -> handleAction(e));
        btnBrowseOutput.setOnAction(e -> handleAction(e));
        tfFrom.disableProperty().bind(radioButtonFiles.selectedProperty());
        tfTo.disableProperty().bind(radioButtonFiles.selectedProperty());
        tfNumOfPages.disableProperty().bind(radioButtonPages.selectedProperty());

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return args;
            }

            return null;
        });
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnBrowseInput) {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(currentDirectory);
            fc.setTitle(bundle.getString("Open"));
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
            fc.getExtensionFilters().add(pdfFilter);
            File selectedFile = fc.showOpenDialog(btnBrowseInput.getScene().getWindow());
            if (selectedFile != null) {
                currentDirectory = new File(selectedFile.getPath()).getParentFile();
                this.tfInputFile.setText(selectedFile.getPath());
            }
        } else if (event.getSource() == btnBrowseOutput) {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(currentDirectory);
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
        }
    }
    
    /**
     * @return the currentDirectory
     */
    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * @param currentDirectory the currentDirectory to set
     */
    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }
}
