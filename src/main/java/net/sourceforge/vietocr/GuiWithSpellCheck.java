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

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import net.sourceforge.vietpad.utilities.SpellCheckHelper;

public class GuiWithSpellCheck extends GuiWithFindReplace {

    @FXML
    private ToggleButton btnSpellCheck;

    private int start, end;
    private SpellCheckHelper speller;

    private final static Logger logger = Logger.getLogger(GuiWithOCR.class.getName());

    @FXML
    @Override
    protected void handleAction(javafx.event.ActionEvent event) {
        if (event.getSource() == btnSpellCheck) {
            spellCheck(event);
        } else {
            super.handleAction(event);
        }
    }

    void populatePopupMenuWithSuggestions(Point pointClicked) {
        try {
            if (this.btnSpellCheck.isSelected()) {
//                int offset = textarea.viewToModel(pointClicked);
//                start = javax.swing.text.Utilities.getWordStart(textarea, offset);
//                end = javax.swing.text.Utilities.getWordEnd(textarea, offset);
//                String curWord = textarea.getText(start, end - start);
//                makeSuggestions(curWord);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            // load standard menu items
            repopulatePopupMenu();
        }
    }

    void repopulatePopupMenu() {
//        popup.add(m_undoAction);
//        popup.add(m_redoAction);
//        popup.getItems().addSeparator();
//        popup.add(actionCut);
//        popup.add(actionCopy);
//        popup.add(actionPaste);
//        popup.add(actionDelete);
//        popup.addSeparator();
//        popup.add(actionSelectAll);
    }

    /**
     * Populates suggestions at top of context menu.
     *
     * @param curWord
     */
    void makeSuggestions(final String curWord) {
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
                String selectedSuggestion = e.getSource().toString();
                if (selectedSuggestion.equals("ignore.word")) {
                    speller.ignoreWord(curWord);
                } else if (selectedSuggestion.equals("add.word")) {
                    speller.addWord(curWord);
                } else {
                    textarea.selectRange(start, end);
                    textarea.replaceSelection(selectedSuggestion);
                }
                speller.spellCheck();
            }
        };

        for (String word : suggests) {
            MenuItem item = new MenuItem(word);
//            Font itemFont = item.getFont();
//            if (itemFont.canDisplayUpTo(word) == -1) {
//                item.setFont(itemFont.deriveFont(Font.BOLD));
//            } else {
//                // use jTextArea's font
//                item.setFont(font.deriveFont(Font.BOLD, itemFont.getSize2D()));
//            }
            item.setOnAction(correctLst);
        }

//        popup.addSeparator();
//        MenuItem item = new MenuItem(bundle.getString("Ignore_All"));
//        item.setActionCommand("ignore.word");
//        item.addActionListener(correctLst);
//        popup.add(item);
//        item = new MenuItem(bundle.getString("Add_to_Dictionary"));
//        item.setActionCommand("add.word");
//        item.addActionListener(correctLst);
//        popup.add(item);
//        popup.addSeparator();
    }

    void spellCheck(ActionEvent evt) {
        String localeId = null;

        if (lookupISO_3_1_Codes.containsKey(curLangCode)) {
            localeId = lookupISO_3_1_Codes.getProperty(curLangCode);
        } else if (lookupISO_3_1_Codes.containsKey(curLangCode.substring(0, 3))) {
            localeId = lookupISO_3_1_Codes.getProperty(curLangCode.substring(0, 3));
        }
        if (localeId == null) {
//            JOptionPane.showMessageDialog(null, "Need to add an entry in data/ISO639-1.xml file.", VietOCR.APP_NAME, JOptionPane.ERROR_MESSAGE);
            return;
        }

        speller = new SpellCheckHelper(this.textarea, localeId);
        if (this.btnSpellCheck.isSelected()) {
            speller.enableSpellCheck();
        } else {
            speller.disableSpellCheck();
        }
//        this.textarea.repaint();
    }
}
