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

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Deque;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.IIOImage;
import javax.swing.UIManager;
import static net.sourceforge.vietocr.MenuToolsController.prefs;
import net.sourceforge.vietocr.util.FixedSizeStack;

public class MenuImageController implements Initializable {

    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem miMetadata;
    @FXML
    private MenuItem miAutocrop;
    @FXML
    private MenuItem miCrop;
    @FXML
    private MenuItem mi2x2;
    @FXML
    private MenuItem mi3x3;
    @FXML
    private MenuItem miBrightness;
    @FXML
    private MenuItem miContrast;
    @FXML
    private MenuItem miDeskew;
    @FXML
    private MenuItem miGrayscale;
    @FXML
    private MenuItem miInvert;
    @FXML
    private MenuItem miMonochrome;
    @FXML
    private MenuItem miSharpen;
    @FXML
    private MenuItem miSmooth;
    @FXML
    protected MenuItem miUndo;
    @FXML
    protected CheckMenuItem chmiDoubleSidedPage;
    @FXML
    protected CheckMenuItem chmiScreenshotMode;
    @FXML
    private CheckMenuItem chmiSegmentedRegions;

    private final String strPageSide = "PageSide";
    private final String strScreenshotMode = "ScreenshotMode";
    private static final String strSegmentedRegions = "SegmentedRegions";
    private static final double MINIMUM_DESKEW_THRESHOLD = 0.05d;
    private BufferedImage originalImage;
    Deque<BufferedImage> stack = new FixedSizeStack<BufferedImage>(10);
    protected ResourceBundle bundle;
    private final static Logger logger = Logger.getLogger(MenuImageController.class.getName());

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N
        this.chmiDoubleSidedPage.setSelected(prefs.getBoolean(strPageSide, false));
        this.chmiScreenshotMode.setSelected(prefs.getBoolean(strScreenshotMode, false));
        this.chmiSegmentedRegions.setSelected(prefs.getBoolean(strSegmentedRegions, false));
    }

    void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    @FXML
    private void handleAction(ActionEvent event) {
        List<IIOImage> iioImageList = GuiController.getInstance().iioImageList;
        List<BufferedImage> imageList = GuiController.getInstance().imageList;
        int imageIndex = GuiController.getInstance().imageIndex;
        if (iioImageList == null && (event.getSource() != chmiDoubleSidedPage) && (event.getSource() != chmiScreenshotMode) && event.getSource() != chmiSegmentedRegions) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, bundle.getString("Please_load_an_image."));
            alert.show();
            return;
        }

        if (event.getSource() == miMetadata) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ImageInfoDialog.fxml"));
                Parent root = fxmlLoader.load();
                ImageInfoDialogController controller = fxmlLoader.getController();
                controller.setImage(iioImageList.get(imageIndex));
                Stage imageInfoDialog = new Stage();
                imageInfoDialog.setResizable(false);
                imageInfoDialog.initStyle(StageStyle.UTILITY);
                imageInfoDialog.setAlwaysOnTop(true);
