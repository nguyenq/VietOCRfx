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

import java.awt.Rectangle;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javax.imageio.IIOImage;
import net.sourceforge.vietocr.util.Utils;
import net.sourceforge.vietpad.utilities.TextUtilities;

public class GuiWithOCR extends GuiWithImageOps {

    @FXML
    private Button btnOCR;
    @FXML
    private Button btnOCRAll;
    @FXML
    private Button btnCancelOCR;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnRemoveLineBreaks;
    @FXML
    private Button btnPostProcess;
    @FXML
    private ComboBox cbOCRLanguage;

    static GuiWithOCR instance;

    protected final File supportDir = new File(System.getProperty("user.home")
            + (MAC_OS_X ? "/Library/Application Support/" + VietOCR.APP_NAME : "/." + VietOCR.APP_NAME.toLowerCase()));
    static final String UTF8 = "UTF-8";
    static final String strUILanguage = "UILanguage";
    static final String TESSDATA = "tessdata";

    private final String DATAFILE_SUFFIX = ".traineddata";
    protected final File baseDir = Utils.getBaseDir(GuiWithOCR.this);
    protected String datapath;
    protected String tessPath;
    protected Properties lookupISO639;
    protected Properties lookupISO_3_1_Codes;
    protected String curLangCode = "eng";
    private String[] installedLanguageCodes;
    protected String[] installedLanguages;
    private static final String strLangCode = "langCode";
    private static final String strTessDir = "TesseractDirectory";
    Task ocrWorker;
    protected String selectedPSM = "3"; // 3 - Fully automatic page segmentation, but no OSD (default)

    private final static Logger logger = Logger.getLogger(GuiWithOCR.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);

        instance = this;

