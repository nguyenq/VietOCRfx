/**
 * Copyright @ 2021 Quan Nguyen
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
package net.sourceforge.vietocr.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import net.sourceforge.tess4j.ITesseract;

/**
 * Allows only one PDF option selected.
 */
public class OuputFormatCheckBoxActionListener implements EventHandler<ActionEvent> {

    private final MenuButton parent;

    public OuputFormatCheckBoxActionListener(MenuButton parent) {
        this.parent = parent;
    }

    @Override
    public void handle(ActionEvent e) {
        if (e.getSource() instanceof CheckBox chb) {
            if (chb.isSelected()) {
                String label = chb.getText();

                if (ITesseract.RenderedFormat.PDF.name().equals(label)) {
                    CheckBox chb1 = (CheckBox) getDescendant(parent, ITesseract.RenderedFormat.PDF_TEXTONLY.name());
                    if (chb1 != null) {
                        chb1.setSelected(false);
                    }
                } else if (ITesseract.RenderedFormat.PDF_TEXTONLY.name().equals(label)) {
                    CheckBox chb2 = (CheckBox) getDescendant(parent, ITesseract.RenderedFormat.PDF.name());
                    if (chb2 != null) {
                        chb2.setSelected(false);
                    }
                }
            }
        }
    }

    /**
     * Gets the descendant of MenuButton with specified text
     * @param container
     * @param text
     * @return 
     */
    private CheckBox getDescendant(MenuButton container, String text) {
        for (MenuItem mi : container.getItems()) {
            if (mi instanceof CustomMenuItem cmi) {
                CheckBox chb = (CheckBox) cmi.getContent();
                if (text.equals(chb.getText())) {
                    return chb;
                }                
            }
        }

        return null;
    }
}
