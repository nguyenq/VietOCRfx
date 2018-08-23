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

import java.awt.Image;
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
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.vietocr.util.SelectionBox;
import net.sourceforge.vietocr.util.Utils;
import net.sourceforge.vietpad.inputmethod.VietKeyListener;

public class GuiController implements Initializable {

    @FXML
    protected MenuBar menuBar;
    @FXML
    private MainMenuController menuBarController;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnSave;
    @FXML
    protected Button btnScan;
    @FXML
    protected Button btnPaste;
    @FXML
    private Button btnCollapseExpand;
    @FXML
    private SplitPane splitPaneImage;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected Group imagePane;
    @FXML
    protected ImageView imageView;
    @FXML
    protected Canvas canvasImage;
    @FXML
    protected ScrollPane scrollPaneImage;
    @FXML
    protected VBox thumbnailBox;
    @FXML
    private Region rgn1;
    @FXML
    private Region rgn2;
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
    private Label labelDimensionValue;
    @FXML
    private Label labelPageNbr;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected HBox statusBar;
    @FXML
    protected ChoiceBox cbPageNum;

    static GuiController instance;

    private static final String IMAGE_PATTERN = "([^\\s]+(\\.(?i)(png|tif|tiff|jpg|jpeg|bmp|gif|pdf|pbm|pgm|ppm|pnm|jp2|j2k|jpf|jpx|jpm))$)";

    private String currentDirectory, outputDirectory;
    protected int imageIndex;
    protected int imageTotal;
    private int filterIndex;
    ObservableList<FileChooser.ExtensionFilter> fileFilters;
    static final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr");
    protected ResourceBundle bundle;
    protected List<BufferedImage> imageList;
    protected List<IIOImage> iioImageList;
    protected String inputfilename;
    protected OCRImageEntity entity;
    protected float scaleX = 1f;
    protected float scaleY = 1f;
    private boolean textChanged;
    private File textFile;

    private Node thumbnailPane;
    double prevDividerPosition;
    Glow glow;
    SelectionBox selectionBox;

