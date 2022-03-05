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
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.sourceforge.vietocr.util.FormLocalizer;
import net.sourceforge.vietocr.util.Watcher;
import net.sourceforge.vietpad.inputmethod.InputMethods;
import net.sourceforge.vietpad.inputmethod.VietKeyListener;

public class MenuSettingsController implements Initializable {

    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem miOptions;
    @FXML
    private MenuItem miDownloadLangData;
    @FXML
    private Menu menuPSM;
    @FXML
    private Menu menuIM;
    @FXML
    private MenuItem menuItemSeparatorIM;
    @FXML
    private Menu miUILanguage;

    private OptionsDialogController optionsController;

    private final String strPSM = "PageSegMode";
    private final String strInputMethod = "inputMethod";
    private final String strUILanguage = "UILanguage";

    private final String strWatchFolder = "WatchFolder";
    private final String strOutputFolder = "OutputFolder";
    private final String strWatchEnabled = "WatchEnabled";
    private final String strDeskewEnabled = "DeskewEnabled";
    private final String strPostProcessingEnabled = "PostProcessingEnabled";
    private final String strCorrectLetterCasesEnabled = "CorrectLetterCasesEnabled";
    private final String strRemoveLinesEnabled = "RemoveLinesEnabled";
    private final String strRemoveLineBreaksEnabled = "RemoveLineBreaksEnabled";
    private final String strBatchOutputFormat = "BatchOutputFormat";
    private final String strDangAmbigsPath = "DangAmbigsPath";
    private final String strDangAmbigs = "DangAmbigs";
    private final String strReplaceHyphensEnabled = "ReplaceHyphensEnabled";
    private final String strRemoveHyphensEnabled = "RemoveHyphensEnabled";

    protected String selectedPSM;
    private String selectedInputMethod;
    private String selectedUILang = "en";
    protected String outputFormats;
    protected String dangAmbigsPath;
    protected boolean dangAmbigsOn;
    protected String watchFolder;
    protected String outputFolder;
    protected boolean watchEnabled;
    protected ProcessingOptions options;

    private Stage statusDialog;
    private StatusDialogController statusDialogController;

    private Watcher watcher;
    private Timeline timeline;

    protected ResourceBundle bundle;

    static final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr3");
    private final static Logger logger = Logger.getLogger(MenuSettingsController.class.getName());

