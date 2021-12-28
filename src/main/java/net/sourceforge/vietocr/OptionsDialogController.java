/**
 * Copyright @ 2018 Quan Nguyen
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.vietocr.controls.OuputFormatCheckBoxActionListener;

public class OptionsDialogController implements Initializable {

    @FXML
    private Button btnOK;
    @FXML
    private Button btnCancel;
    @FXML
    private MenuButton mbOutputFormat;
    @FXML
    private CheckBox chbEnable;
    @FXML
    private CheckBox chbPostProcessing;
    @FXML
    private CheckBox chbCorrectLetterCases;
    @FXML
    private CheckBox chbRemoveLineBreaks;
    @FXML
    private CheckBox chbDeskew;
    @FXML
    private CheckBox chbRemoveLines;
    @FXML
    private CheckBox chbReplaceHyphens;
    @FXML
    private CheckBox chbRemoveHyphens;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (ITesseract.RenderedFormat value : ITesseract.RenderedFormat.values()) {
            CheckBox chb = new CheckBox(value.name());
            chb.setOnAction(new OuputFormatCheckBoxActionListener(mbOutputFormat));
            CustomMenuItem menuItem = new CustomMenuItem(chb);
            // keep the menu open during selection
            menuItem.setHideOnClick(false);
            mbOutputFormat.getItems().add(menuItem);
        }
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnOK) {

            ((Stage) btnOK.getScene().getWindow()).close();
        } else if (event.getSource() == btnCancel) {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }

    /**
     * @return the selectedFormats
     */
    public String getSelectedOutputFormats() {
        List<String> list = new ArrayList<>();
        for (MenuItem mi : mbOutputFormat.getItems()) {
            if (mi instanceof CustomMenuItem cmi) {
                CheckBox item = (CheckBox) cmi.getContent();
                if (item.isSelected()) {
                    list.add(item.getText());
                }
            }

        }
        return list.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(","));
    }

    /**
     * @param selectedFormats the selectedFormats to set
     */
    public void setSelectedOutputFormats(String selectedFormats) {
        List<String> list = Arrays.asList(selectedFormats.split(","));
        for (MenuItem mi : mbOutputFormat.getItems()) {
            if (mi instanceof CustomMenuItem cmi) {
                CheckBox item = (CheckBox) cmi.getContent();
                if (list.contains(item.getText())) {
                    item.setSelected(true);
                }
            }
        }
    }

    /**
     * @return the processingOptions
     */
    public ProcessingOptions getProcessingOptions() {
        ProcessingOptions processingOptions = new ProcessingOptions();
        processingOptions.setDeskew(this.chbDeskew.isSelected());
        processingOptions.setPostProcessing(this.chbPostProcessing.isSelected());
        processingOptions.setRemoveLines(this.chbRemoveLines.isSelected());
        processingOptions.setRemoveLineBreaks(this.chbRemoveLineBreaks.isSelected());
        processingOptions.setCorrectLetterCases(this.chbCorrectLetterCases.isSelected());
        processingOptions.setRemoveHyphens(this.chbRemoveHyphens.isSelected());
        processingOptions.setReplaceHyphens(this.chbReplaceHyphens.isSelected());

        return processingOptions;
    }

    /**
     * @param processingOptions the processingOptions to set
     */
    public void setProcessingOptions(ProcessingOptions processingOptions) {
        this.chbDeskew.setSelected(processingOptions.isDeskew());
        this.chbPostProcessing.setSelected(processingOptions.isPostProcessing());
        this.chbRemoveLines.setSelected(processingOptions.isRemoveLines());
        this.chbRemoveLineBreaks.setSelected(processingOptions.isRemoveLineBreaks());
        this.chbCorrectLetterCases.setSelected(processingOptions.isCorrectLetterCases());
        this.chbRemoveHyphens.setSelected(processingOptions.isRemoveHyphens());
        this.chbReplaceHyphens.setSelected(processingOptions.isReplaceHyphens());
    }
}
