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

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.sourceforge.vietocr.util.*;

public class GuiWithThumbnail extends GuiController {

    @FXML
    private VBox thumbnailBox;

    @Override
    void loadThumbnails() {
        thumbnailBox.getChildren().clear();
        int i = 0;
        for (final BufferedImage bi : imageList) {
            thumbnailBox.getChildren().addAll(createImageView(bi), new Label(String.valueOf(++i)));
        }
    }

    private ImageView createImageView(final BufferedImage bi) {
        Image image = SwingFXUtils.toFXImage(ImageHelper.rescaleImage(bi, 85, 110), null);
        ImageView thumbnail = new ImageView(image);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GRAY);
        Glow glow = new Glow();
        glow.setInput(shadow);

        thumbnail.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                thumbnailBox.getChildren().forEach(n -> n.setEffect(null)); // clear effect on all
                thumbnail.setEffect(glow); // apply effect
                final Object selectedNode = mouseEvent.getSource();
                final int selectedIndex = thumbnailBox.getChildren().indexOf(selectedNode);
                cbPageNum.getSelectionModel().select(selectedIndex / 2); // take labels into account
            }
        });

        return thumbnail;
    }
}
