/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Src;

import Algorithms.Hint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author kieran
 */
public class DesktopDisplay extends Display {

    @Override
    public HashMap<String, Object> loadDisplay(HashMap<String, Hint> hintMap, Artifact[] artifacts, int noOfProfiles) {

        HashMap<String, Object> display = new HashMap<>();
        HashMap<String, ArrayList<GridPane>> map = new HashMap<>();
        WebView preview = new WebView();
        int resultCount = 0;
        int webViewWidthInColumns=3, webViewHeightInRows=1;
        int column=0,row=0;
        double zoomFactor = 0.25;
        double webPrefWidth= 200, webPrefHeight=200;

        for (int currentProfileNumber = 0; currentProfileNumber < noOfProfiles; currentProfileNumber++) {
            int currentArtefactNumber=-1;
            for (Artifact artifact : artifacts) {
                String name = artifact.getFilename().substring(artifact.getFilename().indexOf("-") + 1);
                String[] parts = name.split("-");
                int profileNum = Integer.parseInt(parts[0].substring(parts[0].indexOf("_") + 1));
                if (profileNum == currentProfileNumber) {

                    currentArtefactNumber++;
                    
                    GridPane cell = new GridPane();
                    cell.setPadding(new Insets(10, 10, 10, 10));
                    cell.setVgap(8);
                    cell.setHgap(10);

                    WebView webview = new WebView();  // the thumbnail preview of the artefact in cell
                    webview.setMinSize(200, 200);
                    webview.setPrefSize(200, 200);
                    WebEngine engine = webview.getEngine();
                    engine.setJavaScriptEnabled(true);
                    engine.load("file:///" + artifact.getFilepath());
                    webview.setId("frame_" + resultCount);
                    // if clicked set PreviewFrame to this src
                    webview.setOnMouseClicked(e -> {
                        preview.getEngine().load("file:///" + artifact.getFilepath());
                    });
                    GridPane.setConstraints(webview, 0, 0);
                    cell.getChildren().add(webview);

                    Set<String> keySet = hintMap.keySet();// get the hints one by one and apply to cell
                    int keyCount = 0;
                    for (String key : keySet) {
                        Hint h = hintMap.get(key);
                        String displaytype = h.getDisplaytype();
                        switch (displaytype) {

                            case "range":
                                 Slider slider = new Slider();                                
                                
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
                                    cell.add(slider, column,row);
                                    label.setRotate(90);
                                    label.setAlignment(Pos.CENTER);
                                    cell.add(label, column+1, row);
                                  }
                                else
                                  {
                                    column = keyCount;
                                    row=1 + currentArtefactNumber*(2+webViewHeightInRows);
                                    slider.setPrefWidth(100);
                                    cell.add(label, column,row );
                                    cell.add(slider, column,row+1);
                                  }
                                
                                
                                break;


                            case "checkbox":
                                //make two checkboxes - one for each tab
                                CheckBox checkbox = new CheckBox();
                                
                                //set up the box for the profile tab
                                checkbox.setId(h.getHintName() + "_" + resultCount);
                                Label label2 = new Label();
                                label2.setId(h.getHintName() + "_" + resultCount);
                                label2.setText(h.getDisplaytext());
                                column = keyCount;
                                row= 1 +currentArtefactNumber*(2+webViewHeightInRows);
                                cell.add(label2, column,row);
                                cell.add(checkbox, column,row+1);
                                
                                break;

                            default:
                                throw new AssertionError();
                        }
                        keyCount++;
                    }
                    String nameOfArtefact = name.substring(name.indexOf("-") + 1);

                    if (map.containsKey(nameOfArtefact)) {
                        ArrayList<GridPane> get = map.get(nameOfArtefact);
                        get.add(cell);
                        map.put(nameOfArtefact, get);
                    } else {
                        ArrayList<GridPane> cellArray = new ArrayList<>();
                        cellArray.add(cell);
                        map.put(nameOfArtefact, cellArray);
                    }
                    resultCount++;
                }
            }
        }
        display.put("previewView", preview);
        display.put("results", map);
        display.put("count", Integer.toString(artifacts.length));
        return display;
    }
}
