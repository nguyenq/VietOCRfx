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

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.vietocr.controls.OuputFormatCheckBoxActionListener;

public class BulkDialogController implements Initializable {

    @FXML
    private Button btnRun;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnOptions;
    @FXML
    private MenuButton mbOutputFormat;
    @FXML
    private TextField tfInputFolder;
    @FXML
    private TextField tfOutputFolder;
    @FXML
    private Button btnBrowseInputFolder;
    @FXML
    private Button btnBrowseOutputFolder;

    protected String inDirectory;
    protected String outDirectory;
    private DirectoryChooser dirChoooser;
    final Preferences prefs = GuiController.prefs;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (RenderedFormat value : RenderedFormat.values()) {
            CheckBox chb = new CheckBox(value.name());
            chb.setOnAction(new OuputFormatCheckBoxActionListener(mbOutputFormat));
            CustomMenuItem menuItem = new CustomMenuItem(chb);
            // keep the menu open during selection
            menuItem.setHideOnClick(false);
            mbOutputFormat.getItems().add(menuItem);
        }

        inDirectory = prefs.get("inDirectory", new File(System.getProperty("user.dir"), ".").getPath());
        if (!Files.exists(Paths.get(inDirectory))) {
            inDirectory = System.getProperty("user.home");
        }
        tfInputFolder.setText(inDirectory);
        tfInputFolder.setStyle("-fx-focus-color: transparent;");
        
        outDirectory = prefs.get("outDirectory", new File(System.getProperty("user.dir"), ".").getPath());
        if (!Files.exists(Paths.get(outDirectory))) {
            outDirectory = System.getProperty("user.home");
        }
        
        tfOutputFolder.setText(outDirectory);
        tfOutputFolder.setStyle("-fx-focus-color: transparent;");
        
        dirChoooser = new DirectoryChooser();
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnRun) {
            savePrefs();
            ((Stage) btnRun.getScene().getWindow()).close();
        } else if (event.getSource() == btnCancel) {
            ((Stage) btnCancel.getScene().getWindow()).close();
        } else if (event.getSource() == btnOptions) {
//            ((GuiWithBulkOCR) this.getParent()).jMenuItemOptionsActionPerformed(evt);
//            ((Button) btnOptions.getScene().lookup("#miOptions")).fire();
        } else if (event.getSource() == btnBrowseInputFolder) {
            dirChoooser.setTitle("Set Location of Input Files");
            dirChoooser.setInitialDirectory(new File(inDirectory));
            File dir = dirChoooser.showDialog(btnBrowseInputFolder.getScene().getWindow());
            if (dir != null) {
                inDirectory = dir.getPath();
                tfInputFolder.setText(inDirectory);
            }
        } else if (event.getSource() == btnBrowseOutputFolder) {
            dirChoooser.setTitle("Set Location of Output Files");
            dirChoooser.setInitialDirectory(new File(outDirectory));
            File dir = dirChoooser.showDialog(btnBrowseOutputFolder.getScene().getWindow());
            if (dir != null) {
                outDirectory = dir.getPath();
                tfOutputFolder.setText(outDirectory);
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
        return list.stream().collect(Collectors.joining(","));
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

    void savePrefs() {
        if (inDirectory != null) {
            prefs.put("inDirectory", inDirectory);
        }
        if (outDirectory != null) {
            prefs.put("outDirectory", outDirectory);
        }
    }
}
