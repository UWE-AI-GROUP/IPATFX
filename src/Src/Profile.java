/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Src;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

//TODO JavaDoc

/**
 *
 * @author kieran
 */
public class Profile {

    private static final Logger logger = Logger.getLogger(Profile.class);

    /**
     * The file.
     */
    private File file;

    /**
     * The global score.
     */
    private int globalScore = 5;

    /**
     * The kernels.
     */
    private HashMap<String, Kernel> kernels;

    /**
     * The name.
     */
    private String name;

    /**
     * The no of kernels.
     */
    private int noOfKernels;

    /**
     * The no of kernel profileLevelVariables.
     */
    private int noOfKernerlVariables;

    /**
     * The no of profile profileLevelVariables.
     */
    private int noOfProfileVariables;

    /**
     * The profileLevelVariables.
     */
    private HashMap<String, IpatVariable> profileLevelVariables;

    /**
     * Instantiates a new ipat profile.
     *
     * @param file the file
     */
    public Profile(File file) {
        profileLevelVariables = new HashMap<>();
        kernels = new HashMap<>();
        this.file = file;
        this.name = file.getName();
        
        try {
            Document XmlDoc = new SAXBuilder().build(file);
            //Element root = XmlDoc.getRootElement();
            Element profileNode = XmlDoc.getRootElement();//= root.getChild("profile", root.getNamespace());
            Iterator<?> iterator = profileNode.getChildren().iterator();
            while (iterator.hasNext()) {
                Element hint = (Element) iterator.next();
                if (hint.getName().equalsIgnoreCase("variable")) {
                    String Hintname = hint.getChildText("name");
                    String type = hint.getChildText("type");
                    String temp = hint.getChildText("lbound");
                    Double dub = new Double(temp);
                    double lbound = dub;
                    temp = hint.getChildText("ubound");
                    dub = new Double(temp);
                    double ubound = dub;
                    temp = hint.getChildText("granularity");
                    dub = new Double(temp);
                    double granularity = dub;
                    temp = hint.getChildText("rateOfEvolution");
                    dub = new Double(temp);
                    double rateOfEvolution = dub;
                    temp = hint.getChildText("value");
                    dub = new Double(temp);
                    double value = dub;
                    String dfault = hint.getChildText("default");
                    String flag = hint.getChildText("flag");
                    String unit = hint.getChildText("unit");
                    IpatVariable variable = new IpatVariable(Hintname, type, lbound, ubound, granularity, rateOfEvolution, value, dfault, flag, unit);
                    profileLevelVariables.put(variable.getName(), variable);
                } else if (hint.getName().equalsIgnoreCase("kernel")) {
                    List<?> children1 = hint.getChildren();
                    Iterator<?> it = children1.iterator();
                    Element nm = (Element) it.next();
                    String kernelName = nm.getText();
                    HashMap<String, IpatVariable> vars = new HashMap<>();
                    while (it.hasNext()) {
                        Element hintt = (Element) it.next();
                        String hintname = hintt.getChildText("name");
                        String type = hintt.getChildText("type");
                        String temp = hintt.getChildText("lbound");
                        Double dub = new Double(temp);
                        double lbound = dub;
                        temp = hintt.getChildText("ubound");
                        dub = new Double(temp);
                        double ubound = dub;
                        temp = hintt.getChildText("granularity");
                        dub = new Double(temp);
                        double granularity = dub;
                        temp = hintt.getChildText("rateOfEvolution");
                        dub = new Double(temp);
                        double rateOfEvolution = dub;
                        temp = hintt.getChildText("value");
                        dub = new Double(temp);
                        double value = dub;
                        String dfault = hintt.getChildText("default");
                        String flag = hintt.getChildText("flag");
                        String unit = hintt.getChildText("unit");
                        IpatVariable variable = new IpatVariable(hintname, type, lbound, ubound, granularity, rateOfEvolution, value, dfault, flag, unit);
                        vars.put(hintname, variable);
                    }
                    Kernel kernel = new Kernel(kernelName, vars);
                    kernels.put(kernel.getName(), kernel);
                } else if (hint.getName().equalsIgnoreCase("interaction")) {
                }
            }
        } catch (JDOMException | IOException | NumberFormatException pce) {
            logger.error(pce +" In Profile Constructor");
        }
    }

    public void randomiseProfileVariableValues() {
        Collection<IpatVariable> collection = this.profileLevelVariables.values();
        collection.stream().forEach((SA) -> {
            SA.randomiseValues();
            // logger.debug("new value for " + this.name + " PROFILE VARIABLE " + SA.getName() + " = " + SA.getValue() + "\n");
        });
    }

   
    public void randomiseKernelVariableValues() {
        Collection<Kernel> collection = this.kernels.values();
        collection.stream().map((kernel) -> {
            kernel.randomiseValues();
            return kernel;
        }).map((kernel) -> {
            logger.debug("new values for " + this.name + " KERNEL VARIABLE " + kernel.getName() + ":\n");
            return kernel;
        }).map((kernel) -> kernel.getVariables()).map((variables) -> variables.values()).map((KernelCollection) -> {
            KernelCollection.stream().forEach((SA) -> {
                logger.debug(SA.getName() + " = " + SA.getValue());
            });
            return KernelCollection;
        }).forEach((_item) -> {
            logger.debug("\n----------------------------------------------------------------------------\n");
        });
    }