        btnCancelOCR.managedProperty().bind(btnCancelOCR.visibleProperty());
        getInstalledLanguagePacks();
        populateOCRLanguageBox();
    }

    /**
     * Gets GuiController instance (for child controllers).
     *
     * @return
     */
    public static GuiWithOCR getInstance() {
        return instance;
    }

    /**
     *
     * @param event
     */
    @FXML
    @Override
    protected void handleAction(javafx.event.ActionEvent event) {
        if (event.getSource() == btnOCR) {
            if (this.imageView.getImage() == null) {
                new Alert(Alert.AlertType.INFORMATION, bundle.getString("Please_load_an_image.")).show();
                return;
            }

            Rectangle rect = null; //((JImageLabel) imageView).getRect();

            if (rect != null) {
                try {
                    Image ii = (Image) this.imageView.getImage();
                    int offsetX = 0;
                    int offsetY = 0;
//                    if (ii.getIconWidth() < this.jScrollPaneImage.getWidth()) {
//                        offsetX = (this.jScrollPaneImage.getViewport().getWidth() - ii.getIconWidth()) / 2;
//                    }
//                    if (ii.getIconHeight() < this.jScrollPaneImage.getHeight()) {
//                        offsetY = (this.jScrollPaneImage.getViewport().getHeight() - ii.getIconHeight()) / 2;
//                    }
//                BufferedImage bi = ((BufferedImage) ii.getImage()).getSubimage((int) ((rect.x - offsetX) * scaleX), (int) ((rect.y - offsetY) * scaleY), (int) (rect.width * scaleX), (int) (rect.height * scaleY));

//                // create a new rectangle with scale factors and offets factored in
                    rect = new Rectangle((int) ((rect.x - offsetX) * scaleX), (int) ((rect.y - offsetY) * scaleY), (int) (rect.width * scaleX), (int) (rect.height * scaleY));

                    //move this part to the image entity
//                ArrayList<IIOImage> tempList = new ArrayList<IIOImage>();
//                tempList.add(new IIOImage(bi, null, null));
                    performOCR(iioImageList, inputfilename, imageIndex, rect);
                } catch (RasterFormatException rfe) {
                    logger.log(Level.SEVERE, rfe.getMessage(), rfe);
                    //JOptionPane.showMessageDialog(this, rfe.getMessage(), APP_NAME, JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            } else {
                performOCR(iioImageList, inputfilename, imageIndex, null);
            }
        } else if (event.getSource() == btnOCRAll) {
            if (this.imageView.getImage() == null) {
                new Alert(Alert.AlertType.INFORMATION, bundle.getString("Please_load_an_image.")).show();
                return;
            }
            this.btnOCR.setVisible(false);
            this.btnCancelOCR.setVisible(true);
            this.btnCancelOCR.setDisable(false);
            performOCR(iioImageList, inputfilename, -1, null);
        } else if (event.getSource() == btnCancelOCR) {
            if (ocrWorker != null && !ocrWorker.isDone()) {
                // Cancel current OCR op to begin a new one. You want only one OCR op at a time.
                ocrWorker.cancel(true);
                ocrWorker = null;
            }

            this.btnCancelOCR.setDisable(true);
        } else if (event.getSource() == btnClear) {
            textarea.clear();
        } else if (event.getSource() == btnRemoveLineBreaks) {
            if (textarea.getSelectedText().length() == 0) {
                textarea.selectAll();

                if (textarea.getSelectedText().length() == 0) {
                    return;
                }
            }

            String result = TextUtilities.removeLineBreaks(textarea.getSelectedText());

            int start = textarea.getSelection().getStart();
            textarea.replaceSelection(result);
            textarea.selectRange(start, start + result.length());
        } else if (event.getSource() == btnPostProcess) {

        } else if (event.getSource() == cbOCRLanguage) {

        } else {
            super.handleAction(event);
        }
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
            new Alert(Alert.AlertType.INFORMATION, bundle.getString("Please_select_a_language.")).show();
            return;
        }

        progressBar.setVisible(true);
        labelStatus.getScene().setCursor(Cursor.WAIT);
        this.btnOCR.setDisable(true);
//        this.miOCR.setDisable(true);
//        this.miOCRAll.setDisable(true);

        OCRImageEntity entity = new OCRImageEntity(iioImageList, inputfilename, index, rect, curLangCode);
//        entity.setScreenshotMode(this.checkBoxMenuItemScreenshotMode.isSelected());

        // instantiate SwingWorker for OCR
        ocrWorker = new OcrWorker(entity);
        progressBar.progressProperty().bind(ocrWorker.progressProperty());
        labelStatus.textProperty().bind(ocrWorker.messageProperty());
        ocrWorker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                textarea.appendText(newValue.toString());
            }
        });
