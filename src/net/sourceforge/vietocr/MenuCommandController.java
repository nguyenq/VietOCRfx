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

import java.awt.Rectangle;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.IIOImage;

public class MenuCommandController implements Initializable {

    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem miOCR;
    @FXML
    private MenuItem miOCRAll;
    @FXML
    private MenuItem miPostProcess;
    @FXML
    private MenuItem miBulkOCR;

    private ImageView imageView;
    private TextArea textarea;
    private ProgressBar progressBar1;
    private Label labelStatus;
    private Button btnCancelOCR;
    private Button btnOCR;
    private CheckMenuItem chmiScreenshotMode;

    private double scaleX, scaleY;
    List<IIOImage> iioImageList;
    String inputfilename, curLangCode;
    int imageIndex;
    String tessPath, datapath;
    String selectedPSM;
    private OCRImageEntity entity;
    private OcrWorker ocrWorker;

    protected ResourceBundle bundle;

    private final static Logger logger = Logger.getLogger(MenuCommandController.class.getName());

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
        entity = (OCRImageEntity) menuBar.getUserData();
    }

    @FXML
    private void handleAction(ActionEvent event) {
        Scene scene = menuBar.getScene();

        textarea = (TextArea) scene.lookup("#textarea");
        progressBar1 = (ProgressBar) scene.lookup("#progressBar1");
        imageView = (ImageView) scene.lookup("#imageView");

        labelStatus = (Label) scene.lookup("#labelStatus");
        btnCancelOCR = (Button) scene.lookup("#btnCancelOCR");
        btnOCR = (Button) scene.lookup("#btnOCR");
//        chmiScreenshotMode = (CheckMenuItem) menuBar.getMenus()(2).lookup("#chmiScreenshotMode");

        if (event.getSource() == miOCR) {
            ((Button) menuBar.getScene().lookup("#btnOCR")).fire();
        } else if (event.getSource() == miOCRAll) {
            miOCRAllActionPerformed(event);
        } else if (event.getSource() == miPostProcess) {
            ((Button) menuBar.getScene().lookup("#btnPostProcess")).fire();
        } else if (event.getSource() == miBulkOCR) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BulkDialog.fxml"));
                Parent root = fxmlLoader.load();
                BulkDialogController controller = fxmlLoader.getController();
                Stage bulkDialog = new Stage();
                bulkDialog.setResizable(false);
                bulkDialog.initStyle(StageStyle.UTILITY);
                bulkDialog.setAlwaysOnTop(true);
//            bulkDialog.setX(prefs.getDouble(strChangeCaseX, changeCaseDialog.getX()));
//            bulkDialog.setY(prefs.getDouble(strChangeCaseY, changeCaseDialog.getY()));
                Scene scene1 = new Scene(root);
                bulkDialog.setScene(scene1);
                bulkDialog.setTitle("Bulk OCR");
                bulkDialog.toFront();
                bulkDialog.show();
            } catch (Exception e) {

            }
        }
    }
    
    void miOCRAllActionPerformed(ActionEvent evt) {
        if (this.imageView.getImage() == null) {
            new Alert(Alert.AlertType.NONE, bundle.getString("Please_load_an_image."), ButtonType.OK).showAndWait();
            return;
        }

        this.btnOCR.setVisible(false);
        this.btnCancelOCR.setVisible(true);
        this.btnCancelOCR.setDisable(false);
        performOCR(iioImageList, inputfilename, -1, null);
    }

    /**
     * Perform OCR on images represented by IIOImage.
     *
     * @param iioImageList list of IIOImage
     * @param inputfilename input filename
     * @param index Index of page to be OCRed: -1 for all pages
     * @param rect region of interest
     */
    void performOCR(final List<IIOImage> iioImageList, String inputfilename, final int index, Rectangle rect) {
        if (curLangCode.trim().length() == 0) {
            new Alert(Alert.AlertType.NONE, bundle.getString("Please_select_a_language."), ButtonType.OK).showAndWait();
            return;
        }

        labelStatus.setText(bundle.getString("OCR_running..."));
        progressBar1.setVisible(true);
        //Cursor.WAIT_CURSOR));
        this.btnOCR.setDisable(true);
        this.miOCR.setDisable(true);
        this.miOCRAll.setDisable(true);

        entity = new OCRImageEntity(iioImageList, inputfilename, index, rect, curLangCode);
        entity.setScreenshotMode(this.chmiScreenshotMode.isSelected());

        // instantiate Task for OCR
        ocrWorker = new OcrWorker(entity);
        new Thread(ocrWorker).start();
    }

    void btnCancelOCRActionPerformed(java.awt.event.ActionEvent evt) {
        if (ocrWorker != null && !ocrWorker.isDone()) {
            // Cancel current OCR op to begin a new one. You want only one OCR op at a time.
            ocrWorker.cancel(true);
            ocrWorker = null;
        }

        this.btnCancelOCR.setDisable(true);
    }

    /**
     * A worker class for managing OCR process.
     */
    class OcrWorker extends Task<Void> {

        OCRImageEntity entity;
        List<File> workingFiles;
        List<IIOImage> imageList; // Option for Tess4J

        OcrWorker(OCRImageEntity entity) {
            this.entity = entity;
        }

        @Override
        protected Void call() throws Exception {
            String lang = entity.getLanguage();

            OCR<IIOImage> ocrEngine = new OCRImages(tessPath); // for Tess4J
            ocrEngine.setDatapath(datapath);
            ocrEngine.setPageSegMode(selectedPSM);
            ocrEngine.setLanguage(lang);
            imageList = entity.getSelectedOimages();

            for (int i = 0; i < imageList.size(); i++) {
                if (!isCancelled()) {
                    String result = ocrEngine.recognizeText(imageList.subList(i, i + 1), entity.getInputfilename(), entity.getRect());
//                    publish(result); // interim result
                }
            }

            return null;
        }

        protected void process(List<String> results) {
            for (String str : results) {
                textarea.appendText(str);
                textarea.selectPositionCaret(textarea.getLength());
            }
        }

        @Override
        protected void done() {

            try {
                get(); // dummy method
                labelStatus.setText(bundle.getString("OCR_completed."));
            } catch (InterruptedException ignore) {
                logger.log(Level.WARNING, ignore.getMessage(), ignore);
            } catch (java.util.concurrent.ExecutionException e) {
                String why;
                Throwable cause = e.getCause();
                if (cause != null) {
                    if (cause instanceof IOException) {
                        why = bundle.getString("Cannot_find_Tesseract._Please_set_its_path.");
                    } else if (cause instanceof FileNotFoundException) {
                        why = bundle.getString("An_exception_occurred_in_Tesseract_engine_while_recognizing_this_image.");
                    } else if (cause instanceof OutOfMemoryError) {
                        why = cause.getMessage();
                    } else if (cause instanceof ClassCastException) {
                        why = cause.getMessage();
                        why += "\nConsider converting the image to binary or grayscale before OCR again.";
                    } else {
                        why = cause.getMessage();
                    }
                } else {
                    why = e.getMessage();
                }

                logger.log(Level.SEVERE, why, e);
                labelStatus.setText(null);
                //JOptionPane.showMessageDialog(null, why, "OCR Operation", JOptionPane.ERROR_MESSAGE);
            } catch (java.util.concurrent.CancellationException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                labelStatus.setText("OCR " + bundle.getString("canceled"));
            } finally {
                //Cursor.DEFAULT_CURSOR));
                btnOCR.setVisible(true);
                btnOCR.setDisable(false);
                miOCR.setDisable(false);
                miOCRAll.setDisable(false);
                btnCancelOCR.setVisible(false);

                // clean up temporary image files
                if (workingFiles != null) {
                    for (File tempImageFile : workingFiles) {
                        tempImageFile.delete();
                    }
                }
            }
        }
    }

}
