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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javax.imageio.IIOImage;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.vietocr.util.Utils;
import net.sourceforge.vietpad.inputmethod.VietKeyListener;
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
    private MenuButton mbtnOCRLanguage;
    @FXML
    private MenuButton btnSegmentedRegions;
    @FXML
    private CheckMenuItem chbCharacter;
    @FXML
    private CheckMenuItem chbBlock;
    @FXML
    private CheckMenuItem chbWord;
    @FXML
    private CheckMenuItem chbTextLine;
    @FXML
    private CheckMenuItem chbParagraph;

    static GuiWithOCR instance;
    
    private static final String strSegmentedRegionsPara = "SegmentedRegionsPara";
    private static final String strSegmentedRegionsTextLine = "SegmentedRegionsTextLine";
    private static final String strSegmentedRegionsSymbol = "SegmentedRegionsSymbol";
    private static final String strSegmentedRegionsBlock = "SegmentedRegionsBlock";
    private static final String strSegmentedRegionsWord = "SegmentedRegionsWord";

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
        
        this.chbParagraph.setSelected(prefs.getBoolean(strSegmentedRegionsPara, false));
        this.chbCharacter.setSelected(prefs.getBoolean(strSegmentedRegionsSymbol, false));
        this.chbTextLine.setSelected(prefs.getBoolean(strSegmentedRegionsTextLine, false));
        this.chbBlock.setSelected(prefs.getBoolean(strSegmentedRegionsBlock, false));
        this.chbWord.setSelected(prefs.getBoolean(strSegmentedRegionsWord, false));

        btnCancelOCR.managedProperty().bind(btnCancelOCR.visibleProperty());
        getInstalledLanguagePacks();
        populateOCRLanguageBox();
        new VietKeyListener(textarea);
        btnSegmentedRegions.visibleProperty().addListener((observable) -> {
            setSegmentedRegions();
        });
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

            javafx.scene.shape.Rectangle roi = selectionBox.getRect();
            Rectangle rect = new Rectangle((int) roi.getX(), (int) roi.getY(), (int) roi.getWidth(), (int) roi.getHeight());

            if (!rect.isEmpty()) {
                try {
                    Image ii = (Image) this.imageView.getImage();
                    double offsetX = 0;
                    double offsetY = 0;
                    if (ii.getWidth() < this.scrollPaneImage.getWidth()) {
                        offsetX = (this.scrollPaneImage.getViewportBounds().getWidth() - ii.getWidth()) / 2;
                    }
                    if (ii.getHeight() < this.scrollPaneImage.getHeight()) {
                        offsetY = (this.scrollPaneImage.getViewportBounds().getHeight() - ii.getHeight()) / 2;
                    }
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

        } else if (event.getSource() == chbCharacter || event.getSource() == chbWord || event.getSource() == chbTextLine || event.getSource() == chbParagraph || event.getSource() == chbBlock) {
            setSegmentedRegions();
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
        splitPane.setCursor(Cursor.WAIT);
        statusBar.setCursor(Cursor.WAIT);
        this.btnOCR.setDisable(true);
//        this.miOCR.setDisable(true);
//        this.miOCRAll.setDisable(true);

        OCRImageEntity entity = new OCRImageEntity(iioImageList, inputfilename, index, rect, false, curLangCode);
//        entity.setScreenshotMode(this.checkBoxMenuItemScreenshotMode.isSelected());

        // instantiate Task for OCR
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

        final List<String> selectedOCRLangs = new ArrayList<>();
        final List<String> selectedLangCodes = new ArrayList<>();
        BooleanProperty isViet = new SimpleBooleanProperty();
        
        for (int i = 0; i < installedLanguages.length; i++) {
            String lang = installedLanguages[i];
            CheckMenuItem item = new CheckMenuItem(lang);
            item.setUserData(installedLanguageCodes[i]);
            item.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue) {
                    selectedOCRLangs.add(item.getText());
                    selectedLangCodes.add((String) item.getUserData());
                } else {
                    selectedOCRLangs.remove(item.getText());
                    selectedLangCodes.remove((String) item.getUserData());
                }

                curLangCode = String.join("+", selectedLangCodes);
                mbtnOCRLanguage.setText(String.join("+", selectedOCRLangs));
                
                isViet.set(curLangCode.contains("vie"));
                VietKeyListener.setVietModeEnabled(isViet.get());
            });

            mbtnOCRLanguage.getItems().add(item);
        }
        
        // Show Viet Input Method submenu if selected OCR language is Vietnamese
        Menu settingsMenu = (Menu) menuBar.getMenus().get(4);
        settingsMenu.getItems().get(0).visibleProperty().bind(isViet);

        String savedLangCodes = prefs.get(strLangCode, "");
        for (String langCode : savedLangCodes.split("\\+")) {
            for (MenuItem item : mbtnOCRLanguage.getItems()) {
                if (langCode.equals((String) item.getUserData())) {
                    ((CheckMenuItem) item).setSelected(true);
                }
            }
        }
    }

    void setSegmentedRegions() {
        if (!btnSegmentedRegions.isVisible() || iioImageList == null || !this.btnActualSize.isDisabled()) {
            selectionBox.setSegmentedRegions(null);
            return;
        }

        try {
            OCR<IIOImage> ocrEngine = new OCRImages(tessPath); // for Tess4J
            ocrEngine.setDatapath(datapath);
            HashMap<Color, List<Rectangle>> map = selectionBox.getSegmentedRegions();
            if (map == null) {
                map = new HashMap<Color, List<Rectangle>>();
            }

            IIOImage image = iioImageList.get(imageIndex);

            List<Rectangle> regions;

            if (chbBlock.isSelected()) {
                if (!map.containsKey(Color.GRAY)) {
                    regions = ocrEngine.getSegmentedRegions(image, ITessAPI.TessPageIteratorLevel.RIL_BLOCK);
                    map.put(Color.GRAY, regions);
                }
            } else {
                map.remove(Color.GRAY);
            }

            if (chbParagraph.isSelected()) {
                if (!map.containsKey(Color.GREEN)) {
                    regions = ocrEngine.getSegmentedRegions(image, ITessAPI.TessPageIteratorLevel.RIL_PARA);
                    map.put(Color.GREEN, regions);
                }
            } else {
                map.remove(Color.GREEN);
            }

            if (chbTextLine.isSelected()) {
                if (!map.containsKey(Color.RED)) {
                    regions = ocrEngine.getSegmentedRegions(image, ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
                    map.put(Color.RED, regions);
                }
            } else {
                map.remove(Color.RED);
            }

            if (chbWord.isSelected()) {
                if (!map.containsKey(Color.BLUE)) {
                    regions = ocrEngine.getSegmentedRegions(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
                    map.put(Color.BLUE, regions);
                }
            } else {
                map.remove(Color.BLUE);
            }

            if (chbCharacter.isSelected()) {
                if (!map.containsKey(Color.MAGENTA)) {
                    regions = ocrEngine.getSegmentedRegions(image, ITessAPI.TessPageIteratorLevel.RIL_SYMBOL);
                    map.put(Color.MAGENTA, regions);
                }
            } else {
                map.remove(Color.MAGENTA);
            }

            selectionBox.setSegmentedRegions(null);
            selectionBox.setSegmentedRegions(map);
        } catch (Exception ex) {
            Logger.getLogger(GuiController.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    public void savePrefs() {
        prefs.put(strLangCode, curLangCode);
        prefs.putBoolean(strSegmentedRegionsPara, this.chbParagraph.isSelected());
        prefs.putBoolean(strSegmentedRegionsSymbol, this.chbCharacter.isSelected());
        prefs.putBoolean(strSegmentedRegionsTextLine, this.chbTextLine.isSelected());
        prefs.putBoolean(strSegmentedRegionsBlock, this.chbBlock.isSelected());
        prefs.putBoolean(strSegmentedRegionsWord, this.chbWord.isSelected());
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
            splitPane.setCursor(Cursor.DEFAULT);
            statusBar.setCursor(Cursor.DEFAULT);

            // clean up temporary image files
            if (workingFiles != null) {
                for (File tempImageFile : workingFiles) {
                    tempImageFile.delete();
                }
            }
        }
    }
}
