/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ipat_fx;

import Algorithms.Hint;
import Src.Controller;
import Src.Interaction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author kieran
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Pane previewPane;
    @FXML
    private BorderPane byImagePane;
    @FXML
    private BorderPane byProfilePane;
    @FXML
    private Menu cases;
    @FXML
    private TextField noOfProfiles;
    @FXML
    private TabPane tabPane;

    private TabPane byProfileTab;
    private TabPane byImageTab;
    private String contextPath;
    private File inputFolder;
    private File outputFolder;
    private File profilePath;
    private File hintsXML;
    private String dataPath;
    private String problemDataFolderName;
    private final ArrayList<File> caseFileArray = new ArrayList<>();
    public MenuItem[] caseItemArray;
    public Controller controller;
    public Interaction interaction = new Interaction();
    private String tabFlag;

    @FXML
    private void saveOption(ActionEvent event) {
        FileChooser fc = new FileChooser();
        File file = new File(contextPath + "Saves/");
        if (!file.exists()) {
            file.mkdirs();
        }
        fc.setInitialDirectory(file);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        fc.setInitialFileName("Ipat_" + dateFormat.format(date));
        File dest = fc.showSaveDialog(null);
        File src = new File(dataPath);
        try {
            FileUtils.copyDirectory(src, dest);
        } catch (IOException ex) {
            Logger.getLogger(IPAT_FX.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void loadOption(ActionEvent event) {

        DirectoryChooser dc = new DirectoryChooser();
        File file = new File(contextPath + "Saves/");
        dc.setInitialDirectory(file);
        File dest = new File(dataPath);
        File seeds = new File(dataPath + "/seeds/");
        seeds.delete();
        seeds.mkdirs();
        dest.delete();
        dest.mkdirs();
        File src = dc.showDialog(null);

        try {

            FileUtils.copyDirectory(src, dest);
            File profiles = new File(outputFolder.getAbsolutePath() + "/generations");
            File[] listFiles = profiles.listFiles();

            int lastGeneration = 0;
            for (File listFile : listFiles) {
                if (!listFile.isDirectory()) {
                    String profileName = listFile.getName();
                    System.out.println("profileName : " + profileName);
                    int generation = Integer.parseInt(profileName.substring((profileName.indexOf('_') + 1), profileName.indexOf('-')));
                    System.out.println("generation : " + generation);
                    if (generation > lastGeneration) {
                        lastGeneration = generation;
                    }
                }
            }

            for (File listFile : listFiles) {
                String profileName = listFile.getName();
                if (Integer.parseInt(profileName.substring((profileName.indexOf('_') + 1), profileName.indexOf('-'))) == lastGeneration) {
                    FileUtils.copyFile(listFile, new File(seeds + "/" + listFile.getName()));
                }
            }

            controller = new Controller(inputFolder, outputFolder, seeds, hintsXML, problemDataFolderName);

            HashMap<String, Object> display = controller.initialisation();
            WebView previewView = (WebView) display.get("previewView");
            previewPane.getChildren().add(previewView);
            @SuppressWarnings("unchecked")
            HashMap<String, ArrayList<GridPane>> map = (HashMap<String, ArrayList<GridPane>>) display.get("results");

            if (tabFlag.equalsIgnoreCase("byImage")) {
                byImageTab = getByImage(map);
                byImagePane.setCenter(byImageTab);
            } else if (tabFlag.equalsIgnoreCase("byProfile")) {
                byProfileTab = getByProfile(map, controller.noOfProfiles);
                byProfilePane.setCenter(byProfileTab);
            }

            tabPane.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> ov, Number oldValue, Number newValue) -> {
                if (newValue == Number.class.cast(1)) {
                    byImageTab = getByImage(map);
                    byImagePane.setCenter(byImageTab);
                } else if (newValue == Number.class.cast(0)) {
                    byProfileTab = getByProfile(map, controller.noOfProfiles);
                    byProfilePane.setCenter(byProfileTab);
                } else {
                    System.out.println("Error this tab has not been created.");
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(IPAT_FX.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void chooseFiles(ActionEvent event) {

        int numOfProfiles = Integer.parseInt(noOfProfiles.getText());
        if (numOfProfiles > 8) {
            JOptionPane.showMessageDialog(null, "Please set the number of Profiles to be less than 8.");
        } else if (problemDataFolderName == null) {
            JOptionPane.showMessageDialog(null, "Please first select a case from the cases tab in the menu bar.\n"
                    + "If no cases exist, ensure the candidate solutions are correctly entered in the /web/data folder.");
        } else {
            FileChooser chooser = new FileChooser();

            List<File> uploads = chooser.showOpenMultipleDialog(null);

            uploads.stream().forEach((fi) -> {
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

            });
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
                FlowPane cells = (FlowPane) scrollPane.getContent();
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
        byProfilePane.getChildren().clear();
        byImagePane.getChildren().clear();
        previewPane.getChildren().clear();
        noOfProfiles.setText("6");
        try {
            controller = new Controller(inputFolder, outputFolder, profilePath, hintsXML, problemDataFolderName);
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void resetScores() {

        HashMap<String, Hint> hintMap = controller.hints;
        TabPane tabpane = (TabPane) byProfilePane.getChildren().get(0);
        Tab tab = null;
        if (tabFlag.equalsIgnoreCase("byProfile")) {
            tab = tabpane.getTabs().get(0);
        } else if (tabFlag.equalsIgnoreCase("byImage")) {
            tab = tabpane.getTabs().get(0);
        } else {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, "Something wrong with tabFlag");
        }
        ScrollPane scrollPane = (ScrollPane) tab.getContent();
        FlowPane flowPane = (FlowPane) scrollPane.getContent();
        Iterator<Node> cellsIterator = flowPane.getChildren().iterator();
        while (cellsIterator.hasNext()) {
            GridPane cell = (GridPane) cellsIterator.next();
            Iterator<Node> cellIterator = cell.getChildren().iterator();
            while (cellIterator.hasNext()) {
                Node cellElement = cellIterator.next();
                if (cellElement instanceof Slider) {
                    Set<String> keySet = hintMap.keySet();// get the hints one by one and apply to cell
                    int keyCount = 0;
                    for (String key : keySet) {
                        Hint h = hintMap.get(key);
                       Slider slider = ((Slider) cellElement);
                        String[] split = slider.getId().split("_");
                        if (split[0].equalsIgnoreCase(h.getHintName())) {
                            slider.setValue(Double.valueOf(h.getDefaultValue()));
                        }
                    }
                }
                if (cellElement instanceof CheckBox) {
                    ((CheckBox) cellElement).setSelected(false);
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        contextPath = System.getProperty("user.dir") + "/web/";
        dataPath = contextPath + "/Client Data";
        File index = new File(dataPath);
        if (!index.exists()) {
            index.mkdir();
        } else {
            index.delete();
            if (!index.exists()) {
                index.mkdir();
            }
        }

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
        tabFlag = "byProfile";
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
        FlowPane paneForProfile;

        for (int i = 0; i < noOfProfiles; i++) {
            tabForProfile = new Tab();
            paneForProfile = new FlowPane();
            tabForProfile.setId("li_Profile_" + i);
            tabForProfile.setText("Profile " + i);
            int j = 0;
            for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext(); j++) {
                String nameOfArtefact = iterator.next();
                ArrayList<GridPane> cells = map.get(nameOfArtefact);
                paneForProfile.getChildren().add(cells.get(i));
            }
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(paneForProfile);
            tabForProfile.setContent(scrollPane);
            tabpane.getTabs().add(tabForProfile);
        }
        return tabpane;
    }
}
