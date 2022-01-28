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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.vietocr.controls.OuputFormatCheckBoxActionListener;

public class BulkDialogController extends Dialog<ButtonType> implements Initializable {

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
    @FXML
    private ButtonType optionsButtonType;
    @FXML
    private ButtonType okButtonType;
    @FXML
    private ButtonType cancelButtonType;

    private String inDirectory;
    private String outDirectory;
    private DirectoryChooser dirChoooser;
    final Preferences prefs = GuiController.prefs;

    /**
     * Initializes the controller class.
     *
     * @param owner
     */
    public BulkDialogController(Window owner) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/BulkDialog.fxml"));
        fxmlLoader.setController(this);
        DialogPane pane = fxmlLoader.load();
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        setTitle("Bulk OCR");
        setDialogPane(pane);

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
        Button leftBtn = (Button) this.getDialogPane().lookupButton(optionsButtonType);
        leftBtn.setGraphic(new ImageView(getClass().getResource("/com/fatcow/icons/tools.png").toExternalForm()));  
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnBrowseInputFolder.setOnAction(e -> handleAction(e));
        btnBrowseOutputFolder.setOnAction(e -> handleAction(e));
    }

    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnBrowseInputFolder) {
            dirChoooser.setTitle("Set Location of Input Files");
            dirChoooser.setInitialDirectory(new File(getInDirectory()));
            File dir = dirChoooser.showDialog(btnBrowseInputFolder.getScene().getWindow());
            if (dir != null) {
                setInDirectory(dir.getPath());
                tfInputFolder.setText(getInDirectory());
            }
        } else if (event.getSource() == btnBrowseOutputFolder) {
            dirChoooser.setTitle("Set Location of Output Files");
            dirChoooser.setInitialDirectory(new File(getOutDirectory()));
            File dir = dirChoooser.showDialog(btnBrowseOutputFolder.getScene().getWindow());
            if (dir != null) {
                setOutDirectory(dir.getPath());
                tfOutputFolder.setText(getOutDirectory());
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

    public void savePrefs() {
        if (getInDirectory() != null) {
            prefs.put("inDirectory", getInDirectory());
        }
        if (getOutDirectory() != null) {
            prefs.put("outDirectory", getOutDirectory());
        }
    }

    /**
     * @return the inDirectory
     */
    public String getInDirectory() {
        return inDirectory;
    }

    /**
     * @param inDirectory the inDirectory to set
     */
    public void setInDirectory(String inDirectory) {
        this.inDirectory = inDirectory;
    }

    /**
     * @return the outDirectory
     */
    public String getOutDirectory() {
        return outDirectory;
    }

    /**
     * @param outDirectory the outDirectory to set
     */
    public void setOutDirectory(String outDirectory) {
        this.outDirectory = outDirectory;
    }
}
