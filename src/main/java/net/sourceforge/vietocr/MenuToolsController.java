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

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.tess4j.util.PdfUtilities;
import net.sourceforge.vietocr.util.Utils;

public class MenuToolsController implements Initializable {

    @FXML
    private Menu menuTools;
    @FXML
    private MenuItem miMergeTIFF;
    @FXML
    private MenuItem miSplitTIFF;
    @FXML
    private MenuItem miMergePDF;
    @FXML
    private MenuItem miSplitPDF;
    @FXML
    private MenuItem miConvertPDF;
    private MenuBar menuBar;

    protected ResourceBundle bundle;
    static final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr");
    private final String strImageFolder = "ImageFolder";
    private final String strPdfFolder = "PdfFolder";
    File imageFolder;
    File pdfFolder;
    ExtensionFilter selectedFilter;

    private final static Logger logger = Logger.getLogger(MenuToolsController.class.getName());

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net.sourceforge.vietocr.Gui"); // NOI18N
        imageFolder = new File(prefs.get(strImageFolder, System.getProperty("user.home")));
        pdfFolder = new File(prefs.get(strPdfFolder, System.getProperty("user.home")));
    }

    void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    @FXML
    private void handleAction(ActionEvent event) {
        Label labelStatus = (Label) menuBar.getScene().lookup("#labelStatus");
        ProgressBar progressBar = (ProgressBar) menuBar.getScene().lookup("#progressBar");

        FileChooser.ExtensionFilter tiffFilter = new FileChooser.ExtensionFilter("TIFF", "*.tif", "*.tiff");
        if (event.getSource() == miMergeTIFF) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("Select_Input_Images"));
            fc.setInitialDirectory(imageFolder);

            FileChooser.ExtensionFilter jpegFilter = new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg");
            FileChooser.ExtensionFilter gifFilter = new FileChooser.ExtensionFilter("GIF", "*.gif");
            FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG", "*.png");
            FileChooser.ExtensionFilter bmpFilter = new FileChooser.ExtensionFilter("Bitmap", "*.bmp");
            FileChooser.ExtensionFilter allImageFilter = new FileChooser.ExtensionFilter(bundle.getString("All_Image_Files"), "*.tif", "*.tiff", "*.jpg", "*.jpeg", "*.gif", "*.png", "*.bmp");
            fc.getExtensionFilters().addAll(allImageFilter, tiffFilter, jpegFilter, gifFilter, pngFilter, bmpFilter);

            final List<File> inputs = fc.showOpenMultipleDialog(menuBar.getScene().getWindow());
            if (inputs != null) {
                selectedFilter = fc.getSelectedExtensionFilter();
                imageFolder = inputs.get(0).getParentFile();

                fc.setTitle(bundle.getString("Save_Multi-page_TIFF_Image"));
                fc.setInitialDirectory(imageFolder);
                fc.getExtensionFilters().clear();
                fc.getExtensionFilters().add(tiffFilter);

                File outputTiff = fc.showSaveDialog(menuBar.getScene().getWindow());
                if (outputTiff != null) {
                    if (outputTiff.exists()) {
                        outputTiff.delete();
                    }

                    progressBar.setVisible(true);
                    labelStatus.setVisible(true);
                    labelStatus.getScene().setCursor(Cursor.WAIT);

                    Task<Void> worker = new Task<Void>() {

                        @Override
                        protected Void call() throws Exception {
                            updateMessage(bundle.getString("MergeTIFF_running..."));
                            ImageIOHelper.mergeTiff(inputs.toArray(new File[0]), outputTiff);
                            return null;
                        }

                        @Override
                        protected void succeeded() {
                            super.succeeded();
                            updateMessage(bundle.getString("MergeTIFFcompleted"));
                            updateProgress(1, 1);
                            new Alert(Alert.AlertType.NONE, bundle.getString("MergeTIFFcompleted") + outputTiff.getName() + bundle.getString("created"), ButtonType.OK).showAndWait();
                            progressBar.setVisible(false);
                            labelStatus.setVisible(false);
                            labelStatus.getScene().setCursor(Cursor.DEFAULT);
                        }

                        @Override
                        protected void failed() {
                            super.failed();
                            Throwable ex = getException();
                            logger.log(Level.SEVERE, ex.getMessage(), ex);
                            new Alert(Alert.AlertType.NONE, ex.getMessage(), ButtonType.OK).showAndWait();
                            progressBar.setVisible(false);
                            labelStatus.setVisible(false);
                            labelStatus.getScene().setCursor(Cursor.DEFAULT);
                        }
                    };

                    labelStatus.textProperty().bind(worker.messageProperty());
                    progressBar.progressProperty().unbind();
                    progressBar.progressProperty().bind(worker.progressProperty());

                    new Thread(worker).start();
                }
            }
        } else if (event.getSource() == miSplitTIFF) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("Select_Input_TIFF"));
            fc.setInitialDirectory(imageFolder);
            fc.getExtensionFilters().add(tiffFilter);

            if (selectedFilter != null) {
                fc.setSelectedExtensionFilter(selectedFilter);
            }

            final File file = fc.showOpenDialog(menuBar.getScene().getWindow());
            if (file != null) {
                selectedFilter = fc.getSelectedExtensionFilter();
                imageFolder = file.getParentFile();

                progressBar.setVisible(true);
                labelStatus.setVisible(true);
                labelStatus.getScene().setCursor(Cursor.WAIT);

                Task<Void> worker = new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        updateMessage(bundle.getString("SplitTIFF_running..."));
                        String basefilename = Utils.stripExtension(file.getPath());
                        List<File> files = ImageIOHelper.createTiffFiles(file, -1, true);

                        // move temp TIFF files to selected folder
                        for (int i = 0; i < files.size(); i++) {
                            String outfilename = String.format("%s-%03d.tif", basefilename, i + 1);
                            File outfile = new File(outfilename);
                            outfile.delete();
                            files.get(i).renameTo(outfile);
                        }
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        updateMessage(bundle.getString("SplitTIFFcompleted"));
                        updateProgress(1, 1);
                        new Alert(Alert.AlertType.NONE, bundle.getString("SplitTIFFcompleted"), ButtonType.OK).showAndWait();
                        progressBar.setVisible(false);
                        labelStatus.setVisible(false);
                        labelStatus.getScene().setCursor(Cursor.DEFAULT);
                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        Throwable ex = getException();
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                        new Alert(Alert.AlertType.NONE, ex.getMessage(), ButtonType.OK).showAndWait();
                        progressBar.setVisible(false);
                        labelStatus.setVisible(false);
                        labelStatus.getScene().setCursor(Cursor.DEFAULT);
                    }
                };

                labelStatus.textProperty().bind(worker.messageProperty());

                new Thread(worker).start();
            }
        } else if (event.getSource() == miMergePDF) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("Select_Input_PDFs"));
            fc.setInitialDirectory(imageFolder);
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
            fc.getExtensionFilters().add(pdfFilter);
            List<File> inputPdfs = fc.showOpenMultipleDialog(menuBar.getScene().getWindow());

            if (inputPdfs != null) {
                imageFolder = inputPdfs.get(0).getParentFile();
                fc.setTitle(bundle.getString("Save_Merged_PDF"));
                fc.setInitialDirectory(imageFolder);
                File outputPdf = fc.showSaveDialog(menuBar.getScene().getWindow());
                if (outputPdf != null) {
                    progressBar.setVisible(true);
                    labelStatus.setVisible(true);
                    labelStatus.getScene().setCursor(Cursor.WAIT);

                    Task<String> worker = new Task<String>() {

                        @Override
                        protected String call() throws Exception {
                            updateMessage(bundle.getString("MergePDF_running..."));
                            PdfUtilities.mergePdf(inputPdfs.toArray(new File[0]), outputPdf);
                            return outputPdf.getName();
                        }

                        @Override
                        protected void succeeded() {
                            super.succeeded();
                            updateMessage(bundle.getString("MergePDFcompleted"));
                            updateProgress(1, 1);
                            String msg = bundle.getString("MergePDFcompleted") + getValue() + bundle.getString("created");
                            new Alert(Alert.AlertType.NONE, msg, ButtonType.OK).showAndWait();
                            progressBar.setVisible(false);
                            labelStatus.setVisible(false);
                            labelStatus.getScene().setCursor(Cursor.DEFAULT);
                        }

                        @Override
                        protected void failed() {
                            super.failed();
                            updateMessage(null);
                            Throwable ex = getException();
                            logger.log(Level.SEVERE, ex.getMessage(), ex);
                            new Alert(Alert.AlertType.NONE, ex.getMessage(), ButtonType.OK).showAndWait();
                            progressBar.setVisible(false);
                            labelStatus.setVisible(false);
                            labelStatus.getScene().setCursor(Cursor.DEFAULT);
                        }
                    };

                    labelStatus.textProperty().bind(worker.messageProperty());
                    progressBar.progressProperty().unbind();
                    progressBar.progressProperty().bind(worker.progressProperty());

                    new Thread(worker).start();
                }
            }
        } else if (event.getSource() == miSplitPDF) {
            try {
                SplitPdfDialogController controller = new SplitPdfDialogController(menuBar.getScene().getWindow());
                controller.setCurrentDirectory(pdfFolder);

                Optional<SplitPdfArgs> result = controller.showAndWait();
                if (result.isPresent()) {
                    final SplitPdfArgs args = result.get();
                    pdfFolder = controller.getCurrentDirectory();

                    labelStatus.setText(bundle.getString("SplitPDF_running..."));
                    progressBar.setVisible(true);
                    labelStatus.setVisible(true);
                    labelStatus.getScene().setCursor(Cursor.WAIT);

                    Task<String> worker = new Task<String>() {
                        String outputFilename;

                        @Override
                        protected String call() throws Exception {
                            File inputFile = new File(args.getInputFilename());
                            outputFilename = args.getOutputFilename();
                            File outputFile = new File(outputFilename);

                            if (args.isPages()) {
                                PdfUtilities.splitPdf(inputFile, outputFile, Integer.parseInt(args.getFromPage()), Integer.parseInt(args.getToPage()));
                            } else {
                                if (outputFilename.endsWith(".pdf")) {
                                    outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf(".pdf"));
                                }

                                int pageCount = PdfUtilities.getPdfPageCount(inputFile);
                                if (pageCount == 0) {
                                    throw new RuntimeException("Split PDF failed.");
                                }

                                int pageRange = Integer.parseInt(args.getNumOfPages());
                                int startPage = 1;

                                while (startPage <= pageCount) {
                                    int endPage = startPage + pageRange - 1;
                                    outputFile = new File(outputFilename + startPage + ".pdf");
                                    PdfUtilities.splitPdf(inputFile, outputFile, startPage, endPage);
                                    startPage = endPage + 1;
                                }
                            }

                            return outputFilename;
                        }

                        @Override
                        protected void succeeded() {
                            super.succeeded();
                            updateMessage(bundle.getString("SplitPDF_completed."));
                            updateProgress(1, 1);
                            String msg = bundle.getString("SplitPDF_completed.") + bundle.getString("check_output_in") + new File(outputFilename).getParent();
                            new Alert(Alert.AlertType.NONE, msg, ButtonType.OK).showAndWait();
                            progressBar.setVisible(false);
                            labelStatus.setVisible(false);
                            labelStatus.getScene().setCursor(Cursor.DEFAULT);
                        }

                        @Override
                        protected void failed() {
                            super.failed();
                            updateMessage(null);
                            Throwable ex = getException();
                            logger.log(Level.SEVERE, ex.getMessage(), ex);
                            new Alert(Alert.AlertType.NONE, ex.getMessage(), ButtonType.OK).showAndWait();
                            progressBar.setVisible(false);
                            labelStatus.setVisible(false);
                            labelStatus.getScene().setCursor(Cursor.DEFAULT);
                        }
                    };

                    new Thread(worker).start();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else if (event.getSource() == miConvertPDF) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("Select_Input_PDF"));
            fc.setInitialDirectory(imageFolder);
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
            fc.getExtensionFilters().add(pdfFilter);
            final List<File> inputPdfs = fc.showOpenMultipleDialog(menuBar.getScene().getWindow());

            if (inputPdfs != null) {
                imageFolder = inputPdfs.get(0).getParentFile();
                progressBar.setVisible(true);
                labelStatus.setVisible(true);
                labelStatus.getScene().setCursor(Cursor.WAIT);

                Task<File> worker = new Task<File>() {

                    @Override
                    protected File call() throws Exception {
                        updateMessage(bundle.getString("ConvertPDF_running..."));

                        for (File inputFile : inputPdfs) {
                            File outputTiffFile = PdfUtilities.convertPdf2Tiff(inputFile);
                            String targetFile = Utils.stripExtension(inputFile.getPath()) + ".tif";
                            Files.move(outputTiffFile.toPath(), new File(targetFile).toPath(), REPLACE_EXISTING);
                        }

                        return imageFolder;
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        updateMessage(bundle.getString("ConvertPDF_completed"));
                        updateProgress(1, 1);
                        String msg = bundle.getString("ConvertPDF_completed") + bundle.getString("check_output_in") + "\n" + getValue();
                        new Alert(Alert.AlertType.NONE, msg, ButtonType.OK).showAndWait();
                        progressBar.setVisible(false);
                        labelStatus.setVisible(false);
                        labelStatus.getScene().setCursor(Cursor.DEFAULT);
                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        updateMessage(null);
                        Throwable ex = getException();
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                        new Alert(Alert.AlertType.NONE, ex.getMessage(), ButtonType.OK).showAndWait();
                        progressBar.setVisible(false);
                        labelStatus.setVisible(false);
                        labelStatus.getScene().setCursor(Cursor.DEFAULT);
                    }
                };

                labelStatus.textProperty().bind(worker.messageProperty());
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(worker.progressProperty());

                new Thread(worker).start();
            }
        }
    }

    /**
     * Remembers settings.
     */
    protected void savePrefs() {
        prefs.put(strImageFolder, imageFolder.getPath());
        prefs.put(strPdfFolder, pdfFolder.getPath());
    }
}