    /**
     * Initializes the downloadController class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N
        selectedPSM = prefs.get(strPSM, "3"); // 3 - Fully automatic page segmentation, but no OSD (default)
        watchFolder = prefs.get(strWatchFolder, System.getProperty("user.home"));
        if (!new File(watchFolder).exists()) {
            watchFolder = System.getProperty("user.home");
        }
        outputFolder = prefs.get(strOutputFolder, System.getProperty("user.home"));
        if (!new File(outputFolder).exists()) {
            outputFolder = System.getProperty("user.home");
        }
        watchEnabled = prefs.getBoolean(strWatchEnabled, false);
        outputFormats = prefs.get(strBatchOutputFormat, "text");
        options = new ProcessingOptions();
        dangAmbigsPath = prefs.get(strDangAmbigsPath, new File(System.getProperty("user.home"), "data").getPath());
        dangAmbigsOn = prefs.getBoolean(strDangAmbigs, true);
        options.setDeskew(prefs.getBoolean(strDeskewEnabled, false));
        options.setPostProcessing(prefs.getBoolean(strPostProcessingEnabled, false));
        options.setCorrectLetterCases(prefs.getBoolean(strCorrectLetterCasesEnabled, false));
        options.setRemoveLines(prefs.getBoolean(strRemoveLinesEnabled, false));
        options.setRemoveLineBreaks(prefs.getBoolean(strRemoveLineBreaksEnabled, false));
        options.setReplaceHyphens(prefs.getBoolean(strReplaceHyphensEnabled, false));
        options.setRemoveHyphens(prefs.getBoolean(strRemoveHyphensEnabled, false));
//        Label labelPSMValue = (Label) menuBar.getScene().lookup("#labelPSMValue");
//        labelPSMValue.setText(enumOf(selectedPSM));
        // build PageSegMode submenu
        ToggleGroup groupPSM = new ToggleGroup();
        for (PageSegMode mode : PageSegMode.values()) {
            RadioMenuItem radioItem = new RadioMenuItem(mode.getDesc());
            radioItem.setUserData(mode.getVal());
            radioItem.setSelected(mode.getVal().equals(selectedPSM));
            radioItem.setToggleGroup(groupPSM);
            menuPSM.getItems().add(radioItem);
        }

        groupPSM.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
                if (newToggle != null) {
                    selectedPSM = newToggle.getUserData().toString();
                    GuiWithOCR.getInstance().tesseractParameters.setPsm(selectedPSM);
                    Label labelPSMValue = (Label) menuBar.getScene().lookup("#labelPSMValue");
                    labelPSMValue.setText(enumOf(selectedPSM));
                }
            }
        });

        selectedInputMethod = prefs.get(strInputMethod, "Telex");
        menuItemSeparatorIM.visibleProperty().bind(menuIM.visibleProperty());

        // build Input Method submenu
        ToggleGroup groupIM = new ToggleGroup();
        groupIM.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
                if (newToggle != null) {
                    selectedInputMethod = newToggle.getUserData().toString();
                    VietKeyListener.setInputMethod(InputMethods.valueOf(selectedInputMethod));
                }
            }
        });

        for (InputMethods im : InputMethods.values()) {
            String inputMethod = im.name();
            RadioMenuItem radioItem = new RadioMenuItem(inputMethod);
            radioItem.setUserData(inputMethod);
            radioItem.setSelected(inputMethod.equals(selectedInputMethod));
            radioItem.setToggleGroup(groupIM);
            menuIM.getItems().add(radioItem);
        }

        VietKeyListener.setSmartMark(true);
        VietKeyListener.consumeRepeatKey(true);

        selectedUILang = prefs.get(strUILanguage, "en");
        Locale.setDefault(getLocale(selectedUILang));

        // build UI Language submenu
        ToggleGroup groupUILang = new ToggleGroup();
        groupUILang.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
                if (newToggle != null) {
                    selectedUILang = newToggle.getUserData().toString();
//                    changeUILanguage(getLocale(selectedUILang));
                }
            }
        });

        String[] uiLangs = getInstalledUILangs();
        for (String uiLang : uiLangs) {
            Locale locale = new Locale(uiLang);
            RadioMenuItem radioItem = new RadioMenuItem(locale.getDisplayLanguage());
            radioItem.setUserData(locale.getLanguage());
            radioItem.setSelected(selectedUILang.equals(locale.getLanguage()));
            radioItem.setToggleGroup(groupUILang);
            miUILanguage.getItems().add(radioItem);
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StatusDialog.fxml"));
            Parent root = fxmlLoader.load();
            statusDialogController = fxmlLoader.getController();
            statusDialog = new Stage();
            Scene scene1 = new Scene(root);
            statusDialog.setScene(scene1);
            statusDialog.setTitle("Batch Status");
        } catch (IOException e) {
            // ignore
        }

        // watch for new image files
        final Queue<File> queue = new LinkedList<>();
        watcher = new Watcher(queue, new File(watchFolder));
        watcher.setEnabled(watchEnabled);

        Thread t = new Thread(watcher);
        t.start();

        // autoOCR if there are files in the queue
        timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        actionEvent -> {
                            final File imageFile = queue.poll();
                            performOCR(imageFile);
                        }
                ),
                new KeyFrame(
                        Duration.seconds(10)
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        if (watchEnabled) {
            timeline.play();
        }
    }

    private void performOCR(final File imageFile) {
        if (imageFile != null && imageFile.exists()) {
            if (!statusDialog.isShowing()) {
                statusDialog.show();
            }
            statusDialog.toFront();
            if (statusDialog.isIconified()) {
                statusDialog.setIconified(false);
            } 

            statusDialogController.getTextArea().appendText(imageFile.getPath() + "\n");
            TesseractParameters tesseractParameters = GuiWithOCR.instance.tesseractParameters;
            if (tesseractParameters.getLangCode() == null) {
                statusDialogController.getTextArea().appendText("\t** " + bundle.getString("Please_select_a_language.") + " **\n");
//                        queue.clear();
                return;
            }
            tesseractParameters.setPsm(selectedPSM);

            Platform.runLater(() -> {
                try {
//                        OCRHelper.performOCR(imageFile, new File(outputFolder, imageFile.getName()), tesseractParameters.getDatapath(), tesseractParameters.getLangCode(), tesseractParameters.getPsm(), outputFormats, GuiWithOCR.instance.options);
                    OCRHelper.performOCR(imageFile, new File(outputFolder, imageFile.getName()), tesseractParameters.getDatapath(), tesseractParameters.getLangCode(), tesseractParameters.getPsm(), outputFormats, options);
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                    statusDialogController.getTextArea().appendText("\t** " + bundle.getString("Cannotprocess") + " " + imageFile.getName() + " **\n");
                }
            });
        }
    }

    void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    private String[] getInstalledUILangs() {
        String[] locales = {"bn", "ca", "cs", "en", "de", "fa", "hi", "it", "ja", "lt", "ne", "nl", "pl", "ru", "sk", "tr", "vi"};
        return locales;
    }

    private Locale getLocale(String selectedUILang) {
        return new Locale(selectedUILang);
    }

    /**
     * Changes locale of UI elements.
     *
     * @param locale
     */
    void changeUILanguage(final Locale locale) {
        if (locale.equals(Locale.getDefault())) {
            return; // no change in locale
        }
        Locale.setDefault(locale);
        Platform.runLater(() -> {
            FormLocalizer localizer = new FormLocalizer((Stage) menuBar.getScene().getWindow(), GuiController.class);
            localizer.ApplyCulture(bundle);
        });
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == miDownloadLangData) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DownloadDialog.fxml"));
                Parent root = fxmlLoader.load();
                DownloadDialogController downloadController = fxmlLoader.getController();
                downloadController.setLookupISO639(GuiWithOCR.getInstance().lookupISO639);
                downloadController.setLookupISO_3_1_Codes(GuiWithOCR.getInstance().lookupISO_3_1_Codes);
                downloadController.setInstalledLanguages(GuiWithOCR.getInstance().installedLanguages);
                downloadController.setTessdataDir(new File(GuiWithOCR.getInstance().tesseractParameters.getDatapath()));
                Stage downloadDialog = new Stage();
                downloadDialog.setOnShowing((WindowEvent e) -> {
                    downloadController.loadListView();
                });
                downloadDialog.setResizable(false);
//                downloadDialog.initStyle(StageStyle.UTILITY);
                downloadDialog.initModality(Modality.WINDOW_MODAL);
                downloadDialog.initOwner(menuBar.getScene().getWindow());
                downloadDialog.setAlwaysOnTop(true);
                Scene scene1 = new Scene(root);
                downloadDialog.setScene(scene1);
                downloadDialog.setTitle("Download Language Pack");
                downloadDialog.toFront();
                downloadDialog.show();
            } catch (Exception e) {

            }
        } else if (event.getSource() == miOptions) {
            try {
                optionsController = new OptionsDialogController(menuBar.getScene().getWindow());
                optionsController.setWatchFolder(watchFolder);
                optionsController.setOutputFolder(outputFolder);
                optionsController.setWatchEnabled(watchEnabled);
                optionsController.setProcessingOptions(options);
                optionsController.setDangAmbigsPath(dangAmbigsPath);
                optionsController.setDangAmbigsEnabled(dangAmbigsOn);
                optionsController.setCurLangCode("*");
                optionsController.setProcessingOptions(options);
                optionsController.setSelectedOutputFormats(outputFormats);

                Optional<ProcessingOptions> result = optionsController.showAndWait();
                if (result.isPresent()) {
                    options = result.get();
                    watchFolder = optionsController.getWatchFolder();
                    outputFolder = optionsController.getOutputFolder();
                    watchEnabled = optionsController.isWatchEnabled();
                    options = optionsController.getProcessingOptions();
                    dangAmbigsPath = optionsController.getDangAmbigsPath();
                    dangAmbigsOn = optionsController.isDangAmbigsEnabled();
                    outputFormats = optionsController.getSelectedOutputFormats();
                    updateWatch(watchFolder, watchEnabled);
                }
            } catch (Exception e) {

            }
        }
    }

    void updateWatch(String watchFolder, boolean watchEnabled) {
        watcher.setPath(new File(watchFolder));
        watcher.setEnabled(watchEnabled);
        if (watchEnabled) {
            timeline.play();
        } else {
            timeline.stop();
        }
    }

    /**
     * Gets accessible name of PageSegMode enum.
     *
     * @param val
     * @return
     */
    final String enumOf(String val) {
        return PageSegMode.enumOf(val).name().replace("PSM_", "").replace("_", " ");
    }

    void savePrefs() {
        prefs.put(strPSM, selectedPSM);
        prefs.put(strUILanguage, selectedUILang);

        if (optionsController != null) {
            prefs.put(strBatchOutputFormat, optionsController.getSelectedOutputFormats());
        }

        prefs.put(strDangAmbigsPath, dangAmbigsPath);
        prefs.putBoolean(strDangAmbigs, dangAmbigsOn);
        prefs.putBoolean(strReplaceHyphensEnabled, options.isReplaceHyphens());
        prefs.putBoolean(strRemoveHyphensEnabled, options.isRemoveHyphens());
        prefs.put(strWatchFolder, watchFolder);
        prefs.put(strOutputFolder, outputFolder);
        prefs.putBoolean(strWatchEnabled, watchEnabled);
        prefs.putBoolean(strDeskewEnabled, options.isDeskew());
        prefs.putBoolean(strPostProcessingEnabled, options.isPostProcessing());
        prefs.putBoolean(strCorrectLetterCasesEnabled, options.isCorrectLetterCases());
        prefs.putBoolean(strRemoveLinesEnabled, options.isRemoveLines());
        prefs.putBoolean(strRemoveLineBreaksEnabled, options.isRemoveLineBreaks());
    }
}
