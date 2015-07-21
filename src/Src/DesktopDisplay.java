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
    public HashMap<String, Object> loadDisplay(HashMap hintMap, Artifact[] artifacts, int noOfProfiles) {

        HashMap<String, Object> display = new HashMap();
        HashMap<String, ArrayList<GridPane>> map = new HashMap();
        WebView preview = new WebView();
        int resultCount = 0;

        for (int i = 0; i < noOfProfiles; i++) {
            for (Artifact artifact : artifacts) {
                String name = artifact.getFilename().substring(artifact.getFilename().indexOf("-") + 1);
                String[] parts = name.split("-");
                int profileNum = Integer.parseInt(parts[0].substring(parts[0].indexOf("_") + 1));
                if (profileNum == i) {

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
                                cell.getChildren().add(label);
                                cell.getChildren().add(slider);
                                break;

                            case "checkbox":
                                CheckBox checkbox = new CheckBox();
                                checkbox.setId(h.getHintName() + "_" + resultCount);
                                Label label3 = new Label();
                                label3.setId(h.getHintName() + "_" + resultCount);
                                label3.setText(h.getDisplaytext());
                                GridPane.setConstraints(label3, keyCount, 1);
                                GridPane.setConstraints(checkbox, keyCount, 2);
                                cell.getChildren().add(label3);
                                cell.getChildren().add(checkbox);
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
                        ArrayList<GridPane> cellArray = new ArrayList();
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