    private final static Logger logger = Logger.getLogger(GuiController.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        currentDirectory = prefs.get("currentDirectory", System.getProperty("user.home"));
        outputDirectory = prefs.get("outputDirectory", System.getProperty("user.home"));
        filterIndex = prefs.getInt("filterIndex", 0);

        String style = prefs.get("fontStyle", "");
        Font font = Font.font(
                prefs.get("fontName", Font.getDefault().getFamily()),
                style.contains("Bold") ? FontWeight.BOLD : FontWeight.NORMAL,
                style.contains("Italic") ? FontPosture.ITALIC : FontPosture.REGULAR,
                prefs.getDouble("fontSize", 12));
        textarea.setFont(font);
        new VietKeyListener(textarea);
        selectionBox = new SelectionBox(imagePane);
        
        btnSave.disableProperty().bind(textarea.textProperty().length().isEqualTo(0));

        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N
        HBox.setHgrow(rgn1, Priority.ALWAYS);
        HBox.setHgrow(rgn4, Priority.ALWAYS);
        Platform.runLater(() -> {
            thumbnailPane = this.splitPaneImage.getItems().remove(0);
            splitPaneImage.setDividerPositions(0);
            prevDividerPosition = ((Control) thumbnailPane).getWidth() / this.splitPaneImage.getWidth();
        });

        this.textarea.lengthProperty().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable o) {
                textChanged = true;
            }
        });

        splitPane.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    File file = db.getFiles().get(0);
                    boolean isAccepted = file.getName().matches(IMAGE_PATTERN) || file.getName().endsWith(".txt");
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

        splitPane.setOnDragDropped(new EventHandler<DragEvent>() {
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

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GREEN);
        glow = new Glow();
        glow.setInput(shadow);

        cbPageNum.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number value, Number new_value) {
                imageIndex = new_value.intValue();
                if (imageIndex >= 0) {
                    loadImage();

                    List<Node> thumbnails = thumbnailBox.getChildren().stream()
                            .filter(ImageView.class::isInstance)
                            .collect(Collectors.toList());

                    thumbnails.forEach(node -> {
                        node.setEffect(null);
                    });

                    if (thumbnails.size() > 0) {
                        thumbnails.get(imageIndex).setEffect(glow);
                    }
                }
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
            File curDir = new File(currentDirectory);
            fc.setInitialDirectory(curDir.exists() ? curDir : new File(System.getProperty("user.home")));
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
            if (collapsed) {
                prevDividerPosition = this.splitPaneImage.getDividerPositions()[0];
                thumbnailPane = this.splitPaneImage.getItems().remove(0);
                this.splitPaneImage.setDividerPositions(0);
            } else {
                this.splitPaneImage.setDividerPositions(prevDividerPosition);
                this.splitPaneImage.getItems().add(0, thumbnailPane);
            }
        } else if (event.getSource() == btnPaste) {
            pasteImage();
        }
    }

    public void openFile(final File selectedFile) {
        if (!selectedFile.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, bundle.getString("File_not_exist"));
            alert.show();
            return;
        }
        // if text file, load it into textarea
        if (selectedFile.getName().endsWith(".txt")) {
            if (!promptToSave()) {
                return;
            }
            try {
                this.textarea.setText(Utils.readTextFile(selectedFile));
                this.textarea.requestFocus();
                textFile = selectedFile;
            } catch (Exception e) {
                // ignore
            }
            return;
        }

        this.menuBarController.menuFileController.menuRecentFilesController.updateMRUList(selectedFile.getPath());

        progressBar.setVisible(true);
        splitPane.setCursor(Cursor.WAIT);
        statusBar.setCursor(Cursor.WAIT);
        thumbnailBox.getChildren().clear();

        Task loadWorker = new Task<Void>() {

            @Override
            public Void call() throws Exception {
                updateMessage(bundle.getString("Loading_image..."));
                readImageFile(selectedFile);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage(bundle.getString("Loading_completed"));
                updateProgress(1, 1);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.progressProperty().unbind();
//                        progressBar.setDisable(true);
//                        progressBar.setVisible(false);
                        splitPane.setCursor(Cursor.DEFAULT);
                        statusBar.setCursor(Cursor.DEFAULT);
                    }
                });
            }
        };

        progressBar.progressProperty().bind(loadWorker.progressProperty());
        labelStatus.textProperty().bind(loadWorker.messageProperty());
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
            imageTotal = imageList.size();

            ObservableList pageNumbers = FXCollections.observableArrayList();
            for (int i = 0; i < imageTotal; i++) {
                pageNumbers.add(String.valueOf(i + 1));
            }

            ArrayList<BufferedImage> al = new ArrayList<BufferedImage>();
            al.addAll(imageList);
            entity = new OCRImageEntity(al, selectedFile.getName(), imageIndex, null, "eng");
            menuBar.setUserData(entity);
            imageView.setScaleX(1);
            imageView.setScaleY(1);
            imageView.setRotate(0);
            loadThumbnails();

            Platform.runLater(() -> {
                labelPageNbr.setText("/ " + imageTotal);
                cbPageNum.setItems(pageNumbers);
                cbPageNum.getSelectionModel().selectFirst();
                this.scrollPaneImage.setVvalue(0); // scroll to top
                this.scrollPaneImage.setHvalue(0); // scroll to left
                setButtons();
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

    /**
     * Loads image into view.
     */
    void loadImage() {
        BufferedImage bi = imageList.get(imageIndex);
        imageView.setImage(SwingFXUtils.toFXImage(bi, null));
        selectionBox.deselect();
        setSegmentedRegions();
        labelDimensionValue.setText(String.format("%s × %spx  %sbpp", bi.getWidth(), bi.getHeight(), bi.getColorModel().getPixelSize()));
    }

    void setButtons() {
        // to be implemented in subclass
    }
    
    void setSegmentedRegions() {
        // to be implemented in subclass
    }

    /**
     * Loads thumbnails.
     */
    void loadThumbnails() {
        // to be implemented in subclass
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
        alert.setContentText(bundle.getString("Do_you_want_to_save_the_changes_to_")
                + (textFile == null ? bundle.getString("Untitled") : textFile.getName()) + "?");

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
     * Pastes image from clipboard.
     */
    void pasteImage() {
        try {
            Image image = ImageHelper.getClipboardImage();
            if (image != null) {
                File tempFile = File.createTempFile("tmp", ".png");
                ImageIO.write((BufferedImage) image, "png", tempFile);
                openFile(tempFile);
                tempFile.deleteOnExit();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    /**
     * Quits the application.
     */
    boolean quit() {
        if (!promptToSave()) {
            return false;
        }

        savePrefs();

        return true;
    }

    public void savePrefs() {
        this.menuBarController.savePrefs();

        prefs.put("currentDirectory", currentDirectory);
//        if (getInputContext().getLocale() != null) {
//            prefs.put("inputMethodLocale", getInputContext().getLocale().toString());
//        }
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
            if (!quit()) {
                we.consume();
            } else {
                stage.close();
                Platform.exit();
                System.exit(0);
            }
        });
    }
}