//            imageInfoDialog.setX(prefs.getDouble(strChangeCaseX, imageInfoDialog.getX()));
//            imageInfoDialog.setY(prefs.getDouble(strChangeCaseY, imageInfoDialog.getY()));
                Scene scene1 = new Scene(root);
                imageInfoDialog.setScene(scene1);
                imageInfoDialog.setTitle("Image Properties");
                imageInfoDialog.toFront();
                imageInfoDialog.show();
            } catch (Exception e) {

            }
        } else if (event.getSource() == miAutocrop) {
            menuBar.getScene().setCursor(Cursor.WAIT);

            originalImage = (BufferedImage) iioImageList.get(imageIndex).getRenderedImage();
            BufferedImage croppedImage = net.sourceforge.vietocr.util.ImageHelper.autoCrop(originalImage, 0.1);
            // if same image, skip
            if (originalImage != croppedImage) {
                stack.push(originalImage);
                imageList.set(imageIndex, croppedImage);
                iioImageList.get(imageIndex).setRenderedImage((BufferedImage) croppedImage);
                GuiController.getInstance().loadImage();
            }

            menuBar.getScene().setCursor(Cursor.DEFAULT);
        } else if (event.getSource() == miCrop) {

        } else if (event.getSource() == mi2x2) {

        } else if (event.getSource() == mi3x3) {

        } else if (event.getSource() == miBrightness) {

        } else if (event.getSource() == miContrast) {

        } else if (event.getSource() == miDeskew) {
            menuBar.getScene().setCursor(Cursor.WAIT);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ImageDeskew deskew = new ImageDeskew((BufferedImage) iioImageList.get(imageIndex).getRenderedImage());
                    double imageSkewAngle = deskew.getSkewAngle();

                    if ((imageSkewAngle > MINIMUM_DESKEW_THRESHOLD || imageSkewAngle < -(MINIMUM_DESKEW_THRESHOLD))) {
                        originalImage = (BufferedImage) iioImageList.get(imageIndex).getRenderedImage();
                        stack.push(originalImage);
                        BufferedImage rotatedImage = rotateImage(originalImage, Math.toRadians(-imageSkewAngle));
                        imageList.set(imageIndex, rotatedImage); // persist the rotated image
                        iioImageList.get(imageIndex).setRenderedImage(rotatedImage);
                        GuiController.getInstance().loadImage();
                    }
                    menuBar.getScene().setCursor(Cursor.DEFAULT);
                }
            });
        } else if (event.getSource() == miGrayscale) {

        } else if (event.getSource() == miInvert) {

        } else if (event.getSource() == miMonochrome) {

        } else if (event.getSource() == miSharpen) {

        } else if (event.getSource() == miSmooth) {

        } else if (event.getSource() == miUndo) {
            if (stack.isEmpty()) {
                return;
            }
            BufferedImage image = stack.pop();
            imageList.set(imageIndex, image);
            iioImageList.get(imageIndex).setRenderedImage(image);
            GuiController.getInstance().loadImage();
        } else if (event.getSource() == chmiDoubleSidedPage) {
            Label labelDoubleSidedPage = (Label) menuBar.getScene().lookup("#labelDoubleSidedPage");
            labelDoubleSidedPage.setText(this.chmiDoubleSidedPage.isSelected() ? "Double" : "Single");
        } else if (event.getSource() == chmiScreenshotMode) {
            Label labelScreenShotMode = (Label) menuBar.getScene().lookup("#labelScreenShotMode");
            labelScreenShotMode.setText(this.chmiScreenshotMode.isSelected() ? "On" : "Off");
        } else if (event.getSource() == chmiSegmentedRegions) {
            Node btnSegmentedRegions = menuBar.getScene().lookup("#btnSegmentedRegions");
            btnSegmentedRegions.setVisible(chmiSegmentedRegions.isSelected());
        }
    }

//    @Override
    void clearStack() {
        stack.clear();
    }

//     /**
//     * Rotates image.
//     *
//     * @param angle the degree of rotation
//     */
//    void rotateImage(double angle) {
//        try {
//            Image imageIcon = rotateImage(imageList.get(imageIndex),Math.toRadians(angle));
//            imageList.set(imageIndex, imageIcon); // persist the rotated image
//            iioImageList.get(imageIndex).setRenderedImage((BufferedImage) imageIcon.getImage());
//            GuiController.getInstance().loadImage();
//        } catch (OutOfMemoryError oome) {
//            logger.log(Level.SEVERE, oome.getMessage(), oome);
//            //JOptionPane.showMessageDialog(this, oome.getMessage(), bundle.getString("OutOfMemoryError"), JOptionPane.ERROR_MESSAGE);
//        }
//    }
    /**
     * Gets a rotated image.
     *
     * @param angle
     * @return
     */
    public BufferedImage rotateImage(BufferedImage image, double angle) {
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int w = image.getWidth();
        int h = image.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(newW, newH);

        Graphics2D g2d = result.createGraphics();
        g2d.setColor(UIManager.getColor("Label.background"));
        g2d.fillRect(0, 0, newW, newH);

        g2d.translate((newW - w) / 2, (newH - h) / 2);
        g2d.rotate(angle, w / 2, h / 2);
        g2d.drawRenderedImage(image, null);

        g2d.dispose();

        return result;
    }

    /**
     * Gets graphic default configuration.
     *
     * @return
     */
    private GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    void savePrefs() {
        prefs.putBoolean(strPageSide, this.chmiDoubleSidedPage.isSelected());
        prefs.putBoolean(strScreenshotMode, this.chmiScreenshotMode.isSelected());
        prefs.putBoolean(strSegmentedRegions, this.chmiSegmentedRegions.isSelected());
    }
}
