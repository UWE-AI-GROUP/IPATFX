/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ipat_fx;

import Src.Controller;
import Src.Interaction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author kieran
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Button chooseFilesButton;
    @FXML
    private Pane previewPane;
    @FXML
    private BorderPane byImagePane;
    @FXML
    private BorderPane byProfilePane;
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
    private TextArea fileTextArea;
    @FXML
    private TabPane tabPane;

    private TabPane byProfileTab;
    private TabPane byImageTab;

    private String contextPath;
    private File inputFolder = null;
    private File outputFolder = null;
    private File profilePath = null;
    private File hintsXML = null;
    private String dataPath;
    private String problemDataFolderName;
    private final ArrayList<File> caseFileArray = new ArrayList<>();
    public MenuItem[] caseItemArray;
    public Controller controller;
    public Interaction interaction = new Interaction();
    private String tabFlag;

    @FXML
    private void chooseFiles(ActionEvent event) {

        int numOfProfiles = Integer.parseInt(noOfProfiles.getText());
        if (numOfProfiles > 8) {
            JOptionPane.showMessageDialog(null, "Please set the number of Profiles to be less than 8.");
        } else if (problemDataFolderName == null) {
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
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, "Error loading file see mainframe ", ex);
                }
                fileTextArea.appendText(fileName + "\n");
            }
            try {
                controller = new Controller(inputFolder, outputFolder, profilePath, hintsXML, problemDataFolderName);
                HashMap<String, Object> display = controller.initialisation();
                WebView previewView = (WebView) display.get("previewView");
                previewPane.getChildren().add(previewView);
                @SuppressWarnings("unchecked")
                HashMap<String, ArrayList<GridPane>> map = (HashMap<String, ArrayList<GridPane>>) display.get("results");
                byProfileTab = getByProfile(map, numOfProfiles);
                byProfilePane.setCenter(byProfileTab);
                
                tabPane.getSelectionModel().select(0);
                tabFlag = "byProfile";
                
                tabPane.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> ov, Number oldValue, Number newValue) -> {
                    if (newValue == Number.class.cast(1)) {
                        tabFlag = "byImage";
                        byImageTab = getByImage(map);
                        byImagePane.setCenter(byImageTab);
                    } else if (newValue == Number.class.cast(0)) {
                        tabFlag = "byProfile";
                        byProfileTab = getByProfile(map, numOfProfiles);
                        byProfilePane.setCenter(byProfileTab);
                    } else {
                        System.out.println("Error this tab has not been created.");
                    }
                });

            } catch (IOException ex) {
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
            HashMap<String, Object> scores = new HashMap<>();
            // TODO get the scores from the user input to then get the next gen

            ObservableList<Tab> tabs = null;

            
            if (tabFlag.equalsIgnoreCase("byImage")) {
                tabs = byImageTab.getTabs();
            } else if (tabFlag.equalsIgnoreCase("byProfile")) {
                tabs = byProfileTab.getTabs();
            } else {
                System.out.println("Something is wrong with tabFlag in FXML DOC CONT");
            }

            Iterator<Tab> profileTabIterator = tabs.iterator();
            while (profileTabIterator.hasNext()) {
                Tab profileTab = profileTabIterator.next();
                ScrollPane scrollPane = (ScrollPane) profileTab.getContent();
                GridPane cells = (GridPane) scrollPane.getContent();
                Iterator<Node> cellIterator = cells.getChildren().iterator();
                while (cellIterator.hasNext()) {
                    GridPane cell = (GridPane) cellIterator.next();
                    Iterator<Node> nodeIterator = cell.getChildren().iterator();
                    while (nodeIterator.hasNext()) {
                        Node cellElement = nodeIterator.next();
                        if (cellElement instanceof Slider) {
                            scores.put(cellElement.getId(), String.valueOf(((Slider) cellElement).getValue()));
                        }
                        if (cellElement instanceof CheckBox) {
                            scores.put(cellElement.getId(), ((CheckBox) cellElement).isSelected());
                        }
                    }
                }
            }

            HashMap<String, Object> display = controller.mainloop(scores, numOfProfiles);
            WebView previewView = (WebView) display.get("previewView");
            previewPane.getChildren().add(previewView);
            @SuppressWarnings("unchecked")
            HashMap<String, ArrayList<GridPane>> map = (HashMap<String, ArrayList<GridPane>>) display.get("results");

            if (tabFlag.equalsIgnoreCase("byImage")) {
                byImageTab = getByImage(map);
                byImagePane.setCenter(byImageTab);
            } else if (tabFlag.equalsIgnoreCase("byProfile")) {
                byProfileTab = getByProfile(map, numOfProfiles);
                byProfilePane.setCenter(byProfileTab);
            }

            tabPane.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> ov, Number oldValue, Number newValue) -> {
                if (newValue == Number.class.cast(1)) {
                    byImageTab = getByImage(map);
                    byImagePane.setCenter(byImageTab);
                } else if (newValue == Number.class.cast(0)) {
                    byProfileTab = getByProfile(map, numOfProfiles);
                    byProfilePane.setCenter(byProfileTab);
                } else {
                    System.out.println("Error this tab has not been created.");
                }
            });
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
            caseItemArray[i] = menuItem;
        }

        problemDataFolderName = "/" + caseItemArray[0].getText();
        profilePath = new File(contextPath + "/data" + problemDataFolderName + "/Profiles/");
        hintsXML = new File(contextPath + "/data" + problemDataFolderName + "/hints.xml");
        inputFolder = new File(dataPath + "/input/");
        outputFolder = new File(dataPath + "/output/");

        for (MenuItem caseItem : caseItemArray) {
            caseItem.setOnAction(e -> {
                MenuItem mItem = (MenuItem) e.getSource();
                problemDataFolderName = "/" + mItem.getText();
                profilePath = new File(contextPath + "/data" + problemDataFolderName + "/Profiles/");
                hintsXML = new File(contextPath + "/data" + problemDataFolderName + "/hints.xml");
                inputFolder = new File(dataPath + "/input/");
                outputFolder = new File(dataPath + "/output/");
            });
        }
    }

    public TabPane getByImage(HashMap<String, ArrayList<GridPane>> map) {

        TabPane tabpane = new TabPane();
        Tab tabForImage;
        FlowPane paneForImage;

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            tabForImage = new Tab();
            paneForImage = new FlowPane();
            String nameOfArtefact = iterator.next();
            tabForImage.setId("li_" + nameOfArtefact);
            tabForImage.setText(nameOfArtefact);
            ArrayList<GridPane> cells = map.get(nameOfArtefact);
            for (GridPane cell1 : cells) {
                GridPane cell = cell1;
                //paneForImage.add(cell, 0, i);
                paneForImage.getChildren().add(cell);
            }
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(paneForImage);
            tabForImage.setContent(scrollPane);
            tabpane.getTabs().add(tabForImage);
        }
        return tabpane;
    }

    public TabPane getByProfile(HashMap<String, ArrayList<GridPane>> map, int noOfProfiles) {

        TabPane tabpane = new TabPane();
        Tab tabForProfile;
        GridPane paneForProfile;

        for (int i = 0; i < noOfProfiles; i++) {
            tabForProfile = new Tab();
            paneForProfile = new GridPane();
            tabForProfile.setId("li_Profile_" + i);
            tabForProfile.setText("Profile " + i);
            int j = 0;
            for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext(); j++) {
                String nameOfArtefact = iterator.next();
                ArrayList<GridPane> cells = map.get(nameOfArtefact);
                paneForProfile.add(cells.get(i), 0, j);
            }
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(paneForProfile);
            tabForProfile.setContent(scrollPane);
            tabpane.getTabs().add(tabForProfile);
        }
        return tabpane;
    }
}
