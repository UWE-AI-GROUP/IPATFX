/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ipat_fx;

import Src.Controller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author kieran
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Button chooseFilesButton;
    @FXML
    private WebView previewFrame;
    @FXML
    private WebView byImage;
    @FXML
    private WebView byProfile;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu cases;
    @FXML
    private Button abort;
    @FXML
    private Button nextGen;
    @FXML
    private TextField noOfProfiles;
    @FXML
    private TabPane tabPane;

    private WebEngine byProfileEngine;
    private WebEngine byImageEngine;
    private String contextPath;
    private File inputFolder = null;
    private File outputFolder = null;
    private File profilePath = null;
    private File hintsXML = null;
    private String dataPath;
    private String problemDataFolderName;
    private final ArrayList<File> caseFileArray = new ArrayList<>();
    MenuItem[] caseItemArray;

    @FXML
    private void chooseFiles(ActionEvent event) {
        if (problemDataFolderName == null) {
            JOptionPane.showMessageDialog(null, "Please first select a case from the cases tab in the menu bar.\n"
                    + "If no cases exist, ensure the candidate solutions are correctly entered in the /web/data folder.");
        } else {

            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.showOpenDialog(null);
            File[] uploads = chooser.getSelectedFiles();

            for (File fi : uploads) {
                File file;
                String fileName = fi.getName();
                File input = new File(dataPath + "/input/");
                input.mkdirs();
                File output = new File(dataPath + "/output/");
                output.mkdirs();
                if (fileName.lastIndexOf("\\") >= 0) {
                    file = new File(input.getAbsolutePath() + "/" + fileName.substring(fileName.lastIndexOf("/")));
                } else {
                    file = new File(input.getAbsolutePath() + "/" + fileName.substring(fileName.lastIndexOf("/") + 1));
                }
                try {
                    OutputStream outStream;
                    try (InputStream inStream = new FileInputStream(fi)) {
                        outStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int fileLength;
                        while ((fileLength = inStream.read(buffer)) > 0) {
                            outStream.write(buffer, 0, fileLength);
                        }
                    }
                    outStream.close();
                } catch (IOException ex) {
                    Stage dialogStage = new Stage();
                    dialogStage.initModality(Modality.WINDOW_MODAL);
                    dialogStage.setScene(new Scene(VBoxBuilder.create().
                            children(new Text(ex.getMessage()), new Button("Ok.")).
                            alignment(Pos.CENTER).padding(new Insets(5)).build()));
                    dialogStage.show();
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, "Error loading file see mainframe ", ex);
                }
            }
            try {
                Controller controller = new Controller(inputFolder, outputFolder, profilePath, hintsXML, problemDataFolderName);
                HashMap HTML_Strings = controller.initialisation();
                String byProfileHTML = (String) HTML_Strings.get("byProfile");
                System.out.println("#######");
                System.out.println(byProfileHTML);
                System.out.println("#######");
                byProfileEngine.loadContent(byProfileHTML);
                String byImageHTML = (String) HTML_Strings.get("byImage");
                byImageEngine.loadContent(byImageHTML);
            } catch (IOException ex) {
                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setScene(new Scene(VBoxBuilder.create().
                        children(new Text(ex.getMessage()), new Button("Ok.")).
                        alignment(Pos.CENTER).padding(new Insets(5)).build()));
                dialogStage.show();
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @FXML
    public void nextGeneration() {

        //check if num of profiles is a valid input 
        int numOfProfiles = Integer.parseInt(noOfProfiles.getText());
        if (numOfProfiles > 8) {
            JOptionPane.showMessageDialog(null, "Please make the number of profiles smaller than 8.");
        } else {
            WebEngine engine = byProfile.getEngine();

            engine.setJavaScriptEnabled(true);
            JSObject window = (JSObject) engine.executeScript("window");
            window.setMember("app", new JavaApp());
            JSObject scores = (JSObject) engine.executeScript("function() {"
                    + "var scores = {};"
                    + "var all = document.getElementByClassName('cell');"
                    + "    for (var i = 0; i < all.length; i++) {"
                    + "        var inputs = $(all[i]).find(\"input\");"
                    + "        for (var j = 0; j < inputs.length; j++) {"
                    + "            var name = $(inputs[j]).prop('id');"
                    + "            if ($(inputs[j]).prop('type') === \"checkbox\") {"
                    + "                var value = $(inputs[j]).is(':checked');"
                    + "                scores[name] = value;"
                    + "            }"
                    + "            else if ($(inputs[j]).prop('type') === \"range\") {"
                    + "                var value = $(inputs[j]).val();"
                    + "                scores[name] = value;"
                    + "            }"
                    + "            else {"
                    + "                alert(\"Error: A javascript JQuery check needs to be implemented for \" + $(inputs[i]).attr('id') + \" in javascript.js\");"
                    + "            }"
                    + "        }"
                    + "    }"
                    + "app.getScores(scores);}");
            System.out.println(scores);
        }
    }

    @FXML
    public void abort() {
        System.out.println("abort button pressed");
    }

    @FXML
    public void resetScores() {
        System.out.println("reset scores button pressed");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        contextPath = System.getProperty("user.dir") + "/web/";
        File logFile = new File(contextPath + "/log/log4j-IPAT.log");
        System.setProperty("rootPath", logFile.getAbsolutePath());
        dataPath = contextPath + "/Client Data";
        File prePopulatedProfilePath = new File(contextPath + "/data/");
        File[] listFiles = prePopulatedProfilePath.listFiles();

        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                caseFileArray.add(listFile);
            }
        }

        caseItemArray = new MenuItem[caseFileArray.size()];
        for (int i = 0; i < caseFileArray.size(); i++) {
            File get = caseFileArray.get(i);
            MenuItem menuItem = new MenuItem(get.getName());
            cases.getItems().add(menuItem);
            menuBar.getMenus().add(cases);
            caseItemArray[i] = menuItem;
        }

        problemDataFolderName = "/" + caseItemArray[0].getText();
        System.out.println("case = " + problemDataFolderName);
        profilePath = new File(contextPath + "/data" + problemDataFolderName + "/Profiles/");
        hintsXML = new File(contextPath + "/data" + problemDataFolderName + "/hints.xml");
        inputFolder = new File(dataPath + "/input/");
        outputFolder = new File(dataPath + "/output/");

        for (MenuItem caseItem : caseItemArray) {
            caseItem.setOnAction(e -> {
                MenuItem mItem = (MenuItem) e.getSource();
                problemDataFolderName = "/" + mItem.getText();
                System.out.println("case = " + problemDataFolderName);
                profilePath = new File(contextPath + "/data" + problemDataFolderName + "/Profiles/");
                hintsXML = new File(contextPath + "/data" + problemDataFolderName + "/hints.xml");
                inputFolder = new File(dataPath + "/input/");
                outputFolder = new File(dataPath + "/output/");
            });
        }
        byProfileEngine = byProfile.getEngine();
        byImageEngine = byImage.getEngine();
        byProfileEngine.setUserStyleSheetLocation("file:///" + contextPath + "css/StyleSheet.css");
        byImageEngine.setUserStyleSheetLocation("file:///" + contextPath + "css/StyleSheet.css");

//        String script = "script = document.createElement('script');"
//                + "script.onload = function() {};"
//                + "var head = document.getElementsByTagName(\"head\")[0];"
//                + "script.type = 'text/javascript';"
//                + "script.src = 'https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js';"
//                + "head.appendChild(script);";
//
//        byProfileEngine.executeScript(script);
//        byImageEngine.executeScript(script);

        byProfileEngine.setOnAlert((WebEvent<String> event) -> {
            System.out.println(event.getData());
        });

    }

    public class JavaApp {

        public void exit() {
            Platform.exit();
        }

        public void preview(String src) {
            System.out.println(src);
            previewFrame.getEngine().load(src);
        }

        public void getScores(JSObject scores) {
            System.out.println(" we have them now! ");
        }
    }

}
