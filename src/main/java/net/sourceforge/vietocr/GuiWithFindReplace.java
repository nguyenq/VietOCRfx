/**
 * Copyright @ 2018 Quan Nguyen
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

import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.vietocr.controls.FindReplaceDialogController;

public class GuiWithFindReplace extends GuiWithPostprocess {

    @FXML
    private Button btnFind;

    private Stage frDialog;
    private FindReplaceDialogController controller;
    private final boolean bMatchDiacritics, bMatchWholeWord, bMatchCase, bMatchRegex;
    private final static Logger logger = Logger.getLogger(GuiWithFindReplace.class.getName());

    public GuiWithFindReplace() {
        bMatchDiacritics = prefs.getBoolean("MatchDiacritics", false);
        bMatchWholeWord = prefs.getBoolean("MatchWholeWord", false);
        bMatchCase = prefs.getBoolean("MatchCase", false);
        bMatchRegex = prefs.getBoolean("MatchRegex", false);
    }

    @FXML
    @Override
    protected void handleAction(javafx.event.ActionEvent event) {
        if (event.getSource() == btnFind) {
            try {
                if (frDialog == null) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FindReplaceDialog.fxml"));
                    Parent root = fxmlLoader.load();
                    controller = fxmlLoader.getController();
                    controller.setTextArea(textarea);
                    controller.setMatchDiacritics(bMatchDiacritics);
                    controller.setMatchWholeWord(bMatchWholeWord);
                    controller.setMatchCase(bMatchCase);
                    controller.setMatchRegex(bMatchRegex);
                    frDialog = new Stage();
                    frDialog.setResizable(false);
                    frDialog.initStyle(StageStyle.UTILITY);
                    frDialog.setAlwaysOnTop(true);
//                    frDialog.setX(prefs.getDouble(strChangeCaseX, frDialog.getX()));
//                    frDialog.setY(prefs.getDouble(strChangeCaseY, frDialog.getY()));
                    Scene scene = new Scene(root);
                    frDialog.setScene(scene);
                    frDialog.setTitle("Find and Replace");
                }

                frDialog.toFront();
                frDialog.show();
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        } else {
            super.handleAction(event);
        }
    }

    @Override
    public void savePrefs() {
        if (frDialog != null) {
            prefs.putBoolean("MatchDiacritics", controller.isMatchDiacritics());
            prefs.putBoolean("MatchWholeWord", controller.isMatchWholeWord());
            prefs.putBoolean("MatchRegex", controller.isMatchRegex());
            prefs.putBoolean("MatchCase", controller.isMatchCase());
        }

        super.savePrefs();
    }
}
