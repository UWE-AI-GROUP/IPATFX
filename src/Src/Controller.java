package Src;

/*
 Controller will be responsible for determining from the parameters how many times to call
 metaHeuristic interface in order to generate the next solution based on what kind of heuristic
 it uses.
 When user requests next generation the Controller may also call pKernel virtual display that implements 
 pKernel surrogate model instead of the real web or app several times between actual user interactions via
 the web/app.
 */
import Algorithms.CSSProcessor;
import Algorithms.ESEvolution;
import Algorithms.Hint;
import Algorithms.MetaHeuristic;
import Algorithms.Processor;
import Algorithms.UMLProcessor;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author kieran
 */
public class Controller {

    private static final Logger logger = Logger.getLogger(Controller.class);

    Processor processor;
    Display display = new DesktopDisplay();
    MetaHeuristic metaHeuristic = new ESEvolution();
    public HashMap<String, Hint> hints = new HashMap<>();

    /**
     * How many times a user has interacted during this session
     */
    public int iterationCount = 0;

    /**
     * the number of candidate solutions presented for evaluation
     */
    public int noOfProfiles = 6;

    /**
     * An array of the user supplied html files to process
     */
    public Artifact[] raw_artifacts;

    /**
     * results of applying profiles to raw artefacts : shown to user for
     * evaluation
     */
    public Artifact[] processedArtifacts;

    /**
     * the profiles being current evaluated and processed
     */
    public Profile[] currentGenerationOfProfiles;

    /**
     * the folder on the server disk which stores the uploaded artefacts
     */
    public static File inputFolder;

    /**
     * the folder on the server disk which stores the processed artefacts
     */
    public static File outputFolder;

    /**
     * the folder on the server disk holding the user-supplied initial candidate
     * solutions. naming Format : These should be placed within a sub directory
     * named "profiles", with the hint.xml file within this folder
     */
    public static File profileFolder;

    /**
     * the xml file which holds the designer supplied hints for the application
     */
    public static File hintsXML;

    /**
     *
     * @param inputFolder
     * @param outputFolder
     * @param profileFolder
     * @param hintsXML
     * @param caseName
     * @param display
     * @throws IOException
     */
    public Controller(File inputFolder, File outputFolder, File profileFolder, File hintsXML, String caseName) throws IOException {
        Controller.outputFolder = outputFolder;
        Controller.inputFolder = inputFolder;
        Controller.profileFolder = profileFolder;
        Controller.hintsXML = hintsXML;

        // ### Add additional problem cases below ###
        switch (caseName) {
            case "/UML Evolution":
                this.processor = new UMLProcessor();
                break;
            case "/CSS Evolution":
                this.processor = new CSSProcessor();
                break;

//            case "{/Folder Name}":    
//                 this.processor = new YourProcessor();
//                break;
            default:
                logger.info("Trouble with the instantiation of the Processor in Dispatchers doGet(). No case"
                        + " for this URL extension. Ensure case Strings in Dispatcher.doGet() exactly match those in web.xml");
                throw new AssertionError();
        }
    }

    /**
     * Generates the first set of results and returns them in the appropriate
     * display to the view
     *
     * @return
     */
    public HashMap<String, Object> initialisation() {
        bootstrapApplication();
        logger.info("initial no of Profiles = " + noOfProfiles);
        loadRawArtifacts();
        hints = loadHintsXML();
        metaHeuristic.updateWorkingMemory(currentGenerationOfProfiles);
        metaHeuristic.generateNextSolutions(noOfProfiles);
        for (int i = 0; i < noOfProfiles; i++) {
            currentGenerationOfProfiles[i] = metaHeuristic.getNextGenProfileAtIndex(i);
        }
        getResultArtifacts();
        HashMap<String, Object> results = display.loadDisplay(this.getHints(), this.processedArtifacts, this.noOfProfiles);
        return results;
    }

    /**
     * Generates the next generation of results and returns them to the view by
     * telling the metaheuristic to update its working memory, creates the next
     * generation, applies those profiles to the raw artifacts to get something
     * to display,
     *
     * @param scores
     * @param profileCount
     * @return
     */
    public HashMap<String, Object> mainloop(HashMap<String, Object> scores, int profileCount) {
        Interaction interaction = new Interaction();
        currentGenerationOfProfiles = interaction.updateProfileHints(scores, currentGenerationOfProfiles, hints);
        setNoOfProfiles(profileCount);
        metaHeuristic.updateWorkingMemory(currentGenerationOfProfiles);
        metaHeuristic.generateNextSolutions(noOfProfiles);
        for (int i = 0; i < noOfProfiles; i++) {
            currentGenerationOfProfiles[i] = metaHeuristic.getNextGenProfileAtIndex(i);
        }
        getResultArtifacts();
        HashMap<String, Object> results = display.loadDisplay(this.getHints(), this.processedArtifacts, this.noOfProfiles);
        return results;
    }

