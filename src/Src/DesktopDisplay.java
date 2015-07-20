/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Src;

import Algorithms.Hint;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
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

        HashMap<String, GridPane> tempByImageStore = new HashMap<>(); // temp for byImage
        HashMap<String, Object> display = new HashMap();

       
        WebView preview = new WebView();
        TabPane byProfile = new TabPane();
        TabPane byImage = new TabPane();
        int resultCount = 0;

        for (int i = 0; i < noOfProfiles; i++) { // Create tabs based on number of profiles

            Tab profileNumberTab = new Tab();
            profileNumberTab.setId("li_" + i);
            profileNumberTab.setText(String.valueOf(i));

            for (Artifact artifact : artifacts) { // cycle through all results per profile

                String name = artifact.getFilename().substring(artifact.getFilename().indexOf("-") + 1);
                String[] parts = name.split("-");
                int profileNum = Integer.parseInt(parts[0].substring(parts[0].indexOf("_") + 1));
                if (profileNum == i) { // if result matches the current Profile
                    
                    GridPane byProfileCell = new GridPane(); // create a gridpane "cell" to put the artefact components
                    GridPane byImageCell = new GridPane();
                    
                    byProfileCell.setPadding(new Insets(10, 10, 10, 10));
                    byProfileCell.setVgap(8);
                    byProfileCell.setHgap(10);
                    
                    byImageCell.setPadding(new Insets(10, 10, 10, 10));
                    byImageCell.setVgap(8);
                    byImageCell.setHgap(10);
                    
                    WebView webview = new WebView();  // the thumbnail preview of the artefact in cell
                    WebEngine engine = webview.getEngine();
                    engine.setJavaScriptEnabled(true);
                    engine.load("file:///" + artifact.getFilepath());
                    
                    webview.setId("frame_" + resultCount);
                    // if clicked set PreviewFrame to this src
                    webview.setOnMouseClicked(e -> {
                        preview.getEngine().load("file:///" + artifact.getFilepath());
                    });
                    GridPane.setConstraints(webview, 0, 0);
                    byProfileCell.getChildren().add(webview);
                    System.out.println("worked for webvew 1");
                    
                    WebView webview2 = new WebView();  // the thumbnail preview of the artefact in cell
                    WebEngine engine2 = webview2.getEngine();
                    engine2.setJavaScriptEnabled(true);
                    engine2.load("file:///" + artifact.getFilepath());

                    webview2.setId("frame_" + resultCount);
                    // if clicked set PreviewFrame to this src
                    webview2.setOnMouseClicked(e -> {
                        preview.getEngine().load("file:///" + artifact.getFilepath());
                    });
                    GridPane.setConstraints(webview2, 0, 0);
                    byImageCell.getChildren().add(webview2);
                    System.out.println("worked for webvew 2");  
                    
                    
                 
                 
                    Set keySet = hintMap.keySet();// get the hints one by one and apply to cell
                    int keyCount = 0;
                    for (Object key : keySet) {
                        String k = (String) key;
                        Hint h = (Hint) hintMap.get(k);
                        String displaytype = h.getDisplaytype();
                        switch (displaytype) {
                            
                            case "range":
                                Slider slider = new Slider();
                                slider.setMax(h.getRangeMax());
                                slider.setMin(h.getRangeMin());
                                slider.setValue(Double.valueOf(h.getDefaultValue()));
                                slider.setId(h.getHintName() + "_" + resultCount);
                                Label label = new Label();
                                label.setId(h.getHintName() + "_" + resultCount);
                                label.setText(h.getDisplaytext());
                                GridPane.setConstraints(label, keyCount, 1);
                                GridPane.setConstraints(slider, keyCount, 2);
                                byProfileCell.getChildren().add(label);
                                byProfileCell.getChildren().add(slider);
                                
                                Slider slider2 = new Slider();
                                slider2.setMax(h.getRangeMax());
                                slider2.setMin(h.getRangeMin());
                                slider2.setValue(Double.valueOf(h.getDefaultValue()));
                                slider2.setId(h.getHintName() + "_" + resultCount);
                                Label label2 = new Label();
                                label2.setId(h.getHintName() + "_" + resultCount);
                                label2.setText(h.getDisplaytext());
                                GridPane.setConstraints(label2, keyCount, 1);
                                GridPane.setConstraints(slider2, keyCount, 2);
                                byImageCell.getChildren().add(label2);
                                byImageCell.getChildren().add(slider2);
                                break;
                                
                            case "checkbox":
                                CheckBox checkbox = new CheckBox();
                                checkbox.setId(h.getHintName() + "_" + resultCount);
                                Label label3 = new Label();
                                label3.setId(h.getHintName() + "_" + resultCount);
                                label3.setText(h.getDisplaytext());
                                GridPane.setConstraints(label3, keyCount, 1);
                                GridPane.setConstraints(checkbox, keyCount, 2);
                                byProfileCell.getChildren().add(label3);
                                byProfileCell.getChildren().add(checkbox);
                                
                                CheckBox checkbox2 = new CheckBox();
                                checkbox2.setId(h.getHintName() + "_" + resultCount);
                                Label label4 = new Label();
                                label4.setId(h.getHintName() + "_" + resultCount);
                                label4.setText(h.getDisplaytext());
                                GridPane.setConstraints(label4, keyCount, 1);
                                GridPane.setConstraints(checkbox2, keyCount, 2);
                                byImageCell.getChildren().add(label4);
                                byImageCell.getChildren().add(checkbox2);
                                break;
                            default:
                                throw new AssertionError();
                        }
                        keyCount++;
                    }
                    
                    
                    // change the store to have a Pane to hold the cell nodes for ByImage
                    String nameOfArtefact = name.substring(name.indexOf("-") + 1);
                    if (tempByImageStore.containsKey(nameOfArtefact)) {
                        GridPane getPane = (GridPane) tempByImageStore.get(nameOfArtefact);
                        getPane.add(byImageCell, 0, i);
                        tempByImageStore.put(nameOfArtefact, getPane);
                    } else {
                        GridPane newPane = new GridPane();
                        newPane.add(byImageCell, 0, i);
                        tempByImageStore.put(nameOfArtefact, newPane);
                    }
                    
                    
                    //append inner grid pane to appropriate tab
                    profileNumberTab.setContent(byProfileCell); // TODO this will overwrite other cells (cant have multiple profiles)
                    resultCount += 1;
                    
                    
                    //print contntents of byprofilecell nD BY IMAGE CELL
                    
                }
            }
         
            byProfile.getTabs().add(profileNumberTab);
            byProfile.setId("byProfileTabPane");
            display.put("byProfile", byProfile);
        }

        Set<String> keySet = tempByImageStore.keySet();
        Iterator<String> iterator = keySet.iterator();
        resultCount = 0;
        Tab artifactNameTab;
        while (iterator.hasNext()) {
            String artefactName = iterator.next();
            artifactNameTab = new Tab();
            artifactNameTab.setId("li_" + resultCount);
            artifactNameTab.setText(artefactName);
            GridPane byImageCells = (GridPane) tempByImageStore.get(artefactName);
            
            artifactNameTab.setContent(byImageCells);
            byImage.getTabs().add(artifactNameTab);
            resultCount++;
        }
        
        Iterator<Tab> iterator1 = byImage.getTabs().iterator();
        while (iterator1.hasNext()) {
            Tab next = iterator1.next();
            GridPane content = (GridPane) next.getContent();
            ObservableList<Node> children = content.getChildren();
            Iterator<Node> iterator2 = children.iterator();
            while (iterator2.hasNext()) {
                GridPane next1 = (GridPane) iterator2.next();
                ObservableList<Node> children1 = next1.getChildren();
                Iterator<Node> iterator3 = children1.iterator();
                while (iterator3.hasNext()) {
                    Node next2 = iterator3.next();
                    System.out.println("The ID of " + next2.getTypeSelector() + " is " + next2.getId());
                }
            }
        }
        
        byImage.setId("byImageTabPane");
        display.put("byImage", byImage);
        display.put("count", Integer.toString(artifacts.length));
        display.put("previewView", preview);
        return display;
    }
}