    /**
     * Adds the kernel.
     *
     * @param kernel the kernel
     */
    public void addNewKernel(Kernel kernel) {
        Kernel oldvalue = kernels.put(kernel.getName(), kernel);
         if (oldvalue != null) {
            logger.error("Error adding kernel " + kernel.getName() + " already exists in Profile");
        }
    }

    public void replaceKernel(Kernel kernel) {
        Kernel oldvalue =  kernels.put(kernel.getName(), kernel);
        if (oldvalue == null) {
            logger.error("Error replacing kernel " + kernel.getName() + "in profile " + this.getName() + " Not previously present in profile");
        }
    }

    /**
     * Adds the variable.
     *
     * @param kernelName string
     * @return Kernel with the name kernelName if that key-value pair exists in
     * thisProfile.kernels else null
     */
    public Kernel getKernelCalled(String kernelName) {
        Kernel found = null;
        found = kernels.get(kernelName);
        return found;
    }

       public void addNewVariable(IpatVariable var) {
        IpatVariable oldvalue =  profileLevelVariables.put(var.getName(), var);
         if (oldvalue != null) {
            logger.error("Error adding Profile Variable " + var.getName() + " already exists in Profile");
        }
    }
    
    public void replaceVariable(IpatVariable var) {
        IpatVariable oldval = profileLevelVariables.put(var.getName(), var);
        if (oldval == null) {
            logger.error("error replacing profile variable " + var.getName() + " in profile " + this.getName() + " old value not found or null");
        }
    }

    /**
     * Gets the file.
     *
     * @return the file associated with this profile
     */
    public File getFile() {
        return file;
    }

    public void setFile(File thisfile) {
        file = thisfile;
    }

    /**
     * Gets the global score.
     *
     * @return the global score
     */
    public int getGlobalScore() {
        return globalScore;
    }

    /**
     * Gets the kernels.
     *
     * @return the kernels
     */
    public HashMap<String, Kernel> getKernels() {
        return kernels;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the profileLevelVariables.
     *
     * @return the profileLevelVariables
     */
    public HashMap<String, IpatVariable> getProfileLevelVariables() {
        return profileLevelVariables;
    }

    /**
     * Prints the profile.
     */
    public void printProfile() {
        Set<String> keys = profileLevelVariables.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IpatVariable var = profileLevelVariables.get(key);
            logger.info(var.getName() + " : " + var.getValue());
        }
        Set<String> keySet = kernels.keySet();
        Iterator<String> kernelIterator = keySet.iterator();
        while (kernelIterator.hasNext()) {
            String kernelName = kernelIterator.next();
            logger.debug(kernelName);
            Kernel kernel = kernels.get(kernelName);
            HashMap<String, IpatVariable> kVars = kernel.getVariables();
            Set<String> kVarsKeys = kVars.keySet();
            Iterator<String> kVarsKeysIterator = kVarsKeys.iterator();
            while (kVarsKeysIterator.hasNext()) {
                IpatVariable var = kVars.get(kVarsKeysIterator.next());
                logger.debug("   " + var.getName() + " : "
                        + var.getValue());
            }
        }
    }

    /**
     * Sets the global score.
     *
     * @param globalScore the new global score
     */
    public void setGlobalScore(int globalScore) {
        this.globalScore = globalScore;
    }

