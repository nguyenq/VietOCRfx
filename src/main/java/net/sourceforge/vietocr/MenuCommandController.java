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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javax.imageio.IIOImage;
import net.sourceforge.vietocr.util.Utils;

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

    private final String strInputFolder = "InputFolder";
    private final String strBulkOutputFolder = "BulkOutputFolder";
    private final String strBulkOutputFormat = "BulkOutputFormat";

    private String inputFolder;
    private String outputFolder;
    private String outputFormats;

    private TextArea textarea;
    private ProgressBar progressBar1;
    private Label labelStatus;
    private Button btnCancelOCR;
    private Button btnOCR;
    private CheckMenuItem chmiScreenshotMode;
    private CheckMenuItem chmiDoubleSidedPage;

    static final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr");

    private BulkDialogController dialogController;
    private double scaleX, scaleY;
    List<IIOImage> iioImageList;
    String inputfilename, curLangCode;
    int imageIndex;
    String datapath;
    String selectedPSM;
    private OCRImageEntity entity;
    
    protected ResourceBundle bundle;

    private final static Logger logger = Logger.getLogger(MenuCommandController.class.getName());

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        outputFormats = prefs.get(strBulkOutputFormat, "text");
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
        labelStatus = (Label) scene.lookup("#labelStatus");
        btnCancelOCR = (Button) scene.lookup("#btnCancelOCR");
        btnOCR = (Button) scene.lookup("#btnOCR");
//        chmiScreenshotMode = (CheckMenuItem) menuBar.getMenus()(2).lookup("#chmiScreenshotMode");

        if (event.getSource() == miOCR) {
            ((Button) menuBar.getScene().lookup("#btnOCR")).fire();
        } else if (event.getSource() == miOCRAll) {
            ((Button) menuBar.getScene().lookup("#btnOCRAll")).fire();
        } else if (event.getSource() == miPostProcess) {
            ((Button) menuBar.getScene().lookup("#btnPostProcess")).fire();
        } else if (event.getSource() == miBulkOCR) {
            try {
                if (dialogController == null) {
                    dialogController = new BulkDialogController(scene.getWindow());
                    dialogController.setSelectedOutputFormats(outputFormats);
                }

                Optional<ButtonType> result = dialogController.showAndWait();
                if (result.isPresent()) {
                    if (result.get().getButtonData() == ButtonData.OK_DONE) {
                        inputFolder = dialogController.getInDirectory();
                        outputFolder = dialogController.getOutDirectory();
                        outputFormats = dialogController.getSelectedOutputFormats();
                        List<File> files = new ArrayList<File>();
                        Utils.listImageFiles(files, new File(inputFolder));

                        // instantiate Task for OCR
                        BulkOcrWorker ocrWorker = new BulkOcrWorker(files);
                        progressBar1.progressProperty().bind(ocrWorker.progressProperty());
                        labelStatus.textProperty().bind(ocrWorker.messageProperty());
                        new Thread(ocrWorker).start();
                    } else if (result.get().getButtonData() == ButtonData.LEFT) {
                        // open Options dialog
                        
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    void savePrefs() {
        if (dialogController != null) {
            prefs.put(strBulkOutputFormat, dialogController.getSelectedOutputFormats());
            prefs.put(strInputFolder, dialogController.getInDirectory());
            prefs.put(strBulkOutputFolder, dialogController.getOutDirectory());
        }
    }

    /**
     * A worker class for managing bulk OCR process.
     */
    class BulkOcrWorker extends Task<Void> {

        long startTime;
        List<File> files;

        BulkOcrWorker(List<File> files) {
            this.files = files;
            startTime = System.currentTimeMillis();
        }

        @Override
        protected Void call() throws Exception {
            for (File imageFile : files) {
                if (!isCancelled()) {
                    updateMessage(imageFile.getPath()); // interim result
                    try {
                        String outputFilename = imageFile.getPath().substring(inputFolder.length() + 1);
                        OCRHelper.performOCR(imageFile, new File(outputFolder, outputFilename), datapath, curLangCode, selectedPSM, outputFormats, GuiWithOCR.instance.options);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                        updateMessage("\t** " + bundle.getString("Cannotprocess") + " " + imageFile.getName() + " **");
                    }
                }
            }
            return null;
        }

        protected void process(List<String> results) {
//            for (String str : results) {
//                textarea.appendText(str);
//                textarea.selectPositionCaret(textarea.getLength());
//            }
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
            }
        }
    }
}
