/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Src;

import Algorithms.Hint;
import ipat_fx.FXMLDocumentController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author kieran
 */
public class DesktopDisplay extends Display {

    @Override
    public HashMap<String, Object> loadDisplay(HashMap hintMap, Artifact[] artifacts, int noOfProfiles) {

        HashMap<String, Object> tempByImageStore = new HashMap<>(); // temp for byImage
        HashMap<String, Object> display = new HashMap();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        FXMLDocumentController controller = (FXMLDocumentController) loader.getController();
        System.out.println("Controller " + controller);
        
        TabPane byProfile = new TabPane();
        TabPane byImage = new TabPane();
        int resultCount = 0;

        for (int i = 0; i < noOfProfiles; i++) { // Create tabs based on number of profiles
           
            Tab tab = new Tab();
            tab.setId("li_" + i);
            tab.setText(String.valueOf(i));
 
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
                    webview.setOnMouseClicked(e -> controller.setPreviewFrame("file:///" + artifact.getFilepath()));
                    GridPane.setConstraints(webview, 0, 0);
                    gridpane.getChildren().add(webview);
                    System.out.println("I am a webview " + webview.getId());
                    // get the hints one by one and apply to gridpane "cell"
                    Set keySet = hintMap.keySet();
                    int keyCount = 0;
                    for (Object key : keySet) {
                        String k = (String) key;
                        Hint h = (Hint) hintMap.get(k);
                        String displaytype = h.getDisplaytype();
                        Label label;
                        System.out.println("I am a hint " + h.getHintName());
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
                    String key = name.substring(name.indexOf("-") + 1);
                    if (tempByImageStore.containsKey(key)) {
                        GridPane get = (GridPane) tempByImageStore.get(key);
                        tempByImageStore.put(key, get);
                    } else {
                        tempByImageStore.put(key, gridpane);
                    }
                    tab.setContent(gridpane); // TODO this will overwrite other cells (cant have multiple profiles)
                }
            }
            byProfile.getTabs().add(tab);
            byProfile.setId("byProfileTabPane");
            display.put("byProfile", byProfile);
        }

       
        Set<String> keySet = tempByImageStore.keySet();
        Iterator<String> iterator = keySet.iterator();
        resultCount = 0;
        while (iterator.hasNext()) {
            String next = iterator.next();
            Tab tab = new Tab();
            tab.setId("li_" + resultCount);
            tab.setText(next);
            GridPane get = (GridPane) tempByImageStore.get(iterator.next()); 
            tab.setContent(get);
            byImage.getTabs().add(tab);
            resultCount++;
        }
        byImage.setId("byImageTabPane");
        display.put("byImage", byImage);
        display.put("count", Integer.toString(artifacts.length));
        return display;
    }

}