    /**
     * initialises the Profiles in memory from the files in the profile folder,
     * generating new ones if the count is <6
     * or if the count is >9 accepts only 6 of them
     */
    private void bootstrapApplication() {

        //set up Profiles filter to pick up all the files ending with .xml within Profile folder
        FilenameFilter filter = (File dir, String name) -> name.endsWith(".xml");
        //create an array holding all the files ending .xml in the profile folder to act as seeds
        File[] profiles_list = profileFolder.listFiles(filter);
        if (profiles_list == null) {
            logger.error("Error : profiles_list  == null in bootstrap application. Please check the web.xml in WEB-INF to ensure paths to config folders are correct.\n");
            logger.error("profile folder with path : " + profileFolder.getAbsolutePath());
            logger.error(" with " + profileFolder.listFiles().length + " files : \n");
            for (File profile : profileFolder.listFiles()) {
                logger.error(profile + "\n");
            }
        }
        //declare an array to hold the new profiles
        File[] new_profiles_list = new File[noOfProfiles];
        try {
            // if there are less than desired numner of seeds
            if (0 < profiles_list.length && profiles_list.length < noOfProfiles) {

                logger.debug("profiles_list.length = " + profiles_list.length + "\n");
                logger.debug("noOfProfiles = " + noOfProfiles + "\n");

                // fill new array with the seeds from profile folder
                System.arraycopy(profiles_list, 0, new_profiles_list, 0, profiles_list.length);

                int diffOfNrOfProfilesToMake = noOfProfiles - profiles_list.length;

                // fill the remaining space with copies of an abitrary Profile 
                for (int j = 0; j < diffOfNrOfProfilesToMake; j++) {
                    new_profiles_list[profiles_list.length + j] = profiles_list[0]; //changed 1 for -1
                }

                logger.info("Found only " + profiles_list.length + " profiles, randomly generated remaining " + diffOfNrOfProfilesToMake + "\n");

                //if there are more required than number of seeds but less than 9
            } else if (profiles_list.length > noOfProfiles && profiles_list.length < 9) {
                //just increase the number used and copy all the seeds
                noOfProfiles = profiles_list.length;
                new_profiles_list = new File[noOfProfiles];
                new_profiles_list = profiles_list;

                //we had just the right number - or more than 9    
            } else if (profiles_list.length > 9 || profiles_list.length == noOfProfiles) {
                System.arraycopy(profiles_list, 0, new_profiles_list, 0, noOfProfiles); // we have no Profile seeds or some error has occured
            } else {
                logger.error("There are no Profiles Seeds found. "
                        + "Please Check the profile folder path is correct and that Profile seeds exist within it.\n");
            }

        } catch (ArrayIndexOutOfBoundsException ex) {
            logger.fatal("The array out of bounds in Controller.bootstrapApplication() "
                    + " number of profiles desired: " + noOfProfiles + "\nNumber of profile seeds: " + profiles_list.length
                    + " profile list to be used as current generation: " + new_profiles_list.length + "\n" + ex.getLocalizedMessage());
        }

        //
        currentGenerationOfProfiles = new Profile[noOfProfiles];
        //for each one read the actual profile from the relevant file
        for (int i = 0; i < noOfProfiles; i++) {
            {
                currentGenerationOfProfiles[i] = new Profile(new_profiles_list[i]);
                // randomise the generated extra profiles
                if (i >= profiles_list.length) {
                    logger.debug("randomising generating profile [" + i + "]\n");
                    //create the new filename for this extra profile
                    File fileRename = new File(profileFolder + "/gen_0-profile_" + (i + 1) + ".xml");
                    logger.debug("fileRename of Profile to be generated : " + fileRename.getAbsolutePath());
                    //write the profile we are copying into this new file so that it exists on disk
                    currentGenerationOfProfiles[i].copyToNewFile(fileRename.getAbsolutePath());
                    //chnage the sotred values in memory
                    currentGenerationOfProfiles[i].setFile(fileRename);
                    currentGenerationOfProfiles[i].randomiseProfileVariableValues();
                    currentGenerationOfProfiles[i].randomiseKernelVariableValues();
                    //and writw back to disk for posterity
                    //currentGenerationOfProfiles[i].writeToFile();
                    currentGenerationOfProfiles[i].copyToNewFile(fileRename.getAbsolutePath());

                }
            }
        }
    }

    // populates the Artifacts in memory from the new_profiles_list found in the input folder
    private void loadRawArtifacts() {
        //create pKernel filter to pick put all the files ending .htm
        FilenameFilter filter = (File dir, String name) -> {
            //boolean bool = name.endsWith(".xml") || name.endsWith(".htm");
            boolean bool = name.endsWith(".htm");
            return bool;
        };
        //create an array of all the files in the inpurfolder using the filter        
        File[] file = inputFolder.listFiles(filter);
        //alocate space for arraty of artefacts - one for eacg file
        raw_artifacts = new Artifact[file.length];
        //read in each artefact from file and store in the array
        for (int i = 0; i < file.length; i++) {
            Artifact artifact = new Artifact(file[i]);
            raw_artifacts[i] = artifact;
        }
    }

