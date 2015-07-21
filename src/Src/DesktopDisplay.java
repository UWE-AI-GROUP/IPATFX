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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
        WebEngine previewEngine = preview.getEngine();
        previewEngine.setJavaScriptEnabled(true);
        TabPane byProfile = new TabPane();
        byProfile.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        TabPane byImage = new TabPane();
        byImage.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        int resultCount = 0;
        int webViewWidthInColumns=3, webViewHeightInRows=1;
        int column=0,row=0;
        double zoomFactor = 0.25;
        double webPrefWidth= 200, webPrefHeight=200;
        
        for (int currentProfileNumber = 0; currentProfileNumber < noOfProfiles; currentProfileNumber++) { // Create tabs based on number of profiles

            Tab profileNumberTab = new Tab();
            profileNumberTab.setId("byProfile_" + currentProfileNumber);
            profileNumberTab.setText(String.valueOf(currentProfileNumber));
            GridPane byProfileGridPane = new GridPane(); // create a gridpane "cell" to put the artefact components
            int currentArtefactNumber=-1;
            for (Artifact artifact : artifacts) { // cycle through all results per profile
                

                String name = artifact.getFilename().substring(artifact.getFilename().indexOf("-") + 1);
                String[] parts = name.split("-");
                int profileNum = Integer.parseInt(parts[0].substring(parts[0].indexOf("_") + 1));
                if (profileNum == currentProfileNumber) { // if result matches the current Profile
                    
                    currentArtefactNumber++;
                    GridPane byImageCell = new GridPane();
                    GridPane byProfileCell = new GridPane();
                    //gridpane.add(node, column, row[, columnspan, rowspan]);
                    
                    byProfileCell.setPadding(new Insets(10, 10, 10, 10));
                    byProfileCell.setVgap(8);
                    byProfileCell.setHgap(10);
                    byProfileCell.setId("byProfileCell_"+name);
                    
                    byImageCell.setPadding(new Insets(10, 10, 10, 10));
                    byImageCell.setVgap(8);
                    byImageCell.setHgap(10);
                    byImageCell.setId("byImageCell_"+name);
                    
                    WebView webview = new WebView();  // the thumbnail preview of the artefact in cell
                    WebEngine engine = webview.getEngine();
                    engine.setJavaScriptEnabled(true);
                    engine.load("file:///" + artifact.getFilepath());
                    
                    webview.setId("Pframe_" + resultCount);
                    // if clicked set PreviewFrame to this src
                    webview.setOnMouseClicked(e -> {
                        previewEngine.load("file:///" + artifact.getFilepath());
                    });
//                    
                    //scale the contents - somewhat arbitrary factor
                    webview.setZoom(zoomFactor);
                    webview.setPrefSize(webPrefWidth, webPrefHeight);
                    //add the webview to the gridpane
                    column=0;
                    row= currentArtefactNumber*(2+webViewHeightInRows);
                    byProfileCell.add(webview,column,row ,  webViewWidthInColumns, webViewHeightInRows);
                    
                    
                    WebView anotherwebview = new WebView();  // the thumbnail preview of the artefact in cel
                    WebEngine anotherengine = anotherwebview.getEngine();
                    anotherengine.setJavaScriptEnabled(true);
                    anotherengine.load("file:///" + artifact.getFilepath());
                    anotherwebview.setId("Iframe_" + resultCount);
                    anotherwebview.setZoom(zoomFactor);
                    anotherwebview.setPrefSize(webPrefWidth, webPrefHeight);
                    // if clicked set PreviewFrame to this src
                    anotherwebview.setOnMouseClicked(e -> {
                      previewEngine.load("file:///" + artifact.getFilepath());
                    });
                    
                    //just add the webview directly. Notice the multiplier is different
                    column=0;
                    row=currentProfileNumber*(2+webViewHeightInRows);
                    byImageCell.add(anotherwebview, column, row,webViewWidthInColumns,webViewHeightInRows);
                 
                    Set keySet = hintMap.keySet();// get the hints one by one and apply to cell
                    int keyCount = 0;
                    for (Object key : keySet) {
                        String k = (String) key;
                        Hint h = (Hint) hintMap.get(k);
                        String displaytype = h.getDisplaytype();
                        switch (displaytype) {
                            
                            case "range":
                                //make two sliders- one for each of the by profile/by image tabs
                                Slider slider = new Slider();
                                Slider slider2 = new Slider();
                                //tie them together
                                //tie it to the byProfilevalue
                                slider2.setOnMouseClicked(e->{
                                    slider.setValue(slider2.getValue());} );
                                slider.setOnMouseClicked(e->{
                                    slider2.setValue(slider.getValue());} );
                                
                                
                                //set up ythe slider for the profile tab
                                slider.setMax(h.getRangeMax());
                                slider.setMin(h.getRangeMin());
                                slider.setValue(Double.valueOf(h.getDefaultValue()));
                                slider.setId(h.getHintName() + "_" + resultCount);
                                Label label = new Label();
                                label.setId(h.getHintName() + "_" + resultCount);
                                label.setText(h.getDisplaytext());
                                if(h.getHintName().equalsIgnoreCase("globalscore"))
                                  {
                                    column=webViewWidthInColumns+1;
                                    row = (currentArtefactNumber)*(webViewHeightInRows+2);
                                    slider.setOrientation(Orientation.VERTICAL);
                                    slider.setShowTickMarks(true);
                                    slider.setMajorTickUnit(2.0);
                                    slider.setShowTickLabels(true);
                                    slider.autosize();
                                    byProfileCell.add(slider, column,row);
                                    label.setRotate(90);
                                    label.setAlignment(Pos.CENTER);
                                    byProfileCell.add(label, column+1, row);
                                  }
                                else
                                  {
                                    column = keyCount;
                                    row=1 + currentArtefactNumber*(2+webViewHeightInRows);
                                    slider.setPrefWidth(100);
                                    byProfileCell.add(label, column,row );
                                    byProfileCell.add(slider, column,row+1);
                                  }
                                
                                
                                
                                //set up the slider for the image tab
                                slider2.setMax(h.getRangeMax());
                                slider2.setMin(h.getRangeMin());
                                slider2.setValue(Double.valueOf(h.getDefaultValue()));
                                slider2.setId(h.getHintName() + "_" + resultCount);
                                Label label2 = new Label();
                                label2.setId(h.getHintName() + "_" + resultCount);
                                label2.setText(h.getDisplaytext());
                                if(h.getHintName().equalsIgnoreCase("globalscore"))
                                  {
                                    column=webViewWidthInColumns+1;
                                    row = currentProfileNumber*(webViewHeightInRows+2);
                                    slider2.setOrientation(Orientation.VERTICAL);
                                    slider2.setShowTickMarks(true);
                                    slider2.setMajorTickUnit(2.0);
                                    slider2.setShowTickLabels(true);
                                    slider2.autosize();
                                    byImageCell.add(slider2, column, row);
                                    label2.setRotate(90);
                                    label2.setAlignment(Pos.CENTER);
                                    byImageCell.add(label2, column+1, row);
                                  }
                                else
                                  {
                                    column = keyCount;
                                    slider2.setPrefWidth(100);
                                    row=1+ currentProfileNumber*(2+webViewHeightInRows);
                                    byImageCell.add(label2, column,row);
                                    byImageCell.add(slider2, column,(row+1));
                                  }
                                break;
                                
                                
                                
                            case "checkbox":
                                //make two checkboxes - one for each tab
                                CheckBox checkbox = new CheckBox();
                                CheckBox checkbox2 = new CheckBox();
                                //tie them together
                                checkbox.setOnMouseClicked(e->{ checkbox2.setSelected( checkbox.selectedProperty().getValue());});
                                checkbox2.setOnMouseClicked(e->{ checkbox.setSelected( checkbox2.selectedProperty().getValue());});
                                
                                //set up the box for the profile tab
                                checkbox.setId(h.getHintName() + "_" + resultCount);
                                Label label3 = new Label();
                                label3.setId(h.getHintName() + "_" + resultCount);
                                label3.setText(h.getDisplaytext());
                                column = keyCount;
                                row= 1 +currentArtefactNumber*(2+webViewHeightInRows);
                                byProfileCell.add(label3, column,row);
                                byProfileCell.add(checkbox, column,row+1);
                                
                                //set up the box for the imae tab
                                checkbox2.setId(h.getHintName() + "_" + resultCount);
                                Label label4 = new Label();
                                label4.setId(h.getHintName() + "_" + resultCount);
                                label4.setText(h.getDisplaytext());
                                column = keyCount;
                                row=1+ currentProfileNumber*(2+webViewHeightInRows);
                                byImageCell.add(label4, column,row);
                                byImageCell.add(checkbox2, column,row+1);
                                
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
                        getPane.add(byImageCell, 0, currentProfileNumber);
                        getPane.setGridLinesVisible(true);
                        tempByImageStore.put(nameOfArtefact, getPane);
                    } else {
                        GridPane newPane = new GridPane();
                        newPane.add(byImageCell, 0, currentProfileNumber);
                        tempByImageStore.put(nameOfArtefact, newPane);
                    }
                    
             
                    resultCount += 1;
               //Add the grid pane for this artefact within a profile to the gridpane for the profile
                byProfileGridPane.add(byProfileCell,0,currentArtefactNumber);
                }
    
            }
            //add grid pane to appropriate tab inside a scrollpane
            
            ScrollPane scrollPaneForTab = new ScrollPane();
            scrollPaneForTab.setContent(byProfileGridPane);
            profileNumberTab.setContent(scrollPaneForTab);
            byProfile.getTabs().add(profileNumberTab);
        
        }
        //now every profile has bee considered  add the byProfieTab to the HashMap returned by the dispaly
            byProfile.setId("byProfileTabPane");
            display.put("byProfile", byProfile);
            
            
            
        Set<String> keySet = tempByImageStore.keySet();
        Iterator<String> iterator = keySet.iterator();
        resultCount = 0;
//        Tab artifactNameTab;
        while (iterator.hasNext()) {
            String artefactName = iterator.next();
            Tab artifactNameTab = new Tab();
            artifactNameTab.setId("ByArtefact_" + resultCount);
            artifactNameTab.setText(artefactName);
            GridPane byImageCells = (GridPane) tempByImageStore.get(artefactName);
            ScrollPane scrollPaneForArtefact = new ScrollPane();
            scrollPaneForArtefact.setContent(byImageCells);
            artifactNameTab.setContent(scrollPaneForArtefact);
            byImage.getTabs().add(artifactNameTab);
            resultCount++;
        }
        

        
        
        byImage.setId("byImageTabPane");
        display.put("byImage", byImage);
        display.put("count", Integer.toString(artifacts.length));
        display.put("previewView", preview);
        return display;
    }
}
