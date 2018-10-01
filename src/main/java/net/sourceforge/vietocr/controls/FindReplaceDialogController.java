package net.sourceforge.vietocr.controls;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

import net.sourceforge.vietpad.inputmethod.VietKeyListener;
import net.sourceforge.vietpad.utilities.VietUtilities;

public class FindReplaceDialogController implements Initializable {

    @FXML
    private Button buttonClose;
    @FXML
    private Button buttonFindNext;
    @FXML
    private Button buttonReplace;
    @FXML
    private Button buttonReplaceAll;
    @FXML
    private CheckBox checkBoxMatchCase;
    @FXML
    private CheckBox checkBoxMatchDiacritics;
    @FXML
    private CheckBox checkBoxMatchRegex;
    @FXML
    private CheckBox checkBoxMatchWholeWord;
    @FXML
    private ComboBox<String> comboBoxFind;
    @FXML
    private ComboBox<String> comboBoxReplace;
    @FXML
    private RadioButton radioButtonSearchDown;
    @FXML
    private RadioButton radioButtonSearchUp;

    TextArea textarea;
    private ResourceBundle bundle;

    static private boolean mouse = false;
    private final static Logger logger = Logger.getLogger(FindReplaceDialogController.class.getName());

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.controls.FindReplaceDialog");
        this.comboBoxFind.requestFocus();
        new VietKeyListener(comboBoxFind);
        new VietKeyListener(comboBoxReplace);
    }
    
    /**
     *
     * @param textarea
     */
    public void setTextArea(TextArea textarea) {
        this.textarea = textarea;
    }

    /**
     * @return the state of matchCase checkBox
     */
    public boolean isMatchCase() {
        return this.checkBoxMatchCase.isSelected();
    }

    /**
     * @param matchCase the matchCase to set
     */
    public void setMatchCase(boolean matchCase) {
        this.checkBoxMatchCase.setSelected(matchCase);
    }

    /**
     * @return the state of matchDiacritics checkBox
     */
    public boolean isMatchDiacritics() {
        return this.checkBoxMatchDiacritics.isSelected();
    }

    /**
     * @param matchDiacritics the matchDiacritics to set
     */
    public void setMatchDiacritics(boolean matchDiacritics) {
        this.checkBoxMatchDiacritics.setSelected(matchDiacritics);
    }

    /**
     * @return the state of matchRegex checkBox
     */
    public boolean isMatchRegex() {
        return this.checkBoxMatchRegex.isSelected();
    }

    /**
     * @param matchRegex the matchRegex to set
     */
    public void setMatchRegex(boolean matchRegex) {
        this.checkBoxMatchRegex.setSelected(matchRegex);
    }

    /**
     * @return the state of matchWholeWord checkBox
     */
    public boolean isMatchWholeWord() {
        return this.checkBoxMatchWholeWord.isSelected();
    }

    /**
     * @param matchWholeWord the matchWholeWord to set
     */
    public void setMatchWholeWord(boolean matchWholeWord) {
        this.checkBoxMatchWholeWord.setSelected(matchWholeWord);
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == buttonFindNext) {
            populateComboBox("Find");
            FindNext();
        } else if (event.getSource() == buttonReplace) {
            populateComboBox("Replace");
            Replace();
        } else if (event.getSource() == buttonReplaceAll) {
            populateComboBox("Replace");
            ReplaceAll();
        } else if (event.getSource() == buttonClose) {
            ((Stage) buttonClose.getScene().getWindow()).close();
        }
    }

    /**
     * Populates the combobox with entries from the corresponding text field
     */
    void populateComboBox(String button) {
        String text;
        ComboBox<String> comboBox;

        if (button.equals("Find")) {
            text = comboBoxFind.getValue();
            comboBox = comboBoxFind;
        } else {
            text = comboBoxReplace.getValue();
            comboBox = comboBoxReplace;
        }

        if (text.equals("")) {
            return;
        }

        boolean isEntryExisted = comboBox.getItems().contains(text);
        if (!isEntryExisted) {
            comboBox.getItems().add(0, text);
            comboBox.getSelectionModel().select(0);
        }
    }

    /**
     * Finds next occurrence of find string.
     *
     * @return
     */
    boolean FindNext() {
        String searchData, strFind;
        if (!checkBoxMatchDiacritics.isSelected()) {
            searchData = VietUtilities.stripDiacritics(textarea.getText());
            strFind = VietUtilities.stripDiacritics(this.comboBoxFind.getValue());
        } else {
            searchData = textarea.getText();
            strFind = this.comboBoxFind.getValue();
        }

        if (radioButtonSearchDown.isSelected()) {
            int iStart = textarea.getCaretPosition();

            if (isMatchRegex() || isMatchWholeWord()) {
                if (isMatchWholeWord() && !checkBoxMatchWholeWord.isDisabled()) {
                    strFind = "\\b" + Pattern.quote(strFind) + "\\b";
                }

                try {
                    Pattern regex = Pattern.compile((checkBoxMatchCase.isSelected() ? "" : "(?i)") + strFind, Pattern.MULTILINE);
                    Matcher m = regex.matcher(searchData);
                    m.region(iStart, textarea.getLength());
                    if (m.find()) {
                        textarea.selectRange(m.start(), m.end());
                        return true;
                    }
                } catch (Exception e) {
//                    logger.log(Level.WARNING, e.getMessage(), e);
//                    JOptionPane.showMessageDialog(null, e.getMessage(), bundle.getString("Regex_Error"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                while (iStart + strFind.length() <= textarea.getLength()) {
                    if (searchData.regionMatches(!checkBoxMatchCase.isSelected(), iStart, strFind, 0, strFind.length())) {
                        textarea.selectRange(iStart, iStart + strFind.length());
                        return true;
                    }
                    iStart++;
                }
            }
        } else {
            if (isMatchRegex() || isMatchWholeWord()) {
                if (isMatchWholeWord() && !checkBoxMatchWholeWord.isDisabled()) {
                    strFind = "\\b" + Pattern.quote(strFind) + "\\b";
                }
                int iEnd = textarea.getAnchor();

                try {
                    Pattern regex = Pattern.compile((checkBoxMatchCase.isSelected() ? "" : "(?i)") + String.format("%1$s(?!.*%1$s)", strFind), Pattern.MULTILINE | Pattern.DOTALL);
                    Matcher m = regex.matcher(searchData);
                    m.region(0, iEnd);
                    if (m.find()) {
                        textarea.selectRange(m.start(), m.end());
                        return true;
                    }
                } catch (Exception e) {
//                    logger.log(Level.WARNING, e.getMessage(), e);
//                    JOptionPane.showMessageDialog(null, e.getMessage(), bundle.getString("Regex_Error"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                int iStart = textarea.getAnchor() - strFind.length();

                while (iStart >= 0) {
                    if (searchData.regionMatches(!checkBoxMatchCase.isSelected(), iStart, strFind, 0, strFind.length())) {
                        textarea.selectRange(iStart, iStart + strFind.length());
                        return true;
                    }
                    iStart--;
                }
            }
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(((Stage) buttonClose.getScene().getWindow()).getTitle());
        alert.setContentText(bundle.getString("Cannot_find_\"") + comboBoxFind.getValue() + "\".\n"
                + bundle.getString("Continue_search_from_") + (radioButtonSearchDown.isSelected() ? bundle.getString("beginning") : bundle.getString("end")) + "?");
        ((Stage) alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (radioButtonSearchDown.isSelected()) {
                textarea.selectHome();
            } else {
                textarea.selectEnd();
            }

            textarea.deselect();
//            textarea.selectRange(textarea.getCaretPosition(), textarea.getCaretPosition());
            FindNext();
        } else {
            // ... user chose CANCEL or closed the dialog
        }

        return false;
    }

    /**
     * Replaces currently selected text with replacement string.
     */
    void Replace() {
        String strFind = this.comboBoxFind.getValue();
        String selectedText = textarea.getSelectedText();

        if (selectedText == null) {
            FindNext();
            return;
        }

        if (!checkBoxMatchDiacritics.isSelected()) {
            strFind = VietUtilities.stripDiacritics(strFind);
            selectedText = VietUtilities.stripDiacritics(selectedText);
        }

        String strReplace = this.comboBoxReplace.getValue();
        int start = textarea.getAnchor();
        if (isMatchRegex()) {
            try {
                Pattern regex = Pattern.compile((checkBoxMatchCase.isSelected() ? "" : "(?i)") + strFind, Pattern.MULTILINE);
                textarea.replaceSelection(regex.matcher(selectedText).replaceAll(unescape(strReplace)));
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                JOptionPane.showMessageDialog(null, e.getMessage(), bundle.getString("Regex_Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if ((checkBoxMatchCase.isSelected() && selectedText.compareTo(strFind) == 0) || (!checkBoxMatchCase.isSelected() && selectedText.compareToIgnoreCase(strFind) == 0)) {
            textarea.replaceSelection(strReplace);
        }

        if (!radioButtonSearchDown.isSelected()) {
            textarea.selectRange(start, start);
        }

        FindNext();
    }

    /**
     * Replaces all occurrences of find string with replacement.
     */
    void ReplaceAll() {
        String strFind = this.comboBoxFind.getValue();
        String str = textarea.getText();
        String strTemp;

        if (!checkBoxMatchDiacritics.isSelected()) {
            strFind = VietUtilities.stripDiacritics(strFind);
            strTemp = VietUtilities.stripDiacritics(str);
        } else {
            strTemp = str;
        }

        String strReplace = this.comboBoxReplace.getValue();
        int count = 0;

        if (isMatchRegex() || checkBoxMatchDiacritics.isSelected()) {
            // only for MatchDiacritics
            String patt = isMatchRegex() ? strFind : Pattern.quote(strFind);
            if (isMatchWholeWord() && !checkBoxMatchWholeWord.isDisabled()) {
                patt = "\\b" + patt + "\\b";
            }

            try {
                Pattern regex = Pattern.compile((checkBoxMatchCase.isSelected() ? "" : "(?i)") + patt, Pattern.MULTILINE);
                Matcher matcher = regex.matcher(str);
                while (matcher.find()) {
                    count++;
                }
                matcher.reset();
                str = regex.matcher(str).replaceAll(unescape(strReplace));
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                JOptionPane.showMessageDialog(null, e.getMessage(), bundle.getString("Regex_Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            StringBuilder strB = new StringBuilder(str);

            try {
                Pattern wholewordPatt = Pattern.compile((checkBoxMatchCase.isSelected() ? "" : "(?i)") + "\\b" + strFind + "\\b", Pattern.MULTILINE);
                for (int i = 0; i <= strB.length() - strFind.length();) {
                    if (strTemp.regionMatches(!checkBoxMatchCase.isSelected(), i, strFind, 0, strFind.length())) {
                        // match whole word requires extra treatment
                        if (isMatchWholeWord()) {
                            Matcher m = wholewordPatt.matcher(strTemp);
                            if (m.find(i)) {
                                if (i != m.start()) {
                                    i++;
                                    continue;
                                }
                            } else {
                                i++;
                                continue;
                            }
                        }

                        strB.delete(i, i + strFind.length());
                        strB.insert(i, strReplace);
                        if (!checkBoxMatchDiacritics.isSelected()) {
                            strTemp = VietUtilities.stripDiacritics(strB.toString());
                        } else {
                            strTemp = strB.toString();
                        }
                        i += strReplace.length();
                        count++;
                    } else {
                        i++;
                    }
                }

                str = strB.toString();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                JOptionPane.showMessageDialog(null, e.getMessage(), bundle.getString("Regex_Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (!str.equals(textarea.getText())) {
            textarea.setText(str);
            textarea.selectRange(0, 0);
//            textarea.Modified = true;
        }

        // display count of replacements
        warning(String.format(bundle.getString("ReplacedOccurrence"), count));
    }

    private String unescape(String input) {
        return input.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }

    /**
     * Display warning message
     *
     * @param message Warning message
     */
    protected void warning(String message) {
//        JOptionPane.showMessageDialog(this,
//                message, this.getTitle(),
//                JOptionPane.INFORMATION_MESSAGE);
    }
}
