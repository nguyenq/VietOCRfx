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

import java.awt.image.RenderedImage;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Window;
import javax.imageio.IIOImage;
import net.sourceforge.tess4j.util.ImageIOHelper;

public class ImageInfoDialogController extends Dialog implements Initializable {

    @FXML
    private TextField tfBitDepth;
    @FXML
    private TextField tfHeight;
    @FXML
    private TextField tfWidth;
    @FXML
    private TextField tfXRes;
    @FXML
    private TextField tfYRes;
    @FXML
    private Button btnOK;
    @FXML
    private ChoiceBox<String> cbWidth;
    @FXML
    private ChoiceBox<String> cbHeight;
    IIOImage oimage;
    boolean isProgrammatic;

    public ImageInfoDialogController(Window owner) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ImageInfoDialog.fxml"));
        fxmlLoader.setController(this);
        DialogPane pane = fxmlLoader.load();
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        setTitle("Image Properties");
        setDialogPane(pane);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbWidth.getSelectionModel().selectFirst();
        cbWidth.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                if (!isProgrammatic) {
                    isProgrammatic = true;
                    cbHeight.getSelectionModel().select(cbWidth.getSelectionModel().getSelectedIndex());
                    convertUnits(cbWidth.getSelectionModel().getSelectedIndex());
                    isProgrammatic = false;
                }
            }
        });

        cbHeight.getSelectionModel().selectFirst();
        cbHeight.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                if (!isProgrammatic) {
                    isProgrammatic = true;
                    cbWidth.getSelectionModel().select(cbHeight.getSelectionModel().getSelectedIndex());
                    convertUnits(cbHeight.getSelectionModel().getSelectedIndex());
                    isProgrammatic = false;
                }
            }
        });

//        btnOK.requestFocus();
    }

    /**
     * Converts values between unit systems.
     *
     * @param unit
     */
    private void convertUnits(int unit) {
        int width = oimage.getRenderedImage().getWidth();
        int height = oimage.getRenderedImage().getHeight();

        switch (unit) {
            case 1: // "inches"
                this.tfWidth.setText(String.valueOf(round(width / Float.parseFloat(this.tfXRes.getText()), 1)));
                this.tfHeight.setText(String.valueOf(round(height / Float.parseFloat(this.tfYRes.getText()), 1)));
                break;

            case 2: // "cm"
                this.tfWidth.setText(String.valueOf(round(width / Float.parseFloat(this.tfXRes.getText()) * 2.54, 2)));
                this.tfHeight.setText(String.valueOf(round(height / Float.parseFloat(this.tfYRes.getText()) * 2.54, 2)));
                break;

            default: // "pixel"
                this.tfWidth.setText(String.valueOf(width));
                this.tfHeight.setText(String.valueOf(height));
                break;
        }
    }

    /**
     * Sets image.
     *
     * @param oimage
     */
    public void setImage(IIOImage oimage) {
        this.oimage = oimage;
        readImageData();
    }

    /**
     * Reads image data.
     */
    void readImageData() {
        RenderedImage ri = oimage.getRenderedImage();
        this.tfWidth.setText(String.valueOf(ri.getWidth()));
        this.tfHeight.setText(String.valueOf(ri.getHeight()));
        Map<String, String> metadata = ImageIOHelper.readImageData(oimage);
        this.tfXRes.setText(metadata.get("dpiX"));
        this.tfYRes.setText(metadata.get("dpiY"));
        this.tfBitDepth.setText(String.valueOf(ri.getColorModel().getPixelSize()));
    }

    /**
     * Math rounding operation.
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static double round(double d, int decimalPlace) {
//        BigDecimal bd = new BigDecimal(Double.toString(d));
//        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
//        return bd.doubleValue();
        int temp = (int) (d * Math.pow(10, decimalPlace));
        return ((double) temp) / Math.pow(10, decimalPlace);
    }

}
