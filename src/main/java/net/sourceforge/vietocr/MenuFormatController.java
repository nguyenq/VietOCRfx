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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.vietpad.utilities.TextUtilities;
import org.controlsfx.dialog.FontSelectorDialog;

public class MenuFormatController implements Initializable {

    @FXML
    private MenuBar menuBar;
    @FXML
    private CheckMenuItem chmiWordWrap;
    @FXML
    private MenuItem miChangeCase;
    @FXML
    private MenuItem miRemoveLineBreaks;
    @FXML
    private MenuItem miFont;

    private Stage changeCaseDialog;
    private ChangeCaseDialogController controller;
    private final String strSelectedCase = "selectedCase";
    private final String strChangeCaseX = "changeCaseX";
    private final String strChangeCaseY = "changeCaseY";

    protected ResourceBundle bundle;
    static final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr");
    private boolean wordWrapOn;
    private TextArea textarea;
    private static MenuFormatController instance;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        wordWrapOn = prefs.getBoolean("wordWrap", true);
        chmiWordWrap.setSelected(wordWrapOn);
    }

    /**
     * Gets MenuFormatController instance (for child controllers).
     *
     * @return
     */
    public static MenuFormatController getInstance() {
        return instance;
    }

    void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    @FXML
    private void handleAction(ActionEvent event) {
        textarea = (TextArea) menuBar.getScene().lookup("#textarea");

        if (event.getSource() == chmiWordWrap) {
            wordWrapOn = chmiWordWrap.isSelected();
            textarea.setWrapText(wordWrapOn);
        } else if (event.getSource() == miChangeCase) {
            try {
                if (changeCaseDialog == null) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ChangeCaseDialog.fxml"));
                    Parent root = fxmlLoader.load();
                    controller = fxmlLoader.getController();
                    controller.setSelectedCase(prefs.get(strSelectedCase, "UPPERCASE"));
                    changeCaseDialog = new Stage();
                    changeCaseDialog.setResizable(false);
                    changeCaseDialog.initStyle(StageStyle.UTILITY);
                    changeCaseDialog.setAlwaysOnTop(true);
                    changeCaseDialog.setX(prefs.getDouble(strChangeCaseX, changeCaseDialog.getX()));
                    changeCaseDialog.setY(prefs.getDouble(strChangeCaseY, changeCaseDialog.getY()));
                    Scene scene = new Scene(root);
                    changeCaseDialog.setScene(scene);
                    changeCaseDialog.setTitle("Change Case");
                }

                changeCaseDialog.toFront();
                changeCaseDialog.show();
            } catch (Exception e) {

            }
        } else if (event.getSource() == miRemoveLineBreaks) {
            ((Button) menuBar.getScene().lookup("#btnRemoveLineBreaks")).fire();
        } else if (event.getSource() == miFont) {
            Font font = textarea.getFont();
            FontSelectorDialog dialog = new FontSelectorDialog(font);
            Optional<Font> op = dialog.showAndWait();
            if (op.isPresent()) {
                textarea.setFont(op.get());
            }
        }
    }

    /**
     * Changes letter case.
     *
     * @param typeOfCase The type that the case should be changed to
     */
    public void changeCase(String typeOfCase) {
        if (textarea.selectedTextProperty().length().get() == 0) {
            textarea.selectAll();

            if (textarea.selectedTextProperty().length().get() == 0) {
                return;
            }
        }

        String result = TextUtilities.changeCase(textarea.getSelectedText(), typeOfCase);

        int start = textarea.getSelection().getStart();
        textarea.replaceSelection(result);
        textarea.selectRange(start, start + result.length());
    }

    void savePrefs() {
        if (changeCaseDialog != null) {
            prefs.put(strSelectedCase, controller.getSelectedCase());
            prefs.putDouble(strChangeCaseX, changeCaseDialog.getX());
            prefs.putDouble(strChangeCaseY, changeCaseDialog.getY());
        }
    }

}
