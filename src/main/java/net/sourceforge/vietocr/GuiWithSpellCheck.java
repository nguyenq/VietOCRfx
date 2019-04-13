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

import javafx.scene.control.skin.TextAreaSkin;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.HitInfo;
import net.sourceforge.vietpad.utilities.SpellCheckHelper;
import net.sourceforge.vietpad.utilities.TextUtilities;

public class GuiWithSpellCheck extends GuiWithFindReplace {

    @FXML
    private ToggleButton btnSpellCheck;

    private IndexRange wordBoundaries;
    int pointClicked;
    private SpellCheckHelper speller;

    private final static Logger logger = Logger.getLogger(GuiWithOCR.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);

        textarea.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                TextAreaSkin skin = (TextAreaSkin) textarea.getSkin();
                HitInfo mouseHit = skin.getIndex(event.getX(), event.getY());
                pointClicked = mouseHit.getInsertionIndex();
            }
        });

        TextAreaSkin customContextSkin = new TextAreaSkin(textarea) {
//            @Override
//            public void populateContextMenu(ContextMenu contextMenu) {
//                super.populateContextMenu(contextMenu);
//                if (btnSpellCheck.isSelected()) {
//                    populatePopupMenuWithSuggestions(contextMenu, pointClicked);
//                }
//            }
        };
        textarea.setSkin(customContextSkin);
    }

    @FXML
    @Override
    protected void handleAction(javafx.event.ActionEvent event) {
        if (event.getSource() == btnSpellCheck) {
            spellCheck(event);
        } else {
            super.handleAction(event);
        }
    }

    void populatePopupMenuWithSuggestions(ContextMenu contextMenu, int pos) {
        try {
            wordBoundaries = TextUtilities.getWordBoundaries(textarea.getText(), pos);
            String curWord = textarea.getText(wordBoundaries.getStart(), wordBoundaries.getEnd());
            makeSuggestions(contextMenu, curWord);
        } catch (IllegalArgumentException e) {
            // ignore
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
//        } finally {
//            // load standard menu items
//            repopulatePopupMenu();
        }
    }

    void repopulatePopupMenu() {
//        contextMenu.add(m_undoAction);
//        contextMenu.add(m_redoAction);
//        contextMenu.getItems().addSeparator();
//        contextMenu.add(actionCut);
//        contextMenu.add(actionCopy);
//        contextMenu.add(actionPaste);
//        contextMenu.add(actionDelete);
//        contextMenu.addSeparator();
//        contextMenu.add(actionSelectAll);
    }

    /**
     * Populates suggestions at top of context menu.
     *
     * @param curWord
     */
    void makeSuggestions(ContextMenu contextMenu, final String curWord) {
        if (speller == null || curWord == null || curWord.trim().length() == 0) {
            return;
        }

        List<String> suggests = speller.suggest(curWord);
        if (suggests == null || suggests.isEmpty()) {
            return;
        }

        EventHandler<ActionEvent> correctLst = new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                MenuItem menuItem = (MenuItem) e.getSource();
                String selectedSuggestion = (String) menuItem.getUserData();
                if (selectedSuggestion.equals("ignore.word")) {
                    speller.ignoreWord(curWord);
                } else if (selectedSuggestion.equals("add.word")) {
                    speller.addWord(curWord);
                } else {
                    textarea.replaceText(wordBoundaries, selectedSuggestion);
                }
            }
        };

        MenuItem addItem = new MenuItem(bundle.getString("Add_to_Dictionary"));
        addItem.setUserData("add.word");
        addItem.setOnAction(correctLst);
        MenuItem ignoreItem = new MenuItem(bundle.getString("Ignore_All"));
        ignoreItem.setUserData("ignore.word");
        ignoreItem.setOnAction(correctLst);
        contextMenu.getItems().addAll(0, Arrays.asList(new SeparatorMenuItem(), ignoreItem, addItem, new SeparatorMenuItem()));

        for (String word : suggests) {
            MenuItem spellItem = new MenuItem(word);
            spellItem.setUserData(word);
//            Font itemFont = item.getFont();
//            if (itemFont.canDisplayUpTo(word) == -1) {
            spellItem.setStyle("-fx-font-weight: bold");
//            } else {
//                // use TextArea's font
//                item.setFont(font.deriveFont(Font.BOLD, itemFont.getSize2D()));
//            }
            spellItem.setOnAction(correctLst);
            contextMenu.getItems().add(0, spellItem);
        }
    }

    void spellCheck(ActionEvent evt) {
        String localeId = null;

        if (lookupISO_3_1_Codes.containsKey(curLangCode)) {
            localeId = lookupISO_3_1_Codes.getProperty(curLangCode);
        } else if (lookupISO_3_1_Codes.containsKey(curLangCode.substring(0, 3))) {
            localeId = lookupISO_3_1_Codes.getProperty(curLangCode.substring(0, 3));
        }
        if (localeId == null) {
            new Alert(Alert.AlertType.ERROR, "Need to add an entry in data/ISO639-1.xml file.").show();
            return;
        }

        speller = new SpellCheckHelper(this.textarea, localeId);
        if (this.btnSpellCheck.isSelected()) {
            speller.enableSpellCheck();
        } else {
            speller.disableSpellCheck();
        }
    }
}
