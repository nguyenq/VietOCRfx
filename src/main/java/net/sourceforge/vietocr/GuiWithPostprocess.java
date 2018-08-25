package net.sourceforge.vietocr;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import net.sourceforge.vietocr.postprocessing.Processor;

public class GuiWithPostprocess extends GuiWithOCR {
    
    private final String strDangAmbigsPath = "DangAmbigsPath";
    private final String strDangAmbigs = "DangAmbigs";
    private final String strReplaceHyphensEnabled = "ReplaceHyphensEnabled";
    private final String strRemoveHyphensEnabled = "RemoveHyphensEnabled";
    protected String dangAmbigsPath;
    protected boolean dangAmbigsOn;
    protected boolean replaceHyphensEnabled;
    protected boolean removeHyphensEnabled;
    
    @FXML
    private Button btnPostProcess;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        
        dangAmbigsPath = prefs.get(strDangAmbigsPath, new File(baseDir, "data").getPath());
        dangAmbigsOn = prefs.getBoolean(strDangAmbigs, true);
        replaceHyphensEnabled = prefs.getBoolean(strReplaceHyphensEnabled, false);
        removeHyphensEnabled = prefs.getBoolean(strRemoveHyphensEnabled, false);
    }
    
    @FXML
    @Override
    protected void handleAction(javafx.event.ActionEvent event) {
        if (event.getSource() == btnPostProcess) {
            splitPane.setCursor(Cursor.WAIT);
            statusBar.setCursor(Cursor.WAIT);
            
            Task postprocessWorker = new Task<Void>() {
                String result;
                
                @Override
                public Void call() throws Exception {
                    updateMessage(bundle.getString("Correcting_errors..."));
                    String selectedText = textarea.getSelectedText();
                    // use only the first language if multiple are selected
                    result = Processor.postProcess(!selectedText.trim().equals("") ? selectedText : textarea.getText(), curLangCode.split("\\+")[0], dangAmbigsPath, dangAmbigsOn, replaceHyphensEnabled);
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    super.succeeded();
                    updateMessage(bundle.getString("Correction_completed"));
                    updateProgress(1, 1);
                    
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.progressProperty().unbind();
                            textarea.setText(result);
                            splitPane.setCursor(Cursor.DEFAULT);
                            statusBar.setCursor(Cursor.DEFAULT);
                        }
                    });
                }
            };
            
            progressBar.progressProperty().bind(postprocessWorker.progressProperty());
            labelStatus.textProperty().bind(postprocessWorker.messageProperty());
            new Thread(postprocessWorker).start();
        } else {
            super.handleAction(event);
        }
    }
    
    @Override
    public void savePrefs() {
        prefs.put(strDangAmbigsPath, dangAmbigsPath);
        prefs.putBoolean(strDangAmbigs, dangAmbigsOn);
        prefs.putBoolean(strReplaceHyphensEnabled, replaceHyphensEnabled);
        prefs.putBoolean(strRemoveHyphensEnabled, removeHyphensEnabled);
        
        super.savePrefs();
    }
}