    // Responsible for applying profiles to artifacts
    private void getResultArtifacts() {
        Profile currentProfile;
        Artifact rawArtifact;
        Artifact processedArtifact;
        processedArtifacts = new Artifact[raw_artifacts.length * noOfProfiles];
        int count = 0;

        // Generate CSS based on the new profiles
        for (int profileID = 0; profileID < noOfProfiles; profileID++) {
            currentProfile = currentGenerationOfProfiles[profileID];
            // TESTING : distinguishing the Raw artifact name from the processed one (raw artifact)
            logger.debug("Processing : " + currentProfile.getName());

            // Process the profile to generate CSS
            for (Artifact raw_artifact : raw_artifacts) {
                rawArtifact = raw_artifact;
                //System.out.println(rawArtifact.getFilename());
                processedArtifact = processor.applyProfileToArtifact(currentProfile, rawArtifact, outputFolder.getAbsolutePath() + "/");
                processedArtifacts[count] = processedArtifact;
                count++;
            }
        }
    }

    public HashMap<String, Hint> loadHintsXML() {

        HashMap<String, Hint> hintMap = new HashMap<>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(hintsXML);
            // doc.getDocumentElement().normalize();
            NodeList interactionList = doc.getElementsByTagName("interaction");

            for (int i = 0; i < interactionList.getLength(); i++) {
                Node item = interactionList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element interaction = (Element) interactionList.item(i);
                    Hint hint = new Hint();
                    NodeList elements = interaction.getChildNodes();
                    for (int j = 0; j < elements.getLength(); j++) {
                        Node attribute = elements.item(j);
                        if (attribute.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) elements.item(j);
                            String att = element.getTagName();
                            String value = element.getTextContent();
                            if (value != null) {
                                value = value.trim().replaceAll("\\s+", " ");
                                switch (att) {
                                    case "name":
                                        hint.setHintName(value);
                                        break;
                                    case "displaytype":
                                        hint.setDisplaytype(value);
                                        break;
                                    case "displayText":
                                        hint.setDisplaytext(value);
                                        break;
                                    case "default":
                                        hint.setDefaultValue(value);
                                        break;
                                    case "range-min":
                                        hint.setRangeMin(Double.parseDouble(value));
                                        break;
                                    case "range-max":
                                        hint.setRangeMax(Double.parseDouble(value));
                                        break;
                                    case "KernelAffected":
                                        hint.AddAffectedKernel(value);
                                        break;
                                    case "KernelVariableAffected":
                                        hint.addAffectedKernelVariable(value);
                                        break;
                                    case "ProfileVariablesAffected":
                                        hint.addAffectedProfileVariable(value);
                                        break;
                                    case "Effect":
                                        hint.setEffect(value);
                                        break;
                                    default:

                                        logger.error("Error with Hint [ " + i + " ] = Tag: " + att + " / Value: " + value + " - Check hints.xml for incorrect Tag names");
                                        throw new AssertionError();
                                }
                            }
                        }
                    }
                    hintMap.put(hint.getHintName(), hint);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            logger.fatal(e.getMessage());
        }
        return hintMap;
    }

    public void setNoOfProfiles(int newNoOfProfiles) {

        Profile[] newCurrentGenerationOfProfiles = new Profile[newNoOfProfiles];
        for (int i = 0; i < newNoOfProfiles; i++) {
            if (i < this.noOfProfiles) {
                newCurrentGenerationOfProfiles[i] = currentGenerationOfProfiles[i];
            }
            // randomise the generated extra profiles
            if (i >= this.noOfProfiles) {
                newCurrentGenerationOfProfiles[i] = currentGenerationOfProfiles[0];
                logger.debug("randomising generated profile [" + i + "]\n");
                //create the new filename for this extra profile
                String fileRename = profileFolder + "/gen_0-profile_" + (i + 1) + ".xml";
                //write the profile we are copying into this new file so that it exists on disk
                newCurrentGenerationOfProfiles[i].copyToNewFile(fileRename);
                //chnage the sotred values in memory
                newCurrentGenerationOfProfiles[i].setFile(new File(fileRename));
                newCurrentGenerationOfProfiles[i].randomiseProfileVariableValues();
                newCurrentGenerationOfProfiles[i].randomiseKernelVariableValues();
                newCurrentGenerationOfProfiles[i].setGlobalScore(0);
                //and writw back to disk for posterity
                newCurrentGenerationOfProfiles[i].writeToFile();
                newCurrentGenerationOfProfiles[i].copyToNewFile(fileRename);

            }
        }
        this.noOfProfiles = newNoOfProfiles;
        this.currentGenerationOfProfiles = newCurrentGenerationOfProfiles;
        metaHeuristic.updateWorkingMemory(currentGenerationOfProfiles);
        System.out.println("How Many in Controller.setNoOfProfiles after : " + this.noOfProfiles);
    }

    public HashMap<String, Hint> getHints() {
        return this.hints;
    }
}
