package Algorithms;

import Src.Kernel;
import Src.Profile;
import Src.IpatVariable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * A Hint is an optional interaction provided to the user which produces an
 * application specific bias in the search space of the problem.
 *
 * Instances of this class are loaded at runtime from Controller.loadHintsXML()
 * which extracts them from an external file where they are stored as a HashMap
 * to be read from upon the generation of results in the Display interface.
 *
 */
public class Hint {

    private static final Logger logger = Logger.getLogger(Hint.class);

    private final ArrayList<String> profileVariablesAffected;
    private final ArrayList<String> kernelsAffected;
    private final ArrayList<String> kernelVariablesAffected;
    private String hintName;
    private String displaytype;
    private String displaytext;
    private double rangeMin;
    private double rangeMax;
    private String defaultValue;
    private String effect;

    /**
     * Empty constructor which initialises the final ArrayLists of -
     * profileVariablesAffected - kernelsAffected - kernelVariablesAffected and
     * sets the hintName to null.
     */
    public Hint() {
        profileVariablesAffected = new ArrayList<>();
        kernelsAffected = new ArrayList<>();
        kernelVariablesAffected = new ArrayList<>();
        hintName = null;
    }

    /**
     * Overloaded constructor which initialises the final ArrayLists of -
     * profileVariablesAffected - kernelsAffected - kernelVariablesAffected and
     * sets the hintName to the one provided as a parameter
     *
     * @param name
     */
    public Hint(String name) {
        profileVariablesAffected = new ArrayList<>();
        kernelsAffected = new ArrayList<>();
        kernelVariablesAffected = new ArrayList<>();
        hintName = name;
    }

    /**
     * A fully loaded constructor which also checks to ensure that the min and
     * max range values haven't been incorrectly set the wrong way around.
     *
     * @param theName
     * @param theDisplaytype
     * @param theDisplaytext
     * @param theRangeMin
     * @param theRangeMax
     * @param theDefaultVal
     * @param theEffect
     */
    public Hint(String theName, String theDisplaytype, String theDisplaytext, double theRangeMin, double theRangeMax, String theDefaultVal, String theEffect) {
        profileVariablesAffected = new ArrayList<>();
        kernelsAffected = new ArrayList<>();
        kernelVariablesAffected = new ArrayList<>();
        hintName = theName;
        displaytype = theDisplaytype;
        displaytext = theDisplaytext;
        if (theRangeMax < theRangeMin) {
            rangeMax = theRangeMin;
            rangeMin = theRangeMax;
        } else {
            rangeMin = theRangeMin;
            rangeMax = theRangeMax;
        }
        defaultValue = theDefaultVal;
        effect = theEffect;
    }

    /**
     * adds the name of a kernel affected by this hint.
     *
     * @param kernelName
     */
    public void AddAffectedKernel(String kernelName) {
        kernelsAffected.add(kernelName);
    }

    /**
     *
     * @return ArrayList of all kernels affected by this Hint.
     */
    public ArrayList<String> getKernelsAffected() {
        return kernelsAffected;
    }

    /**
     *
     * @return name of the hint used in memory as a reference
     */
    public String getHintName() {
        return hintName;
    }

    /**
     * Sets the hintName, used primarily when loading hints into memory from
     * file
     *
     * @param hintName
     */
    public void setHintName(String hintName) {
        this.hintName = hintName;
    }

    /**
     * adds the name of a Profile level variable affected by this hint.
     *
     * @param newVarName
     */
    public void addAffectedProfileVariable(String newVarName) {
        profileVariablesAffected.add(newVarName);
    }

    /**
     * gets the names of Profile level variables affected by this hint.
     *
     * @return ArrayList of profile level variables names
     */
    public ArrayList<String> getProfileVariablesAffected() {
        return profileVariablesAffected;
    }

