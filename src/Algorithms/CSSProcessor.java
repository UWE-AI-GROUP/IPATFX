/*
 * 
 */
package Algorithms;

import Src.Artifact;
import Src.IpatVariable;
import Src.Kernel;
import Src.Profile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

//TODO add javadoc
/**
 * The Class CSSProcessor processes profiles to generate next generation HTML,
 * CSS and PNG files.
 */
public final class CSSProcessor implements Processor {
private static final Logger logger = Logger.getLogger(CSSProcessor.class);
    private HashMap<String, ArrayList<String>> cssLabels;

    /**
     *
     */
    public CSSProcessor() {
        this.cssLabels = setupCSSLabelStore();
    }

    /*
     Profile - the profile being applied to artifact
     Artifact - the raw artifact to be processed
     index - the identifier for the profile
     */

    /**
     *
     * @param profile
     * @param artifact
     * @param outputFolder
     * @return
     */
    
    @Override
    public Artifact applyProfileToArtifact(Profile profile, Artifact artifact, String outputFolder) {

        HashMap<String, Kernel> kernels = profile.getKernels();
        if (kernels == null) {
            logger.error("Error: applyProfileToArtifcat in CSSProcessor. No kernels present in Profile.");
        }

        // ----------- CSS Formatters -------------------------------//
        String CSS_Start_Braces = "{";
        String CSS_End_Braces = "}";
        String CSS_PropSeparator = ":";
        String CSS_PropPairSeparator = ";";

        // ------------ CSS Generation ------------------------------//
        String css = "";
        HashMap<String, IpatVariable> pv = profile.getProfileLevelVariables();
        if (pv == null) {
            logger.error("Error: applyProfileToArtifcat in CSSProcessor. No solution attributes in Profile.");
        }
        Set<String> keySet = pv.keySet();
        Iterator<String> iterator = keySet.iterator();
        String csspLine = "body{";
        int colorCheck = 0;
        int red = 0;
        int blue = 0;
        int green = 0;
        while (iterator.hasNext()) {
            String vkey = iterator.next().toString();
            IpatVariable ipvar = pv.get(vkey);

            if (ipvar.getName().contains("Page")) {
                colorCheck++;
                if (ipvar.getName().contains("Red")) {
                    Double dd = ipvar.getValue();
                    red = dd.intValue();
                }
                if (ipvar.getName().contains("Blue")) {
                    Double dd = ipvar.getValue();
                    blue = dd.intValue();
                }
                if (ipvar.getName().contains("Green")) {
                    Double dd = ipvar.getValue();
                    green = dd.intValue();
                }
                if (colorCheck >= 3) {
                    csspLine += "background-color:rgb(" + red + "," + blue + "," + green + ");";
                }
            }
        }

        csspLine += CSS_End_Braces + "\n";
        css += csspLine;
        Set<String> keySet1 = kernels.keySet();
        Iterator<String> kernelsEnuTemp = keySet1.iterator();
        String[] tempArray = new String[kernels.size()];
        int k = 3;
        while (kernelsEnuTemp.hasNext()) {
            String kernelName = kernelsEnuTemp.next().toString();
            if (kernelName.equalsIgnoreCase("h1")) {
                tempArray[0] = kernelName;
            } else if (kernelName.equalsIgnoreCase("h2")) {
                tempArray[1] = kernelName;
            } else if (kernelName.equalsIgnoreCase("p")) {
                tempArray[2] = kernelName;
            } else {
                tempArray[k] = kernelName;
                k++;
            }
        }
        ArrayList<String> tempArray2 = new ArrayList<String>();
        for (int n = 0; n < tempArray.length; n++) {
            tempArray2.add(n, tempArray[n]);
        }
        double CSS_lastfontsize = 72.0;

        Iterator<String> kernelsEnu = tempArray2.iterator();
        while (kernelsEnu.hasNext()) {
            String cssLine = "";
            String ktype = kernelsEnu.next();
            Kernel kernel1 = kernels.get(ktype);
            cssLine += kernel1.getName() + CSS_Start_Braces;
            HashMap<String, IpatVariable> vars = kernel1.getVariables();
            Set<String> keySet2 = vars.keySet();

            Iterator<String> evars = keySet2.iterator();
            colorCheck = 0;
            red = 0;
            blue = 0;
            green = 0;
            while (evars.hasNext()) {
                String vkey = evars.next().toString();
                IpatVariable ipvar = vars.get(vkey);

                if (ipvar.getName().contains("color")) {
                    colorCheck++;
                    if (ipvar.getName().contains("red")) {
                        Double dd = ipvar.getValue();
                        red = dd.intValue();
                    }
                    if (ipvar.getName().contains("blue")) {
                        Double dd = ipvar.getValue();
                        blue = dd.intValue();
                    }
                    if (ipvar.getName().contains("green")) {
                        Double dd = ipvar.getValue();
                        green = dd.intValue();
                    }
                    if (colorCheck >= 3) {
                        cssLine += "color:rgb(" + red + "," + blue + "," + green + ");";
                    }
                } else {
                    if (ipvar.getType().equalsIgnoreCase("cardinal")) {
                        Double val = ipvar.getValue();
                        ArrayList<String> values = cssLabels.get(ipvar.getName());
                        String value = values.get(val.intValue());
                        cssLine += ipvar.getName() + CSS_PropSeparator + value
                                + CSS_PropPairSeparator;

                    } else if (ipvar.getType().equalsIgnoreCase("ordinal")) {
                        Double val = ipvar.getValue();

                        if (ktype.equalsIgnoreCase("h1")) {
                            val = CSS_lastfontsize * ((val / 100));
                            CSS_lastfontsize = val;

                        } else if (ktype.equalsIgnoreCase("h2") || ktype.equalsIgnoreCase("p")) {
                            val = CSS_lastfontsize * 0.5 * (1.0 + (val) / 100);
                            CSS_lastfontsize = val;
                        }

                        cssLine += ipvar.getName() + CSS_PropSeparator + val.intValue()
                                + ipvar.getUnit() + CSS_PropPairSeparator;
                    } else if (ipvar.getType().equalsIgnoreCase("boolean")) {
                        if (ipvar.getValue() == 1.0) {
                            cssLine += "font-style" + CSS_PropSeparator + ipvar.getName() + CSS_PropPairSeparator;
                        }
                    }
                }
            }
            cssLine += CSS_End_Braces + "\n";
            css += cssLine;
        }
        String CSS = css;

        // ---------- Filenames Generation------------//
        String outHtmlPath;
        String processedArtifactName;
        String profileName = profile.getName();
        // just want the name of the profile without the .xml extension
        profileName = profileName.substring(0, profileName.lastIndexOf('.'));

        try {
            String rawArtifactName = artifact.getFilename();
            rawArtifactName = rawArtifactName.substring(0, rawArtifactName.lastIndexOf('.'));
             // TESTING : distinguishing the raw artifact name from the processed one (processed one)
            //  logger.debug("Raw artifact name = " + rawArtifactName + " : profilename = " + profileName);
            processedArtifactName = profileName + "-" + rawArtifactName + ".html";
            // logger.debug("Processed artifact name = " + processedArtifactName);
            outHtmlPath = outputFolder + processedArtifactName;
            String htmlFile = "";
            BufferedReader reader = new BufferedReader(new FileReader(artifact.getFile().getAbsolutePath()));
            String temp;
            while ((temp = reader.readLine()) != null) {
                htmlFile += temp + "\n";
                if (temp.contains("<head>")) {
                    htmlFile += "<style type=\"text/css\">";
                    htmlFile += CSS;
                    htmlFile += "</style>";
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outHtmlPath))) {
                writer.write(htmlFile);
            }

            return new Artifact(new File(outHtmlPath));
        } catch (Exception e) {
          logger.fatal("fatal error in CSSProcessor " + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    /**
     *
     * @return
     */
    public HashMap<String, ArrayList<String>> setupCSSLabelStore() {
        // Cardinal variables store

        // The fontfamilies.
        String[] fontfamilies
                = {"Arial Black", "Calibiri", "Helvetica", "Courier", "Times",
                    "sans-serif", "Console", "Tahoma", "Century Gothic",
                    "Palatino", "Cambria"};

        //The floatvals.
        String[] floatvals
                = {"left", "right", "top", "bottom", "center"};

        //The margin.
        String[] margin
                = {"0px 0px 10px 10px", "10px 10px 0px 0px", "0px 10px 10px 0px",
                    "10px 0px 0px 10px", "10px 0px 10px 0px"};

        HashMap<String, ArrayList<String>> cssStore = new HashMap<>();

        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(Arrays.asList(fontfamilies));
        cssStore.put("font-family", temp);

        temp = new ArrayList<>();
        temp.addAll(Arrays.asList(floatvals));
        cssStore.put("float", temp);

        temp = new ArrayList<>();
        temp.addAll(Arrays.asList(margin));
        cssStore.put("margin", temp);

        return cssStore;
    }

}