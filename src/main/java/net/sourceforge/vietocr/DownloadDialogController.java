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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.sourceforge.vietocr.util.FileExtractor;
import net.sourceforge.vietocr.util.Utils;

public class DownloadDialogController implements Initializable {
    
    @FXML
    private Button btnDownload;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnClose;
    @FXML
    private ListView listViewLang;
    @FXML
    private Label labelStatus;
    @FXML
    private ProgressBar progressBar;
    
    final static int BUFFER_SIZE = 1024;
    final static String DICTIONARY_FOLDER = "dict";
    final static String TESSDATA_FOLDER = "tessdata";
    final String tmpdir = System.getProperty("java.io.tmpdir");
    private Properties availableLanguageCodes;
    private Properties availableDictionaries;
    private Properties lookupISO_3_1_Codes;
    private Properties lookupISO639;
    private String[] installedLanguages;
    File baseDir;
    List<SwingWorker<File, Integer>> downloadTracker;
    long contentLength, byteCount;
    int numberOfDownloads, numOfConcurrentTasks;
    ResourceBundle bundle;
    private File tessdataDir;
    
    private final static Logger logger = Logger.getLogger(DownloadDialogController.class.getName());

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = ResourceBundle.getBundle("net/sourceforge/vietocr/DownloadDialog");
        
        baseDir = Utils.getBaseDir(DownloadDialogController.this);
        downloadTracker = new ArrayList<SwingWorker<File, Integer>>();
        availableLanguageCodes = new Properties();
        availableDictionaries = new Properties();
        
        try {
            File xmlFile = new File(baseDir, "data/TessLangDataURL.xml");
            availableLanguageCodes.loadFromXML(new FileInputStream(xmlFile));
            xmlFile = new File(baseDir, "data/OO-SpellDictionaries.xml");
            availableDictionaries.loadFromXML(new FileInputStream(xmlFile));
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        
        listViewLang.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        btnDownload.disableProperty().bind(listViewLang.getSelectionModel().selectedItemProperty().isNull());
    }
    