    /**
     *
     * @param newVarName
     */
    public void addAffectedKernelVariable(String newVarName) {
        kernelVariablesAffected.add(newVarName);
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getKernelVariablesAffected() {
        return kernelVariablesAffected;
    }

    /**
     *
     * @return
     */
    public String getDisplaytype() {
        return displaytype;
    }

    /**
     *
     * @param displaytype
     */
    public void setDisplaytype(String displaytype) {
        this.displaytype = displaytype;
    }

    /**
     *
     * @return
     */
    public String getDisplaytext() {
        return displaytext;
    }

    /**
     *
     * @param displaytext
     */
    public void setDisplaytext(String displaytext) {
        this.displaytext = displaytext;
    }

    /**
     *
     * @return
     */
    public double getRangeMin() {
        return rangeMin;
    }

    /**
     *
     * @param rangeMin
     */
    public void setRangeMin(double rangeMin) {
        this.rangeMin = rangeMin;
    }

    /**
     *
     * @return
     */
    public double getRangeMax() {
        return rangeMax;
    }

    /**
     *
     * @param rangeMax
     */
    public void setRangeMax(double rangeMax) {
        this.rangeMax = rangeMax;
    }

    /**
     *
     * @return
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     *
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     *
     * @return
     */
    public String getEffect() {
        return effect;
    }

    /**
     *
     * @param effect
     */
    public void setEffect(String effect) {
        this.effect = effect;
    }

    /**
     * Evaluates using the effect attribute of the hint which form of processing
     * is required for the "amount" value supplied as a parameter to the given
     * candidate solution.
     *
     * @param toChange - The candidate solution for which the hints were applied
     * to.
     * @param amount - The numerical multiplier which determines the degree to
     * which the value is changed
     * @return
     */
    public Profile InterpretHintInProfile(Profile toChange, double amount) {
        Profile thisProfile = toChange;

        switch (effect.toLowerCase()) {
            case "setrateofevolutionequalzero":
                if ((amount != 1.0) && (amount != 0.0)) {
                    logger.error("wrong value for amount with setRateOfEvolutionEqualZero hint should be 0 or 1\n");
                } else {
                    thisProfile = InterpretSetRateOfEvolutionEqualZeroHintInProfile(toChange, amount);
                }
                break;
            case "moderatebyvalue":
                thisProfile = InterpretModeratingHintInProfile(toChange, amount);
                break;
            case "toggle":
                thisProfile = InterpretToggleHintInProfile(toChange, amount);
                break;
            case "setnewvalue":
                thisProfile = InterpretSetNewValueInProfile(toChange, amount);
                break;
            default:
                thisProfile = toChange;//no other types implemented yet
        }

        //finally write the changed values to file
        String profilePath = thisProfile.getFile().getPath();
        String profileNameAndPath = profilePath + thisProfile.getName();
        thisProfile.copyToNewFile(profileNameAndPath);

        //and then return
        return thisProfile;
    }

    /**
     *
     * @param toChange
     * @param amount
     * @return
     */
    public Profile InterpretSetNewValueInProfile(Profile toChange, double amount) {
        Profile thisProfile = toChange;
        IpatVariable currentVariable;
        HashMap<String, IpatVariable> profileLevelVars = thisProfile.getProfileLevelVariables();
        String currentVarName;

        assert ((amount >= rangeMin) && (amount <= rangeMax));

        logger.debug("in InterpretModeratingHintInProfile(), range min = " + rangeMin + "rangeMax = " + rangeMax + "amount = " + amount +"\n");   
        //start off with the profile variables that are affected
        for (Iterator<String> profileVariableIterator = profileVariablesAffected.iterator(); profileVariableIterator.hasNext();) {
            currentVarName =  profileVariableIterator.next();
            //get the variable from the local copy in the hashtable
            currentVariable = profileLevelVars.get(currentVarName);
            if (currentVariable == null) {
                logger.error("error - trying to change  variable " + currentVarName + " which does not exist in profile\n");
            } else {
                //reset the value in thecopy of the variable
                currentVariable.setValue(amount);
                //write back in the changed variable
                thisProfile.replaceVariable(currentVariable);
            }
        }
        //then for each of the affected kernels
        for (String kernelname : kernelsAffected) {
            Kernel kernel = thisProfile.getKernelCalled(kernelname);
            if (kernel == null) {
                logger.error("Kernel " + kernelname + " not present in profile " + thisProfile.getName() + " within Hint\n");
            } else {
                HashMap<String, IpatVariable> vars = kernel.getVariables();
                Iterator<String> kvarIterator;
                // if we don't have a list of which kernel variables to change use all
                if (kernelVariablesAffected.isEmpty()) {//boring conversion because Profile uses hashmaps and enumerators
                    ArrayList<String> allKernelVariables = new ArrayList<>();
                    Set<String> keySet = vars.keySet();
                    Iterator<String> kVarNames = keySet.iterator();
                    while (kVarNames.hasNext()) {
                        allKernelVariables.add(kVarNames.next());
                    }
                    kvarIterator = allKernelVariables.iterator();
                } else // otherwise, if we have some specified, just use them
                {
                    kvarIterator = kernelVariablesAffected.iterator();
                }

                while (kvarIterator.hasNext()) {
                    currentVarName =  kvarIterator.next();
                    currentVariable = vars.get(currentVarName);
                    //reset the value in thecopy of the variable
                    currentVariable.setValue(amount);
                    vars.put(currentVarName, currentVariable);
                }
                Kernel changedKernel = new Kernel(kernel.getName(), vars);
                //finally need to write this new kernel back to the profile in the  nextGen arraylist
                //delete the old one the add the new one
                //hisProfile.replaceKernel(kernel.getName());
                thisProfile.replaceKernel(changedKernel);
            } //end of code dealing with affected kernels
        }

        return thisProfile;
    }

    /**
     * method that interprets the hints provided by the user and makes
     * appropriate application-specific changes ot the profile variables
     *
     * @param toChange
     * @param amount
     * @return changed profile
     */
    public Profile InterpretSetRateOfEvolutionEqualZeroHintInProfile(Profile toChange, double amount) {
        Profile thisProfile = toChange;
        IpatVariable currentVariable;
        HashMap<String, IpatVariable> profileLevelVars = thisProfile.getProfileLevelVariables();
        String currentVarName;

        //first freeze all of the profile level variables
        for (Iterator<String> profileVariableIterator = profileVariablesAffected.iterator(); profileVariableIterator.hasNext();) {
            currentVarName = profileVariableIterator.next();
            //get the variable from the local copy in the hashtable
            currentVariable = profileLevelVars.get(currentVarName);
            //set the rate of evoltion to zero so mutation has no effect
            currentVariable.setRateOfEvolution(amount);
            //write back in the changed variable
            thisProfile.replaceVariable(currentVariable);
        }

        //then for each of the affected kernels
        for (String kernelname : kernelsAffected) {
            Kernel kernel = thisProfile.getKernelCalled(kernelname);
            if (kernel == null) {
                logger.error("Kernel " + kernelname + " not present in profile " + thisProfile.getName() + " within Hint\n");
            } else {
                HashMap<String, IpatVariable> vars = kernel.getVariables();
                Iterator<String> kvarIterator;
                // if we don't have a list of which kernel variables to change use all
                if (kernelVariablesAffected.isEmpty()) {//boring conversion because Profile uses hashmaps and enumerators
                    ArrayList<String> allKernelVariables = new ArrayList<>();
                    Set<String> keySet = vars.keySet();
                    Iterator<String> kVarNames = keySet.iterator();
                    while (kVarNames.hasNext()) {
                        allKernelVariables.add(kVarNames.next());
                    }
                    kvarIterator = allKernelVariables.iterator();
                } else // otherwise, if we have some specified, just use them
                {
                    kvarIterator = kernelVariablesAffected.iterator();
                }

                while (kvarIterator.hasNext()) {
                    currentVarName =  kvarIterator.next();
                    currentVariable =  vars.get(currentVarName);
                    //set the rate of evoltion to zero so mutation has no effect
                    currentVariable.setRateOfEvolution(amount);
                    vars.put(currentVarName, currentVariable);
                }
                Kernel changedKernel = new Kernel(kernel.getName(), vars);
                //finally need to write this new kernel back to the profile in the  nextGen arraylist
                //delete the old one the add the new one
                // thisProfile.removeKernel(kernel.getName());
                thisProfile.replaceKernel(changedKernel);
            } //end of code dealing with affected kernels
        }

        // all hints have been dealt with - we can exit
        return thisProfile;
    }

    /**
     * method that interprets the hints provided by the user and makes
     * appropriate application-specific changes ot the profile variables
     *
     * @param toChange
     * @param amount
     * @return changed profile
     */
    public Profile InterpretModeratingHintInProfile(Profile toChange, double amount) {
        Profile thisProfile = toChange;
        IpatVariable currentVariable;
        HashMap<String, IpatVariable> profileLevelVars = thisProfile.getProfileLevelVariables();
        String currentVarName;

        assert ((amount >= rangeMin) && (amount <= rangeMax));
        double range = rangeMax - rangeMin;
        double midpoint = rangeMin + 0.5 * range;
        amount = (amount - midpoint);
        double multiplier = Math.pow(2.0, amount);

        logger.debug("in InterpretModeratingHintInProfile(), range min = " + rangeMin + "rangeMax = " + rangeMax + "amount = " + amount + "multiplier is " + multiplier + "\n");   
        //start off with the profile variables that are affected
        for (Iterator<String> profileVariableIterator = profileVariablesAffected.iterator(); profileVariableIterator.hasNext();) {
            currentVarName =  profileVariableIterator.next();
            //get the variable from the local copy in the hashtable
            currentVariable = profileLevelVars.get(currentVarName);
            // TODO check for currentVariable !null
            //set the rate of evoltion to zero so mutation has no effect
            double oldValue = currentVariable.getValue();

            //calculate raw new value
            double newValue = oldValue * multiplier;
            //take account of granularity
            newValue = newValue - Math.IEEEremainder(newValue, currentVariable.getGranularity());
            //truncate to range
            newValue = Math.max(currentVariable.getLbound(), newValue);
            newValue = Math.min(currentVariable.getUbound(), newValue);

            //reset the value in thecopy of the variable
            currentVariable.setValue(newValue);
            //write back in the changed variable
            thisProfile.replaceVariable(currentVariable);
        }
        //then for each of the affected kernels
        for (String kernelname : kernelsAffected) {
            Kernel kernel = thisProfile.getKernelCalled(kernelname);
            if (kernel == null) {
                logger.error("Kernel " + kernelname + " not present in profile " + thisProfile.getName() + " within Hint\n");
            } else {
                HashMap<String, IpatVariable> vars = kernel.getVariables();
                Iterator<String> kvarIterator;
                // if we don't have a list of which kernel variables to change use all
                if (kernelVariablesAffected.isEmpty()) {//boring conversion because Profile uses hashmaps and enumerators
                    ArrayList<String> allKernelVariables = new ArrayList<>();
                    Set<String> keySet = vars.keySet();
                    Iterator<String> kVarNames = keySet.iterator();
                    while (kVarNames.hasNext()) {
                        allKernelVariables.add(kVarNames.next());
                    }
                    kvarIterator = allKernelVariables.iterator();
                } else // otherwise, if we have some specified, just use them
                {
                    kvarIterator = kernelVariablesAffected.iterator();
                }

                while (kvarIterator.hasNext()) {
                    currentVarName = kvarIterator.next();
                    currentVariable =  vars.get(currentVarName);
                    if(currentVariable==null)
                        logger.error("couldn't find variable with name " + currentVarName + " in kernel " + kernelname + "\n");
                    else
                    {double oldValue = currentVariable.getValue();
                    
                    //calculate raw new value
                    double newValue = oldValue * multiplier;
                    //take account of granularity
                    newValue = newValue - Math.IEEEremainder(newValue, currentVariable.getGranularity());
                    //truncate to range
                    newValue = Math.max(currentVariable.getLbound(), newValue);
                    newValue = Math.min(currentVariable.getUbound(), newValue);
                    
                    //reset the value in thecopy of the variable
                    currentVariable.setValue(newValue);
                    vars.put(currentVarName, currentVariable);
                    }
                }
                Kernel changedKernel = new Kernel(kernel.getName(), vars);
                //finally need to write this new kernel back to the profile in the  nextGen arraylist
                //delete the old one the add the new one
                //thisProfile.removeKernel(kernel.getName());
                thisProfile.replaceKernel(changedKernel);
            } //end of code dealing with affected kernels
        }

        return thisProfile;
    }

    /**
     * method that interprets the hints provided by the user and makes
     * appropriate application-specific changes ot the profile variables
     *
     * @param toChange
     * @param amount
     * @return changed profile
     */
    public Profile InterpretToggleHintInProfile(Profile toChange, double amount) {
        Profile thisProfile = toChange;
        IpatVariable currentVariable;
        HashMap<String, IpatVariable> profileLevelVars = thisProfile.getProfileLevelVariables();
        String currentVarName;
        double newValue;

        //start off with the profile variables that are affected
        for (Iterator<String> profileVariableIterator = profileVariablesAffected.iterator(); profileVariableIterator.hasNext();) {
            currentVarName = profileVariableIterator.next();
            //get the variable from the local copy in the hashtable
            currentVariable = profileLevelVars.get(currentVarName);

            if (currentVariable == null) {
                logger.error("trying to change profile variable " + currentVarName + " but it doesnt exist in the profile\n");
            }

            //toggle the value of the variables
            if (amount == rangeMin) {
                newValue = currentVariable.getLbound();
            } else if (amount == rangeMax) {
                newValue = currentVariable.getUbound();
            } else {
                newValue = currentVariable.getValue();
            }
            //reset the value in thecopy of the variable
            currentVariable.setValue(newValue); 
            //write back in the changed variable
            thisProfile.replaceVariable(currentVariable);
        }
        //then for each of the affected kernels
        for (String kernelname : kernelsAffected) {
            Kernel kernel =  thisProfile.getKernelCalled(kernelname);
            
            if (kernel == null) {
                logger.error("Kernel " + kernelname + " not present in profile " + thisProfile.getName() + " within Hint\n");
            } else {
                HashMap<String, IpatVariable> vars = kernel.getVariables();
                Iterator<String> kvarIterator;
                // if we don't have a list of which kernel variables to change use all
                if (kernelVariablesAffected.isEmpty()) {//boring conversion because Profile uses hashmaps and enumerators
                    ArrayList<String> allKernelVariables = new ArrayList<>();
                    Set<String> keySet = vars.keySet();
                    Iterator<String> kVarNames = keySet.iterator();
                    while (kVarNames.hasNext()) {
                        allKernelVariables.add(kVarNames.next());
                    }
                    kvarIterator = allKernelVariables.iterator();
                } else // otherwise, if we have some specified, just use them
                {
                    kvarIterator = kernelVariablesAffected.iterator();
                }
                
                while (kvarIterator.hasNext()) {
                    currentVarName = kvarIterator.next();
                    currentVariable = vars.get(currentVarName);
                    //toggle the value of the variables
                    if (amount == rangeMin) {
                        newValue = currentVariable.getLbound();
                    } else if (amount == rangeMax) {
                        newValue = currentVariable.getUbound();
                    } else {
                        newValue = currentVariable.getValue();
                    }
                    
                    //reset the value in thecopy of the variable
                    currentVariable.setValue(newValue);
                    vars.put(currentVarName, currentVariable);
                }
                Kernel changedKernel = new Kernel(kernel.getName(), vars);
                //finally need to write this new kernel back to the profile in the  nextGen arraylist
                //delete the old one the add the new one
                //thisProfile.removeKernel(kernel.getName());
                thisProfile.replaceKernel(changedKernel);
            } //end of code dealing with affected kernels
        }

        return thisProfile;
    }

}
