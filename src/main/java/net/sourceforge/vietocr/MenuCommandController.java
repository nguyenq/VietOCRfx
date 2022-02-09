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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
import javafx.stage.Stage;
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
    private ProgressBar progressBar;
    private Label labelStatus;
    private Button btnCancelOCR;
    private Button btnOCR;
    private CheckMenuItem chmiScreenshotMode;
    private CheckMenuItem chmiDoubleSidedPage;
    private Stage statusDialog;

    static final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr");

    private BulkDialogController dialogController;
    private StatusDialogController statusDialogController;
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
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N
        outputFormats = prefs.get(strBulkOutputFormat, "TEXT");
        inputFolder = prefs.get(strInputFolder, System.getProperty("user.home"));
        outputFolder = prefs.get(strBulkOutputFolder, System.getProperty("user.home"));
    }

    void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
        entity = (OCRImageEntity) menuBar.getUserData();
    }

    @FXML
    private void handleAction(ActionEvent event) {
        Scene scene = menuBar.getScene();

        textarea = (TextArea) scene.lookup("#textarea");
        progressBar = (ProgressBar) scene.lookup("#progressBar");
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

                if (statusDialogController == null) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StatusDialog.fxml"));
                    Parent root = fxmlLoader.load();
                    statusDialogController = fxmlLoader.getController();
                    statusDialog = new Stage();
                    Scene scene1 = new Scene(root);
                    statusDialog.setScene(scene1);
                    statusDialog.setTitle("Bulk Status");
                }

                Optional<ButtonType> result = dialogController.showAndWait();
                if (result.isPresent()) {
                    if (result.get().getButtonData() == ButtonData.OK_DONE) {
                        inputFolder = dialogController.getInDirectory();
                        outputFolder = dialogController.getOutDirectory();
                        outputFormats = dialogController.getSelectedOutputFormats();
                        List<File> files = new ArrayList<File>();
                        Utils.listImageFiles(files, new File(inputFolder));
                        statusDialog.toFront();
                        statusDialog.show();
                        statusDialogController.getTextArea().appendText("\t-- " + bundle.getString("Beginning_of_task") + " --\n");

                        // instantiate Task for OCR
                        BulkOcrWorker ocrWorker = new BulkOcrWorker(files);
                        progressBar.progressProperty().bind(ocrWorker.progressProperty());
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
        TextArea textareaMonitor = statusDialogController.getTextArea();

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
                        Platform.runLater(() -> {
                            textareaMonitor.appendText(imageFile.getPath() + "\n");
                        });
                        String outputFilename = imageFile.getPath().substring(inputFolder.length() + 1);
                        TesseractParameters tesseractParameters = GuiWithOCR.instance.tesseractParameters;
                        OCRHelper.performOCR(imageFile, new File(outputFolder, outputFilename), tesseractParameters.getDatapath(), tesseractParameters.getLangCode(), tesseractParameters.getPsm(), outputFormats, GuiWithOCR.instance.options);

                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                        updateMessage("\t** " + bundle.getString("Cannotprocess") + " " + imageFile.getName() + " **");
                    }
                }
            }
            return null;
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            updateMessage(bundle.getString("OCR_completed."));
            updateProgress(1, 1);

            Platform.runLater(() -> {
                progressBar.progressProperty().unbind();
                statusDialogController.getTextArea().appendText("\t-- " + bundle.getString("End_of_task") + " --\n");
                reset();
            });
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            updateMessage("OCR " + bundle.getString("canceled"));
            updateProgress(0, 1);

            Platform.runLater(() -> {
                reset();
            });
        }

        @Override
        protected void failed() {
            super.failed();
            updateMessage("Failed!");
            updateProgress(0, 1);
            Platform.runLater(() -> {
                reset();
            });
        }

        private void reset() {
            btnOCR.setVisible(true);
            btnOCR.setDisable(false);
            btnCancelOCR.setVisible(false);
            progressBar.setDisable(true);
//            splitPane.setCursor(Cursor.DEFAULT);
//            statusBar.setCursor(Cursor.DEFAULT);
            long millis = System.currentTimeMillis() - startTime;
            String elapsedTime = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            textareaMonitor.appendText("\t" + bundle.getString("Elapsed_time") + ": " + elapsedTime + "\n");
        }
    }
}