    void loadListView() {
        String[] available = availableLanguageCodes.keySet().toArray(new String[0]);
        List<String> languageNames = new ArrayList<String>();
        for (String key : available) {
            languageNames.add(lookupISO639.getProperty(key, key));
        }
        Collections.sort(languageNames, Collator.getInstance());
        listViewLang.setItems(FXCollections.observableArrayList(languageNames));
    }
    
    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == btnDownload) {
            if (this.listViewLang.getSelectionModel().getSelectedIndex() == -1) {
                return;
            }
            
            boolean isWriteAccess = CheckDirectoryWriteAccess(tessdataDir);
            
            if (!isWriteAccess) {
                String msg = String.format(bundle.getString("Access_denied"), tessdataDir.getPath());
                JOptionPane.showMessageDialog(null, msg, VietOCR.APP_NAME, JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            this.btnDownload.setDisable(true);
            this.btnCancel.setDisable(false);
            this.labelStatus.setText(bundle.getString("Downloading..."));
//            this.progressBar.setMaximum(100);
            this.progressBar.setProgress(0);
            this.progressBar.setVisible(true);
            labelStatus.getScene().setCursor(Cursor.WAIT);
            
            downloadTracker.clear();
            contentLength = byteCount = 0;
            numOfConcurrentTasks = this.listViewLang.getSelectionModel().getSelectedIndices().size();
            
            for (Object value : this.listViewLang.getSelectionModel().getSelectedItems()) {
                String key = FindKey(lookupISO639, value.toString()); // Vietnamese -> vie
                if (key != null) {
                    try {
                        URL url = new URL(availableLanguageCodes.getProperty(key));
                        downloadDataFile(url, TESSDATA_FOLDER); // download language data pack. In Tesseract 3.02, data is packaged under tesseract-ocr/tessdata folder

                        if (lookupISO_3_1_Codes.containsKey(key)) {
                            String iso_3_1_Code = lookupISO_3_1_Codes.getProperty(key); // vie -> vi_VN
                            if (availableDictionaries.containsKey(iso_3_1_Code)) {
                                url = new URL(availableDictionaries.getProperty(iso_3_1_Code));
                                ++numOfConcurrentTasks;
                                downloadDataFile(url, DICTIONARY_FOLDER); // download dictionary
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        } else if (event.getSource() == btnCancel) {
            for (SwingWorker<File, Integer> downloadWorker : downloadTracker) {
                if (downloadWorker != null && !downloadWorker.isDone()) {
                    downloadWorker.cancel(true);
                    downloadWorker = null;
                }
            }
            this.btnCancel.setDisable(true);
        } else if (event.getSource() == btnClose) {
            if (numberOfDownloads > 0) {
                //JOptionPane.showMessageDialog(DownloadDialogController.this, bundle.getString("Please_restart"), Gui.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
            ((Stage) btnClose.getScene().getWindow()).close();
        }
    }

    /**
     * Determines if a folder is writable by a user.
     *
     * @param directory
     * @return
     */
    private boolean CheckDirectoryWriteAccess(File directory) {
        boolean writeAccess = false;
        
        if (directory.exists()) {
            try {
                File tempFile = File.createTempFile("tmp", null, directory);
                if (tempFile.exists()) {
                    tempFile.delete();
                    writeAccess = true;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        
        return writeAccess;
    }
    
    String FindKey(Properties lookup, String value) {
        for (Enumeration e = lookup.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (lookup.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Downloads language data packs from Tesseract site and dictionary files
     * from OpenOffice site, and extracts them into appropriate folders.
     *
     * @param remoteFile
     * @param destFolder
     * @throws Exception
     */
    void downloadDataFile(final URL remoteFile, final String destFolder) throws Exception {
        final URLConnection connection = remoteFile.openConnection();
        connection.setReadTimeout(15000);
        connection.connect();
        contentLength += connection.getContentLength(); // filesize
        SwingWorker<File, Integer> downloadWorker = new SwingWorker<File, Integer>() {
            
            @Override
            public File doInBackground() throws Exception {
                InputStream inputStream = connection.getInputStream();
                File outputFile = new File(tmpdir, new File(remoteFile.getFile()).getName());
                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream bout = new BufferedOutputStream(fos);
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) > -1) {
                    if (isCancelled()) {
                        break;
                    }
                    bout.write(buffer, 0, bytesRead);
                    byteCount += bytesRead;
                    if (contentLength != 0) {
                        int progressPercent = (int) (100 * byteCount / contentLength);
                        if (progressPercent > 100) {
                            progressPercent = 100;
                        }
                        setProgress(progressPercent);
                    }
                }
                
                bout.close();
                inputStream.close();
                return outputFile;
            }
            
            @Override
            public void done() {
                try {
                    File downloadedFile = get();
                    
                    File destFolderPath;
                    if (destFolder.equals(DICTIONARY_FOLDER)) {
                        destFolderPath = new File(baseDir, destFolder);
                    } else {
                        destFolderPath = tessdataDir;
                    }
                    FileExtractor.extractCompressedFile(downloadedFile.getPath(), destFolderPath.getPath());
                    if (destFolder.equals(TESSDATA_FOLDER)) {
                        numberOfDownloads++;
                    }
                    
                    if (--numOfConcurrentTasks <= 0) {
                        labelStatus.setText(bundle.getString("Download_completed"));
                        progressBar.setVisible(false);
                    }
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                    numOfConcurrentTasks = 0;
                } catch (java.util.concurrent.ExecutionException e) {
                    String why;
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        if (cause instanceof UnsupportedOperationException) {
                            why = cause.getMessage();
                        } else if (cause instanceof RuntimeException) {
                            why = cause.getMessage();
                        } else if (cause instanceof FileNotFoundException) {
                            why = bundle.getString("Resource_does_not_exist") + cause.getMessage();
                        } else {
                            why = cause.getMessage();
                        }
                    } else {
                        why = e.getMessage();
                    }
                    logger.log(Level.SEVERE, why, e);
                    JOptionPane.showMessageDialog(null, why, VietOCR.APP_NAME, JOptionPane.ERROR_MESSAGE);
                    progressBar.setVisible(false);
                    labelStatus.setText(null);
                    --numOfConcurrentTasks;
                } catch (java.util.concurrent.CancellationException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                    labelStatus.setText(bundle.getString("Download_cancelled"));
//                    progressBar.setVisible(false);
                    numOfConcurrentTasks = 0;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    JOptionPane.showMessageDialog(null, e.getMessage(), VietOCR.APP_NAME, JOptionPane.ERROR_MESSAGE);
                    progressBar.setVisible(false);
                    labelStatus.setText(bundle.getString("Unable_to_install"));
                    --numOfConcurrentTasks;
                } finally {
                    if (numOfConcurrentTasks <= 0) {
                        btnDownload.setDisable(false);
                        btnCancel.setDisable(true);
                        labelStatus.getScene().setCursor(Cursor.DEFAULT);
                    }
                }
            }
        };
        
        downloadWorker.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setProgress((Integer) evt.getNewValue());
                }
            }
        });
        downloadTracker.add(downloadWorker);
        downloadWorker.execute();
    }

    /**
     * @return the lookupISO_3_1_Codes
     */
    public Properties getLookupISO_3_1_Codes() {
        return lookupISO_3_1_Codes;
    }

    /**
     * @param lookupISO_3_1_Codes the lookupISO_3_1_Codes to set
     */
    public void setLookupISO_3_1_Codes(Properties lookupISO_3_1_Codes) {
        this.lookupISO_3_1_Codes = lookupISO_3_1_Codes;
    }

    /**
     * @return the lookupISO639
     */
    public Properties getLookupISO639() {
        return lookupISO639;
    }

    /**
     * @param lookupISO639 the lookupISO639 to set
     */
    public void setLookupISO639(Properties lookupISO639) {
        this.lookupISO639 = lookupISO639;
    }

    /**
     * @return the tessdataDir
     */
    public File getTessdataDir() {
        return tessdataDir;
    }

    /**
     * @param tessdataDir the tessdataDir to set
     */
    public void setTessdataDir(File tessdataDir) {
        this.tessdataDir = tessdataDir;
    }

    /**
     * @return the installedLanguages
     */
    public String[] getInstalledLanguages() {
        return installedLanguages;
    }

    /**
     * @param installedLanguages the installedLanguages to set
     */
    public void setInstalledLanguages(String[] installedLanguages) {
        this.installedLanguages = installedLanguages;
    }
}
