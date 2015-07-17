/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Src;

import Algorithms.Hint;
import ipat_fx.FXMLDocumentController;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author kieran
 */
public class DesktopDisplay extends Display {

    @Override
    public HashMap<String, Object> loadDisplay(HashMap hintMap, Artifact[] artifacts, int noOfProfiles) {

        HashMap<String, Object> tempByImageStore = new HashMap<>(); // temp for byImage
        HashMap<String, Object> display = new HashMap();

       
        WebView preview = new WebView();
        TabPane byProfile = new TabPane();
        TabPane byImage = new TabPane();
        int resultCount = 0;

        for (int i = 0; i < noOfProfiles; i++) { // Create tabs based on number of profiles

            Tab byProfileTab = new Tab();
            byProfileTab.setId("li_" + i);
            byProfileTab.setText(String.valueOf(i));

            for (Artifact artifact : artifacts) { // cycle through all results per profile

                String name = artifact.getFilename().substring(artifact.getFilename().indexOf("-") + 1);
                String[] parts = name.split("-");
                int profileNum = Integer.parseInt(parts[0].substring(parts[0].indexOf("_") + 1));
                if (profileNum == i) { // if result matches the current Profile
                    GridPane gridpane = new GridPane(); // create a gridpane "cell" to put the artefact components
                    gridpane.setPadding(new Insets(10, 10, 10, 10));
                    gridpane.setVgap(8);
                    gridpane.setHgap(10);
                    WebView webview = new WebView();  // the visible thumbnail preview of the artefact
                    WebEngine engine = webview.getEngine();
                    engine.load("file:///" + artifact.getFilepath());
                    webview.setId("frame_" + resultCount);
                    // if clicked set PreviewFrame to this src
                    webview.setOnMouseClicked(e -> {
                        preview.getEngine().load("file:///" + artifact.getFilepath());
                    });
                    GridPane.setConstraints(webview, 0, 0);
                    gridpane.getChildren().add(webview);
                    Set keySet = hintMap.keySet();// get the hints one by one and apply to gridpane "cell"
                    int keyCount = 0;
                    for (Object key : keySet) {
                        String k = (String) key;
                        Hint h = (Hint) hintMap.get(k);
                        String displaytype = h.getDisplaytype();
                        Label label;
                        switch (displaytype) {
                            case "range":
                                Slider slider = new Slider();
                                slider.setMax(h.getRangeMax());
                                slider.setMin(h.getRangeMin());
                                slider.setValue(Double.valueOf(h.getDefaultValue()));
                                slider.setId(h.getHintName() + "_" + resultCount);
                                label = new Label();
                                label.setId(h.getHintName() + "_" + resultCount);
                                label.setText(h.getDisplaytext());
                                GridPane.setConstraints(label, keyCount, 1);
                                GridPane.setConstraints(slider, keyCount, 2);
                                gridpane.getChildren().add(label);
                                gridpane.getChildren().add(slider);
                                break;
                            case "checkbox":
                                CheckBox checkbox = new CheckBox();
                                checkbox.setId(h.getHintName() + "_" + resultCount);
                                label = new Label();
                                label.setId(h.getHintName() + "_" + resultCount);
                                label.setText(h.getDisplaytext());
                                GridPane.setConstraints(label, keyCount, 1);
                                GridPane.setConstraints(checkbox, keyCount, 2);
                                gridpane.getChildren().add(label);
                                gridpane.getChildren().add(checkbox);
                                break;
                            default:
                                throw new AssertionError();
                        }
                    }
                    resultCount += 1;
                    // add cell to the tempByImageStore based on it's artefact name 
                    String nameOfArtefact = name.substring(name.indexOf("-") + 1);
                    if (tempByImageStore.containsKey(nameOfArtefact)) {
                        GridPane get = (GridPane) tempByImageStore.get(nameOfArtefact);
                        tempByImageStore.put(nameOfArtefact, get);
                    } else {
                        tempByImageStore.put(nameOfArtefact, gridpane);
                    }
                    byProfileTab.setContent(gridpane); // TODO this will overwrite other cells (cant have multiple profiles)
                }
            }
         
            byProfile.getTabs().add(byProfileTab);
            byProfile.setId("byProfileTabPane");
            display.put("byProfile", byProfile);
        }

        Set<String> keySet = tempByImageStore.keySet();
        Iterator<String> iterator = keySet.iterator();
        resultCount = 0;
        while (iterator.hasNext()) {
            String artefactName = iterator.next();
            System.out.println("by Image = " + artefactName);
            Tab byImageTab = new Tab();
            byImageTab.setId("li_" + resultCount);
            byImageTab.setText(artefactName);
            GridPane cells = (GridPane) tempByImageStore.get(iterator.next());
            byImageTab.setContent(cells);
            byImage.getTabs().add(byImageTab);
            resultCount++;
        }
        byImage.setId("byImageTabPane");
        display.put("byImage", byImage);
        display.put("count", Integer.toString(artifacts.length));
        display.put("previewView", preview);
        return display;
    }
}
