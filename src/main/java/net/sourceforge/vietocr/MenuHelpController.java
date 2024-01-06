/**
 * Copyright @ 2016 Quan Nguyen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package net.sourceforge.vietocr;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class MenuHelpController implements Initializable {

    @FXML
    private MenuItem miHelp;
    @FXML
    private MenuItem miAbout;

    public static final String EVENT_TYPE_CLICK = "click";
    public static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    public static final String EVENT_TYPE_MOUSEOUT = "mouseout";

    Stage helpDialog;

    private final static Logger logger = Logger.getLogger(MenuHelpController.class.getName());

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (event.getSource() == miHelp) {
            if (helpDialog == null) {
                Label urlLabel = new Label();
                urlLabel.setTranslateX(5);
                WebView webView = new WebView();
                URL url = getClass().getResource("/readme.html");
                WebEngine webEngine = webView.getEngine();
                webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            EventListener listener = (Event ev) -> {
                                String domEventType = ev.getType();
                                if (domEventType.equals(EVENT_TYPE_CLICK)) {
                                    String href = ((Element) ev.getTarget()).getAttribute("href");
                                    try {
                                        if (href.startsWith("readme")) {
                                            webEngine.load(getClass().getResource("/" + href).toExternalForm());
                                        } else {
                                            linkActivated(new URI(href).toURL());
                                        }
                                    } catch (Exception e) {
                                        logger.log(Level.WARNING, e.getMessage(), e);
                                    }
                                    ev.preventDefault(); // prevent loading into webview; launch external browser only
                                } else if (domEventType.equals(EVENT_TYPE_MOUSEOVER)) {
                                    String href = ((Element) ev.getTarget()).getAttribute("href");
                                    urlLabel.setText(href);
                                } else if (domEventType.equals(EVENT_TYPE_MOUSEOUT)) {
                                    urlLabel.setText(null);
                                }
                            };

                            Document doc = webEngine.getDocument();
                            NodeList nodeList = doc.getElementsByTagName("a");
                            for (int i = 0; i < nodeList.getLength(); i++) {
                                Node el = nodeList.item(i);
                                ((EventTarget) el).addEventListener(EVENT_TYPE_CLICK, listener, false);
                                ((EventTarget) el).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                                ((EventTarget) el).addEventListener(EVENT_TYPE_MOUSEOUT, listener, false);
                            }
                        }
                    }
                });

                webEngine.load(url.toExternalForm());
                helpDialog = new Stage();
                helpDialog.initModality(Modality.WINDOW_MODAL);
                helpDialog.setTitle("Help");
                VBox vbox = new VBox();
                vbox.setSpacing(5);
                vbox.getChildren().addAll(webView, urlLabel);
                VBox.setVgrow(webView, Priority.ALWAYS);
                helpDialog.setScene(new Scene(vbox));
            }
            helpDialog.setIconified(false);
            helpDialog.show();
        } else if (event.getSource() == miAbout) {
            try {
                Properties config = new Properties();
                config.loadFromXML(getClass().getResourceAsStream("config.xml"));
                String version = config.getProperty("Version");
                LocalDate releaseDate = LocalDate.parse(config.getProperty("ReleaseDate"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                String msg = VietOCR.APP_NAME + " " + version + " \u00a9 2007\n"
//                        + bundle.getString("program_desc") + "\n"
                        + String.format("JavaFX GUI Frontend for Tesseract %s OCR Engine", config.getProperty("TessVersion")) + "\n"
                        + releaseDate.format(formatter)
                        + "\nhttp://vietocr.sourceforge.net";
                new Alert(Alert.AlertType.INFORMATION, msg).show();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Follows the reference in an link. The given url is the requested
     * reference. By default this calls <a href="#setPage">setPage</a>, and if
     * an exception is thrown the original previous document is restored and a
     * beep sounded. If an attempt was made to follow a link, but it represented
     * a malformed url, this method will be called with a null argument.
     *
     * @param url the URL to follow
     */
    protected void linkActivated(URL url) {
        try {
            if (url.toString().startsWith("jar:")) {
//                html.setPage(url);
            } else {
                Desktop.getDesktop().browse(url.toURI());
            }
        } catch (IOException | URISyntaxException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

}