//        ocrWorker.exceptionProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                Exception ex = (Exception) newValue;
//                ex.printStackTrace();
//            }
//        });
        new Thread(ocrWorker).start();
    }

    private void getInstalledLanguagePacks() {
        if (WINDOWS) {
            tessPath = baseDir.getPath();
            datapath = tessPath + "/tessdata";
        } else {
            tessPath = prefs.get(strTessDir, "/usr/bin");
            datapath = "/usr/share/tesseract-ocr/4.00/tessdata";
        }

        lookupISO639 = new Properties();
        lookupISO_3_1_Codes = new Properties();

        try {
            File tessdataDir = new File(tessPath, TESSDATA);
            if (!tessdataDir.exists()) {
                String TESSDATA_PREFIX = System.getenv("TESSDATA_PREFIX");
                if (TESSDATA_PREFIX == null && !WINDOWS) { // if TESSDATA_PREFIX env var not set
                    if (tessPath.equals("/usr/bin")) { // default install path of Tesseract on Linux
                        TESSDATA_PREFIX = "/usr/share/tesseract-ocr/"; // default install path of tessdata on Linux
                    } else {
                        TESSDATA_PREFIX = "/usr/local/share/"; // default make install path of tessdata on Linux
                    }
                }
                tessdataDir = new File(TESSDATA_PREFIX, TESSDATA);
                datapath = TESSDATA_PREFIX;
            }

            installedLanguageCodes = tessdataDir.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(DATAFILE_SUFFIX) && !name.equals("osd.traineddata");
                }
            });
            Arrays.sort(installedLanguageCodes, Collator.getInstance());

            File xmlFile = new File(baseDir, "data/ISO639-3.xml");
            lookupISO639.loadFromXML(new FileInputStream(xmlFile));
            xmlFile = new File(baseDir, "data/ISO639-1.xml");
            lookupISO_3_1_Codes.loadFromXML(new FileInputStream(xmlFile));
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
//            JOptionPane.showMessageDialog(null, e.getMessage(), VietOCR.APP_NAME, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            if (installedLanguageCodes == null) {
                installedLanguages = new String[0];
            } else {
                installedLanguages = new String[installedLanguageCodes.length];
            }
            for (int i = 0; i < installedLanguages.length; i++) {
                installedLanguageCodes[i] = installedLanguageCodes[i].replace(DATAFILE_SUFFIX, "");
                installedLanguages[i] = lookupISO639.getProperty(installedLanguageCodes[i], installedLanguageCodes[i]);
            }
        }
    }

    /**
     * Populates OCR Language box.
     */
    @SuppressWarnings("unchecked")
    private void populateOCRLanguageBox() {
        if (installedLanguageCodes == null) {
            new Alert(Alert.AlertType.INFORMATION, bundle.getString("Tesseract_is_not_found._Please_specify_its_path_in_Settings_menu.")).show();
            return;
        }

        cbOCRLanguage.getItems().addAll(FXCollections.observableArrayList(installedLanguages));
        
        cbOCRLanguage.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                for (Object key : lookupISO639.keySet()) {
                    if (lookupISO639.getProperty(key.toString()).equals(newValue)) {
                        curLangCode = key.toString();
                        break;
                    }
                }
            }
        });
        
        cbOCRLanguage.getSelectionModel().select(lookupISO639.getProperty(prefs.get(strLangCode, null)));
    }
    
    @Override
    public void savePrefs() {
        prefs.put(strLangCode, curLangCode);
        super.savePrefs();
    }

    /**
     * A worker class for managing OCR process.
     */
    class OcrWorker extends Task<String> {

        OCRImageEntity entity;
        List<File> workingFiles;
        List<IIOImage> imageList; // Option for Tess4J

        OcrWorker(OCRImageEntity entity) {
            this.entity = entity;
        }

        @Override
        protected String call() throws Exception {
            updateMessage(bundle.getString("OCR_running..."));
            String lang = entity.getLanguage();

            OCR<IIOImage> ocrEngine = new OCRImages(tessPath); // for Tess4J
            ocrEngine.setDatapath(datapath);
            ocrEngine.setPageSegMode(selectedPSM);
            ocrEngine.setLanguage(lang);
            imageList = entity.getSelectedOimages();

            for (int i = 0; i < imageList.size(); i++) {
                if (isCancelled()) {
                    break;
                }
                String result = ocrEngine.recognizeText(imageList.subList(i, i + 1), entity.getInputfilename(), entity.getRect());
                updateValue(result); // interim result
            }
            return null;
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            updateMessage(bundle.getString("OCR_completed."));
            updateProgress(1, 1);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.progressProperty().unbind();
                    reset();
                }
            });
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            updateMessage("OCR " + bundle.getString("canceled"));
            updateProgress(0, 1);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    reset();
                }
            });
        }

        @Override
        protected void failed() {
            super.failed();
            updateMessage("Failed!");
            updateProgress(0, 1);
            reset();
        }

        void reset() {
            btnOCR.setVisible(true);
            btnOCR.setDisable(false);
//                miOCR.setDisable(false);
//                miOCRAll.setDisable(false);
            btnCancelOCR.setVisible(false);
            progressBar.setDisable(true);
            labelStatus.getScene().setCursor(Cursor.DEFAULT);
            textarea.setCursor(Cursor.DEFAULT);

            // clean up temporary image files
            if (workingFiles != null) {
                for (File tempImageFile : workingFiles) {
                    tempImageFile.delete();
                }
            }
        }
    }
}
