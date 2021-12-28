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
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnRun) {
            ((Stage) btnRun.getScene().getWindow()).close();
        } else if (event.getSource() == btnCancel) {
            ((Stage) btnCancel.getScene().getWindow()).close();
        } else if (event.getSource() == btnOptions) {
//            ((GuiWithBulkOCR) this.getParent()).jMenuItemOptionsActionPerformed(evt);
//            ((Button) btnOptions.getScene().lookup("#miOptions")).fire();
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
}