    /**
     * Convert string to document.
     *
     * @param string the string
     * @return the document
     */
    public Document convertStringToDocument(String string) {
        try {
            StringReader stringReader = new StringReader(string);
            return new SAXBuilder().build(stringReader);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the profile.
     *
     * @param profile the profile
     * @return true, if successful
     */
    public boolean writeToFile() {

        try {

            Document XmlDoc = new SAXBuilder().build(this.getFile());
            //Element root = XmlDoc.getRootElement();
            Element profileNode = XmlDoc.getRootElement();//root.getChild("profile", root.getNamespace());
            List<?> children = profileNode.getChildren();
            Iterator<?> iterator = children.iterator();
            HashMap<String, IpatVariable> mySolutionAttributes = this.getProfileLevelVariables();
            HashMap<String, Kernel> kernelStore = this.getKernels();

            while (iterator.hasNext()) {

                Element hint = (Element) iterator.next();

                if (hint.getName().equalsIgnoreCase("variable")) {

                    //  System.out.println("\n Profile variable \n");
                    Element elem = hint.getChild("name");
                    IpatVariable var = mySolutionAttributes.get(elem.getValue());
                    elem.setText(var.getName());

                    elem = hint.getChild("type");
                    //  System.out.println("var.getType() = " + var.getType());
                    elem.setText(var.getType());

                    elem = hint.getChild("lbound");
                    Double dub = var.getLbound(); // changed from new Double
                    //  System.out.println("var.getLbound() = " + var.getLbound());
                    elem.setText(dub.toString());

                    elem = hint.getChild("ubound");
                    dub = var.getUbound();
                    // System.out.println("var.getUbound() = " + var.getUbound());
                    elem.setText(dub.toString());

                    elem = hint.getChild("granularity");
                    dub = var.getGranularity();
                    // System.out.println("var.getGranularity() = " + var.getGranularity());
                    elem.setText(dub.toString());

                    elem = hint.getChild("rateOfEvolution");
                    dub = var.getRateOfEvolution();
                    //  System.out.println("var.getRateOfEvolution() = " + var.getRateOfEvolution());
                    elem.setText(dub.toString());

                    elem = hint.getChild("value");
                    dub = var.getValue();
                    //  System.out.println("var.getValue() = " + var.getValue());
                    elem.setText(dub.toString());

                    elem = hint.getChild("default");
                    //   System.out.println("var.getDfault() = " + var.getDfault());
                    elem.setText(var.getDfault());

                    elem = hint.getChild("flag");
                    //  System.out.println("var.getFlag() = " + var.getFlag());
                    elem.setText(var.getFlag());

                    elem = hint.getChild("unit");
                    //  System.out.println("var.getUnit() = " + var.getUnit());
                    elem.setText(var.getUnit());

                } else if (hint.getName().equalsIgnoreCase("kernel")) {
                    List<?> children1 = hint.getChildren();
                    Iterator<?> it = children1.iterator();
                    Element nm = (Element) it.next();
                    String kernelName = nm.getText();
                    Kernel kern = kernelStore.get(kernelName);
                    HashMap<String, IpatVariable> vars = kern.getVariables();
                    Collection<IpatVariable> coll = vars.values();
                    Iterator<IpatVariable> enu3 = coll.iterator();

                    while (it.hasNext()) {
                        Element hintt = (Element) it.next();
                        IpatVariable varb = enu3.next();

                        Element elem = hintt.getChild("name");
                        elem.setText(varb.getName());

                        elem = hintt.getChild("type");
                        elem.setText(varb.getType());

                        elem = hintt.getChild("lbound");
                        Double dub = varb.getLbound();
                        elem.setText(dub.toString());

                        elem = hintt.getChild("ubound");
                        dub = varb.getUbound();
                        elem.setText(dub.toString());

                        elem = hintt.getChild("granularity");
                        dub = varb.getGranularity();
                        elem.setText(dub.toString());

                        elem = hintt.getChild("rateOfEvolution");
                        dub = varb.getRateOfEvolution();
                        elem.setText(dub.toString());

                        elem = hintt.getChild("value");
                        dub = varb.getValue();
                        elem.setText(dub.toString());

                        elem = hintt.getChild("default");
                        elem.setText(varb.getDfault());

                        elem = hintt.getChild("flag");
                        elem.setText(varb.getFlag());

                        elem = hintt.getChild("unit");
                        elem.setText(varb.getUnit());
                    }
                }
                else if (hint.getName().equalsIgnoreCase("globalscore")) 
                  {
                    Element elem = hint.getChild("value");
                    elem.setText("0");
                  }
            }

            //System.out.println("[SetProfile] Writing score " + 0 + " to profile: " + profile.getFile().toString());
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            String xmlString = outputter.outputString(XmlDoc);
             System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println(this.getFile().getAbsolutePath());
             System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
            try( BufferedWriter writer = new BufferedWriter(new FileWriter(this.getFile().getAbsolutePath()))) {
                writer.write(xmlString);
            }

        } catch (JDOMException | IOException pce) {
            System.out.println(pce.getMessage());
        }
        return true;
    }


    // used for if the Profile is to have a hard copy of itself in a specified location on disk
    public boolean copyToNewFile(String outputPath) {

        /* apply changes to solution attribute values in memory to their hard copy file before 
         copying that file to the location specified as "outputPath"
         */
        this.writeToFile();

        String copy;
        File fileCopy = this.getFile();
        BufferedWriter writer;
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            Document XmlDoc = new SAXBuilder().build(fileCopy);
            copy = outputter.outputString(XmlDoc);
            writer = new BufferedWriter(new FileWriter(outputPath));
            writer.write(copy);
            writer.close();
        } catch (JDOMException | IOException pce) {
           logger.error(Arrays.toString(pce.getStackTrace()) + " in Profile");
        }
        return true; // copy;
    }
}
