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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
                controller = new Controller(inputFolder, outputFolder, profilePath, hintsXML, problemDataFolderName);
                HashMap display = controller.initialisation();
                WebView previewView = (WebView) display.get("previewView");
                previewPane.getChildren().add(previewView);
                byProfileTab = (TabPane) display.get("byProfile");
                byProfilePane.setCenter(byProfileTab);
                byImageTab = (TabPane) display.get("byImage");
                byImagePane.setCenter(byImageTab);
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
            HashMap<String, Object> scores = new HashMap();
            // TODO get the scores from the user input to then get the next gen
            ObservableList<Tab> tabs = byProfileTab.getTabs();
            Iterator<Tab> iterator = tabs.iterator();
            while (iterator.hasNext()) {
                Tab profileTab = iterator.next();
                GridPane cells = (GridPane) profileTab.getContent();
                Iterator<Node> cellIterator = cells.getChildren().iterator();
                while (cellIterator.hasNext()) {
                    Node cellElement = cellIterator.next();
                   // System.out.println(cellElement.getId() + " : " +  cellElement.getTypeSelector());
                     if(cellElement instanceof Slider){
                         scores.put(cellElement.getId(), ((Slider)cellElement).getValue());
                         System.out.println(cellElement.getId() + " / Slider value: "+((Slider)cellElement).getValue());
                }
                     if(cellElement instanceof CheckBox){
                         scores.put(cellElement.getId(), ((CheckBox)cellElement).isSelected());
                         System.out.println(cellElement.getId() + " / CheckBox value: "+((CheckBox)cellElement).isSelected());
                }
                     
                }
            }
              
            
            
               HashMap display = controller.mainloop(scores, numOfProfiles);
               
                WebView previewView = (WebView) display.get("previewView");
                previewPane.getChildren().add(previewView);
                byProfileTab = (TabPane) display.get("byProfile");
                byProfilePane.setCenter(byProfileTab);
                byImageTab = (TabPane) display.get("byImage");
                byImagePane.setCenter(byImageTab);
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
}
