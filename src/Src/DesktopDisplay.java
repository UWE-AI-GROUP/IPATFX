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

/**
 *
 * @author kieran
 */
public class DesktopDisplay extends Display {
  
    
     @Override
     public HashMap<String, String> loadDisplay(Controller controller) {
        HashMap hintMap = controller.getHints();
        Artifact[] artifacts = controller.processedArtifacts;
        HashMap<String, String> byImageArray = new HashMap<>();
        HashMap<String, String> HM = new HashMap();
        int resultCount = 0;
        String hintString = "";
        
        String headerString = "<html><header>";
        String scriptString = "<script language=javascript>function loadscores() "
                    + "   { "
                      + " app.scores[1] = \"added by js function\" ;"  
//                    + "    var scores = {};"
                    + "    var all = document.getElementsByClassName(\"cell\");"
                    + "    for (var i = 0; i < all.length; i++) "
                    + "       {" 
                     + "         var inputs = all[i].getElementsByTagName(\"input\");"
                    + "        for (var j = 0; j < inputs.length; j++) "
                    + "           {"
                    + "             var name = inputs[j].getAttribute(\"id\");"
                    + "              app.inputnames[inputs.length*i +j] = name;"
                    + "             if (inputs[j].getAttribute(\"type\") === \"checkbox\")"
                    + "                {"
                    + "                  var value = inputs[j].checked;"
                    + "                   app.inputvalues[inputs.length*i +j] = value;"
                    + "                }"
                    + "             else if (inputs[j].getAttribute(\"type\") === \"range\")"
                    + "                {"
                    + "                  var value = inputs[j].value;"
                    + "                  app.inputvalues[inputs.length*i +j] = value;"
                    + "                 }"
                    + "             else"
                    + "              {  "
                    + "                alert(\"Error: A javascript JQuery check needs to be implemented for \" + $(inputs[i]).attr('id') + \" in javascript.js\");"
                    + "              }"
                    + "          }"
                    + "      }"
                    + "  return \"somestring\" }"
                    + "</script>";
        String htmlBodyStart = "<body>";
        String htmlBodyEnd = "</body></html>";
        
        String webPageAsString;
        
        
        String cells = "<div id='tabs-container'><ul class='tabs-menu'>";
        for (int i = 0; i < controller.noOfProfiles; i++) {
            cells += "<li  id='li_" + i + "' onclick='tabClicked(this.id)'><a class='tabText_" + i + "' href='#byProfile_" + i + "'>" + i + "</a></li>";
        }
        cells += " </ul> <div class='tabstuff'>";
        String cell = "";
        for (int i = 0; i < controller.noOfProfiles; i++) {
            cells += "<div id='byProfile_" + i + "' class='tab-content'>";
            for (Artifact artifact : artifacts) {
                cell = "";
                String name = artifact.getFilename().substring(artifact.getFilename().indexOf("-") + 1);
                String[] parts = name.split("-");
                int profileNum = Integer.parseInt(parts[0].substring(parts[0].indexOf("_") + 1));
                if (profileNum == i) {
                    cell = "<div class='cell'>" + "<div id='overlay_" + resultCount + "' class='overlay' onclick='app.preview(document.getElementById('frame_'+"+resultCount+").src)'></div>" + "<iframe src='file:///" + artifact.getFilepath() + "' scrolling='no' class='cellFrames' id='frame_" + resultCount + "' ></iframe>";
                    Set keySet = hintMap.keySet();
                    for (Object key : keySet) {
                        String k = (String) key;
                        Hint h = (Hint) hintMap.get(k);
                        String displaytype = h.getDisplaytype();
                        switch (displaytype) {
                            case "range":
                                cell += "<div class='hint'><input type='range' class='hintScore' id ='" + h.getHintName() + "_" + resultCount + "' min='" + h.getRangeMin() + "' max='" + h.getRangeMax() + "' value='" + h.getDefaultValue() + "' step='1'/><label for='" + h.getHintName() + "_" + resultCount + "' class='label'>" + h.getDisplaytext() + "</label></div>";
                                hintString += h.getHintName() + "_" + resultCount + ",";
                                break;
                            case "checkbox":
                                cell += "<div class='hint'><input type='checkbox' id='" + h.getHintName() + "_" + resultCount + "' class='hintScore' ><label for='" + h.getHintName() + "_" + resultCount + "' class='label'>" + h.getDisplaytext() + "</label></div>";
                                hintString += h.getHintName() + "_" + resultCount + ",";
                                break;
                            default:
                                throw new AssertionError();
                        }
                    }
                    cell += "</div>";
                    resultCount += 1;
                    String key = name.substring(name.indexOf("-") + 1);
                    if (byImageArray.containsKey(key)) {
                        String get = byImageArray.get(key);
                        get += cell;
                        byImageArray.put(key, get);
                    } else {
                        byImageArray.put(key, cell);
                    }
                }
                cells += cell;
            }
            cells += "</div>";
        }
        cells += "</div>";
        webPageAsString = headerString + scriptString + htmlBodyStart + cells + htmlBodyEnd;
        HM.put("byProfile", webPageAsString);
        cells = "<div id='tabs-container'><ul class='tabs-menu'>";
        Set<String> keySet = byImageArray.keySet();
        Iterator<String> iterator = keySet.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            String next = iterator.next();
            cells += "<li  id='li_" + count + "' onclick='tabClicked(this.id)'><a class='tabText_" + count + "' href='#byImage_" + count + "'>" + next + "</a></li>";
            count++;
        }
        cells += " </ul> <div class='tabstuff'>";
        iterator = keySet.iterator();
        count = 0;
        while (iterator.hasNext()) {
            cells += "<div id='byImage_" + count + "' class='tab-content'>";
            String get = byImageArray.get(iterator.next());
            cells += get + "</div>";
            count++;
        }
        cells += "</div>";
        webPageAsString = headerString + scriptString + htmlBodyStart + cells + htmlBodyEnd;
        HM.put("byImage", webPageAsString);
        HM.put("hintString", hintString);
        HM.put("count", Integer.toString(artifacts.length));
        return HM;
    }
    
     
     
}
