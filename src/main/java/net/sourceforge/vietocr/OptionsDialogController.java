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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.vietocr.controls.OuputFormatCheckBoxActionListener;

public class OptionsDialogController extends Dialog<ProcessingOptions> implements Initializable {

    @FXML
    private MenuButton mbOutputFormat;
    @FXML
    private CheckBox chbDangAmbigs;
    @FXML
    private TextField tfDangAmbigsPath;
    @FXML
    private TextField tfWatch;
    @FXML
    private TextField tfOutput;
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
    @FXML
    private Button btnDangAmbigs;
    @FXML
    private Button btnWatch;
    @FXML
    private Button btnOutput;
    @FXML
    private ButtonType okButtonType;
    @FXML
    private ButtonType cancelButtonType;

    private String curLangCode;
    private DirectoryChooser dirChoooser;
    private ResourceBundle bundle;

    public OptionsDialogController(Window owner) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/OptionsDialog.fxml"));
        fxmlLoader.setController(this);
        DialogPane pane = fxmlLoader.load();
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        setTitle("Options");
        setDialogPane(pane);
        dirChoooser = new DirectoryChooser();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net/sourceforge/vietocr/OptionsDialog");
        btnWatch.setOnAction(e -> handleAction(e));
        btnOutput.setOnAction(e -> handleAction(e));
        btnDangAmbigs.setOnAction(e -> handleAction(e));

        for (ITesseract.RenderedFormat value : ITesseract.RenderedFormat.values()) {
            CheckBox chb = new CheckBox(value.name());
            chb.setOnAction(new OuputFormatCheckBoxActionListener(mbOutputFormat));
            CustomMenuItem menuItem = new CustomMenuItem(chb);
            // keep the menu open during selection
            menuItem.setHideOnClick(false);
            mbOutputFormat.getItems().add(menuItem);
        }

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return getProcessingOptions();
            }

            return null;
        });
    }

    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnWatch) {
            dirChoooser.setTitle(bundle.getString("Set_Watch_Folder"));
            dirChoooser.setInitialDirectory(new File(this.tfWatch.getText()));
            File dir = dirChoooser.showDialog(btnWatch.getScene().getWindow());
            if (dir != null) {
                this.tfWatch.setText(dir.getPath());
            }
        } else if (event.getSource() == btnOutput) {
            dirChoooser.setTitle(bundle.getString("Set_Output_Folder"));
            dirChoooser.setInitialDirectory(new File(this.tfOutput.getText()));
            File dir = dirChoooser.showDialog(btnOutput.getScene().getWindow());
            if (dir != null) {
                this.tfOutput.setText(dir.getPath());
            }
        } else if (event.getSource() == btnDangAmbigs) {
            dirChoooser.setTitle(bundle.getString("Path_to") + " " + curLangCode + ".DangAmbigs.txt");
            dirChoooser.setInitialDirectory(new File(this.tfDangAmbigsPath.getText()));
            File dir = dirChoooser.showDialog(btnDangAmbigs.getScene().getWindow());
            if (dir != null) {
                this.tfDangAmbigsPath.setText(dir.getPath());
            }
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

    /**
     * @return the watchFolder
     */
    public String getWatchFolder() {
        return this.tfWatch.getText();
    }

    /**
     * @param watchFolder the watchFolder to set
     */
    public void setWatchFolder(String watchFolder) {
        this.tfWatch.setText(watchFolder);
    }

    /**
     * @return the outputFolder
     */
    public String getOutputFolder() {
        return this.tfOutput.getText();
    }

    /**
     * @param outputFolder the outputFolder to set
     */
    public void setOutputFolder(String outputFolder) {
        this.tfOutput.setText(outputFolder);
    }

    /**
     * @return the watchEnabled
     */
    public boolean isWatchEnabled() {
        return chbEnable.isSelected();
    }

    /**
     * @param watchEnabled the watchEnabled to set
     */
    public void setWatchEnabled(boolean watchEnabled) {
        this.chbEnable.setSelected(watchEnabled);
    }

    /**
     * @return the dangAmbigsPath
     */
    public String getDangAmbigsPath() {
        return tfDangAmbigsPath.getText();
    }

    /**
     * @param dangAmbigsPath the dangAmbigsPath to set
     */
    public void setDangAmbigsPath(String dangAmbigsPath) {
        this.tfDangAmbigsPath.setText(dangAmbigsPath);
    }

    /**
     * @param curLangCode the curLangCode to set
     */
    public void setCurLangCode(String curLangCode) {
        this.curLangCode = curLangCode;
    }

    /**
     * @return the dangAmbigsOn
     */
    public boolean isDangAmbigsEnabled() {
        return this.chbDangAmbigs.isSelected();
    }

    /**
     * @param dangAmbigsOn the dangAmbigsOn to set
     */
    public void setDangAmbigsEnabled(boolean dangAmbigsOn) {
        this.chbDangAmbigs.setSelected(dangAmbigsOn);;
    }
}
