package net.sourceforge.vietocr;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VietOCR extends Application {

    public final static String APP_NAME = "VietOCR";
    public final static boolean MAC_OS_X = System.getProperty("os.name").startsWith("Mac");
    final static Locale VIETNAM = new Locale("vi", "VN");
    protected static Locale systemDefault;
    protected boolean localeVietOn;
    protected final Preferences prefs = Preferences.userRoot().node("/net/sourceforge/vietocr");

    @Override
    public void start(Stage stage) throws Exception {
        if (MAC_OS_X && Locale.getDefault().getCountry().equals("VN")) {
            Locale.setDefault(VIETNAM);
        }
        systemDefault = Locale.getDefault();
        Locale.setDefault(
                (localeVietOn = prefs.getBoolean("localeVN", false)) || systemDefault.getLanguage().equals("vi")
                ? VIETNAM : Locale.US);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Gui.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("net.sourceforge.vietocr.Gui", Locale.getDefault()));
        Parent root = fxmlLoader.load();
        ((GuiController) fxmlLoader.getController()).setStageState(stage);

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setScene(scene);
        stage.setTitle(APP_NAME);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
