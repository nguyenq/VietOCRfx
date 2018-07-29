/**
 * Copyright @ 2016 Quan Nguyen
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.IIOImage;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.vietocr.util.Utils;

public class GuiController implements Initializable {

    @FXML
    private MenuBar menuBar;
    @FXML
    private MainMenuController menuBarController;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnSave;
    @FXML
    protected Button btnScan;
    @FXML
    private Button btnCollapseExpand;
    @FXML
    private SplitPane splitPaneImage;
    @FXML
    protected ImageView imageView;
    @FXML
    protected Canvas canvasImage;
    @FXML
    protected ScrollPane scrollPaneImage;
    @FXML
    private Region rgn1;
    @FXML
    private Region rgn2;
    @FXML
    private Region rgn3;
    @FXML
    private Region rgn4;
    @FXML
    protected TextArea textarea;
    @FXML
    protected Label labelStatus;
    @FXML
    protected Label labelScreenShotMode;
    @FXML
    protected Label labelPSMValue;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    private HBox segmentedRegionsBox;

    static GuiController instance;

    private static final String IMAGE_PATTERN = "([^\\s]+(\\.(?i)(png|tif|tiff|jpg|jpeg|bmp|gif|pdf|pbm|pgm|ppm|pnm|jp2|j2k|jpf|jpx|jpm))$)";

    private String currentDirectory, outputDirectory;
    protected short imageIndex;
    protected short imageTotal;
    private int filterIndex;
    ObservableList<FileChooser.ExtensionFilter> fileFilters;
    static final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr");
    protected ResourceBundle bundle;
    Font font;
    protected List<BufferedImage> imageList;
    protected List<IIOImage> iioImageList;
    protected String inputfilename;
    protected OCRImageEntity entity;
    protected float scaleX = 1f;
    protected float scaleY = 1f;
    private boolean textChanged;
    private File textFile;

    private final static Logger logger = Logger.getLogger(GuiController.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        currentDirectory = prefs.get("currentDirectory", System.getProperty("user.home"));
        outputDirectory = prefs.get("outputDirectory", System.getProperty("user.home"));
        filterIndex = prefs.getInt("filterIndex", 0);

        String style = prefs.get("fontStyle", "");
        font = Font.font(
                prefs.get("fontName", Font.getDefault().getFamily()),
                style.contains("Bold") ? FontWeight.BOLD : FontWeight.NORMAL,
                style.contains("Italic") ? FontPosture.ITALIC : FontPosture.REGULAR,
                prefs.getDouble("fontSize", 12));
        textarea.setFont(font);
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N
        HBox.setHgrow(rgn1, Priority.ALWAYS);
        HBox.setHgrow(rgn3, Priority.ALWAYS);
        HBox.setHgrow(rgn4, Priority.ALWAYS);
        Platform.runLater(() -> {
            splitPaneImage.setDividerPositions(0);
        });

        this.textarea.lengthProperty().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable o) {
                textChanged = true;
            }
        });

        scrollPaneImage.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    File file = db.getFiles().get(0);
                    boolean isAccepted = file.getName().matches(IMAGE_PATTERN);
                    if (isAccepted) {
                        event.acceptTransferModes(TransferMode.COPY);
                    } else {
                        event.consume();
                    }
                } else {
                    event.consume();
                }
            }
        });

        scrollPaneImage.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    openFile(db.getFiles().get(0));
                }

                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    /**
     * Gets GuiController instance (for child controllers).
     *
     * @return
     */
    public static GuiController getInstance() {
        return instance;
    }

    @FXML
    protected void handleAction(ActionEvent event) {
        if (event.getSource() == btnOpen) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open Image File");
            fc.setInitialDirectory(new File(currentDirectory));
            FileChooser.ExtensionFilter allImageFilter = new FileChooser.ExtensionFilter(bundle.getString("All_Image_Files"), "*.bmp", "*.jpg", "*.jpeg", "*.png", "*.tif", "*.tiff");
            FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG", "*.png");
            FileChooser.ExtensionFilter tiffFilter = new FileChooser.ExtensionFilter("TIFF", "*.tif", "*.tiff");

            fileFilters = fc.getExtensionFilters();
            fileFilters.addAll(allImageFilter, pngFilter, tiffFilter);
            if (filterIndex < fileFilters.size()) {
                fc.setSelectedExtensionFilter(fileFilters.get(filterIndex));
            }

            File file = fc.showOpenDialog(btnOpen.getScene().getWindow());
            if (file != null) {
                currentDirectory = file.getParent();
                filterIndex = fileFilters.indexOf(fc.getSelectedExtensionFilter());
                openFile(file);
            }
        } else if (event.getSource() == btnSave) {
            saveAction();
            textChanged = false;
        } else if (event.getSource() == btnCollapseExpand) {
            this.btnCollapseExpand.setText(this.btnCollapseExpand.getText().equals("»") ? "«" : "»");
            boolean collapsed = this.btnCollapseExpand.getText().equals("»");
            this.splitPaneImage.setDividerPositions(collapsed ? 0 : 0.25);
//            this.splitPaneImage.setDividerSize(collapsed ? 0 : 5);
//        } else if (event.getSource() == btnOCR) {
//
//        } else if (event.getSource() == btnCancel) {
//
//        } else if (event.getSource() == btnClear) {
//
//        } else if (event.getSource() == btnRemoveLineBreaks) {
//
//        } else if (event.getSource() == btnSpellCheck) {
//
//        } else if (event.getSource() == btnPostProcess) {
//
//        } else if (event.getSource() == cbOCRLanguage) {
        }
    }

    public void openFile(final File selectedFile) {
        if (!selectedFile.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, bundle.getString("File_not_exist"));
            alert.show();
            return;
        }
        if (!promptToSave()) {
            return;
        }

        this.menuBarController.menuFileController.menuRecentFilesController.updateMRUList(selectedFile.getPath());

        Task loadWorker = new Task<Void>() {

            @Override
            public Void call() throws Exception {
                readImageFile(selectedFile);
                return null;
            }
        };

        new Thread(loadWorker).start();
    }

    void readImageFile(File selectedFile) {
        try {
            inputfilename = selectedFile.getPath();
            iioImageList = ImageIOHelper.getIIOImageList(selectedFile);
            imageList = Utils.getImageList(iioImageList);
            if (imageList == null) {
                new Alert(Alert.AlertType.ERROR, bundle.getString("Cannotloadimage")).show();
                return;
            }

            imageIndex = 0;
            ArrayList<BufferedImage> al = new ArrayList<BufferedImage>();
            al.addAll(imageList);
            entity = new OCRImageEntity(al, selectedFile.getName(), imageIndex, null, "eng");
            menuBar.setUserData(entity);

            Platform.runLater(() -> {
                loadImage();
                this.scrollPaneImage.setVvalue(0); // scroll to top
                this.scrollPaneImage.setHvalue(0); // scroll to left
                ((Stage) imageView.getScene().getWindow()).setTitle(VietOCR.APP_NAME + " - " + selectedFile.getName());
            });
        } catch (OutOfMemoryError oome) {
            new Alert(Alert.AlertType.ERROR, "Out-Of-Memory Exception").show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            if (e.getMessage() != null) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        }
    }

    void loadImage() {
        imageView.setImage(SwingFXUtils.toFXImage(imageList.get(imageIndex), null));
//        labelPageNbr.setText(String.format("Page: %d of %d", imageIndex + 1, imageList.size()));
    }

    /**
     * Displays a dialog to save changes.
     *
     * @return false if user canceled, true else
     */
    protected boolean promptToSave() {
        if (!textChanged) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, VietOCR.APP_NAME, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle(VietOCR.APP_NAME);
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("Do_you_want_to_save_the_changes_to_") + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES) {
            return saveAction();
        } else if (result.get() == ButtonType.NO) {
            return true;
        } else {
            return false;
        }
    }

    boolean saveAction() {
        if (textFile == null || !textFile.exists()) {
            return saveFileDlg();
        } else {
            return saveTextFile(textFile);
        }
    }

    boolean saveFileDlg() {
        FileChooser fc = new FileChooser();
        fc.setTitle(bundle.getString("Save_As"));
        fc.setInitialDirectory(new File(outputDirectory));
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fc.getExtensionFilters().addAll(textFilter);

        if (textFile != null) {
            fc.setInitialDirectory(textFile.getParentFile());
            fc.setInitialFileName(textFile.getName());
        }

        File f = fc.showSaveDialog(btnSave.getScene().getWindow());
        if (f != null) {
            outputDirectory = f.getParent();
            textFile = f;
            return saveTextFile(textFile);
        } else {
            return false;
        }
    }

    boolean saveTextFile(File file) {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.write(textarea.getText());
//            textChangedProp.set(false);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Quits the application.
     */
    void quit() {
        if (!promptToSave()) {
            return;
        }

        this.menuBarController.savePrefs();

//            prefs.put("currentDirectory", chooser.getCurrentDirectory().getPath());
//            if (getInputContext().getLocale() != null) {
//                prefs.put("inputMethodLocale", getInputContext().getLocale().toString());
//            }
        Font font = textarea.getFont();
        prefs.put("fontName", font.getName());
        prefs.putDouble("fontSize", font.getSize());
        prefs.put("fontStyle", font.getStyle());

        Stage stage = (Stage) menuBar.getScene().getWindow();
        prefs.putBoolean("windowState", stage.isMaximized());

        if (!stage.isMaximized()) {
            prefs.putDouble("frameHeight", stage.getHeight());
            prefs.putDouble("frameWidth", stage.getWidth());
            prefs.putDouble("frameX", stage.getX());
            prefs.putDouble("frameY", stage.getY());
        }

        stage.close();
        Platform.exit();
    }

    void setStageState(Stage stage) {
        stage.setOnShowing(we -> {
            boolean maximized = prefs.getBoolean("windowState", false);
            stage.setMaximized(maximized);
            if (!maximized) {
                stage.setX(prefs.getDouble("frameX", 0));
                stage.setY(prefs.getDouble("frameY", 0));
                stage.setWidth(prefs.getDouble("frameWidth", 800));
                stage.setHeight(prefs.getDouble("frameHeight", 600));
            }
            //                updateSave(false);
            //                populateMRUList();
        });

        stage.setOnCloseRequest(we -> {
            quit();
        });
    }
}
