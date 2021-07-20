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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import net.sourceforge.vietocr.util.*;

public class GuiWithThumbnail extends GuiController {

    @FXML
    private ScrollPane thumbnailScrollpane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        thumbnailBox.prefWidthProperty().bind(thumbnailScrollpane.widthProperty());
        thumbnailBox.prefHeightProperty().bind(thumbnailScrollpane.heightProperty());
    }

    @Override
    void loadThumbnails() {
        LoadThumbnailWorker worker = new LoadThumbnailWorker(imageList);
        worker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                thumbnailBox.getChildren().addAll(newValue);
            }
        });
        new Thread(worker).start();
    }

    private ImageView createImageView(final BufferedImage bi) {
        Image image = SwingFXUtils.toFXImage(ImageHelper.rescaleImage(bi, 85, 110), null);
        ImageView thumbnail = new ImageView(image);

        thumbnail.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                thumbnailBox.getChildren().forEach(n -> n.setEffect(null)); // clear effect on all
                thumbnail.setEffect(glow); // apply effect
                final Object selectedNode = mouseEvent.getSource();
                int selectedIndex = thumbnailBox.getChildren().stream()
                        .filter(ImageView.class::isInstance)
                        .collect(Collectors.toList())
                        .indexOf(selectedNode);
                cbPageNum.getSelectionModel().select(selectedIndex); // take labels into account
            }
        });

        return thumbnail;
    }

    /**
     * A worker class for loading thumbnails.
     */
    class LoadThumbnailWorker extends Task<List<Node>> {

        List<BufferedImage> imageList;

        LoadThumbnailWorker(List<BufferedImage> imageList) {
            this.imageList = imageList;
        }

        @Override
        protected List<Node> call() throws Exception {
            int i = 0;
            for (final BufferedImage bi : imageList) {
                ImageView iv = createImageView(bi);
                if (i == 0) {
                    iv.setEffect(glow);
                }
                updateValue(Arrays.asList(iv, new Label(String.valueOf(i + 1))));
                i++;
            }

            return null;
        }
    }
}
