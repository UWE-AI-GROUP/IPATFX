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
import org.apache.log4j.Logger;

/**
 *
 * @author kieran
 */
public class Interaction {

    private static final Logger logger = Logger.getLogger(Interaction.class);

    /**
     *
     * @param data
     * @param currentGenerationOfProfiles
     * @param hints
     * @param controller
     * @return
     */
    public Profile[] updateProfileHints(HashMap<String, Object> data, Profile[] currentGenerationOfProfiles, HashMap<String, Hint> hints) {

        data.keySet().stream().forEach((hint) -> {
            Object value = data.get(hint);
            System.out.println("hint " + hint + " / value " + value);
        });

        int numOfProfiles = currentGenerationOfProfiles.length;
        int numOfHints = hints.size();
        HashMap<String, HashMap<Integer, Double>> averageCounters = new HashMap<>();
        HashMap<String, HashMap<Integer, ?>> ordered = new HashMap<>();
        Hint hintProc;

        // We don't know the order in which hints are initialised in hints.xml so organisation of return values is required
        // Run through the different hints (keys) in the data set
        logger.debug("Num of Profiles =" + numOfProfiles);
        logger.debug("Num of Hints =" + numOfHints);
        logger.debug("Data Size =" + data.size());

        Set<String> hintSet = data.keySet();
        int numOfResults = data.size() / numOfHints;
        logger.debug("numOfResults =" + numOfResults);
        int numOfUploads = numOfResults / numOfProfiles;
        logger.debug("numOfUploads =" + numOfUploads);
        for (Object keySet1 : hintSet) {

            String hintName = (String) keySet1;
            String[] hint_Iteration = hintName.split("_");
            // name of the hint in question
            String hint = hint_Iteration[0];
            int iteration = Integer.parseInt(hint_Iteration[1]);
            // which array position to add the different results to
            System.out.println("iteration " + iteration + " number of uploads " + numOfUploads);
            int profileNum = iteration / numOfUploads;

            Object rawValue = data.get(hintName);

            // print statements to ensure that the cells value are placed into the right array and positions for averaging
            // System.out.println("=================");
            //  System.out.println("Iteration: " + iteration + "\nHint: " + hint + "\nValue: " + data.get(key) + "\nArray Postition: " + profileNum + "\n");
            // if the ordered list hasn't yet initialised this profile's hint's averages map then create it and add the value as its first entry
            if (!ordered.containsKey(hint)) {

                // determine the data type, add it to the appropriate hashmap which is then added to "ordered"
                if (rawValue instanceof Boolean) {
                    HashMap<Integer, Boolean> profilesBooleanHintAverages = new HashMap<>();
                    Boolean value = (Boolean) rawValue;
                    profilesBooleanHintAverages.put(profileNum, value);
                    ordered.put(hint, profilesBooleanHintAverages);
                    //    System.out.println("Created Boolean value (first input)");
                }

                if (rawValue instanceof String) {
                    HashMap<Integer, Double> averageCountMap = new HashMap<>();
                    HashMap<Integer, Double> profilesDoubleHintAverages = new HashMap<>();
                    profilesDoubleHintAverages.put(profileNum, Double.parseDouble((String) rawValue));
                    averageCountMap.put(profileNum, 1.0);
                    ordered.put(hint, profilesDoubleHintAverages);
                    averageCounters.put(hint, averageCountMap);
                    //    System.out.println("Added new hint [" + hint + "] averaged value for profile [" + profileNum + "] with value [" + ((String) rawValue) + "]");
                }

                // else add the value to the existing profile's hint's averages map, 
            } else {

                if (rawValue instanceof Boolean) {
                    @SuppressWarnings("unchecked")
                    HashMap<Integer, Boolean> PBHA = (HashMap<Integer, Boolean>) ordered.get(hint);
                    if (!PBHA.containsKey(profileNum)) {
                        PBHA.put(profileNum, ((Boolean) rawValue));
                        ordered.put(hint, PBHA);
                    } else {
                        if (!PBHA.get(profileNum) && (Boolean) rawValue) {
                            PBHA.put(profileNum, true);
                            ordered.put(hint, PBHA);
                        }
                    }

                    // if its a string or other
                } else {

                    // get the averageMap for this hint
                    @SuppressWarnings("unchecked")
                    HashMap<Integer, Double> PDHA =  (HashMap<Integer, Double>) ordered.get(hint);
                    
                    

                    // check if the profile we are adding a value to already has a value, if it does add to its average
                    if (PDHA.containsKey(profileNum)) {
                        //if (PDHA.get(profileNum) != null) {
                        Double runningAverage = PDHA.get(profileNum);
                        HashMap<Integer, Double> averageCount = averageCounters.get(hint);
                        Double currentCount = averageCount.get(profileNum);

                    //    System.out.println("runningAverage " + runningAverage);
                        //   System.out.println("currentCount " + currentCount);
                        //   System.out.println("rawValue " + rawValue);
                        Double av = (runningAverage * currentCount + Double.parseDouble((String) rawValue)) / (currentCount + 1);
                        PDHA.put(profileNum, av);
                        //  System.out.println("Updated hint [" + hint + "] in profilesDoubleHintAverages at [" + profileNum + "] from value [" + runningAverage + "] to value [" + av + "] as the [" + (currentCount + 1) + "] entry");
                        averageCount.put(profileNum, currentCount + 1);
                        averageCounters.put(hint, averageCount);
                        ordered.put(hint, PDHA);

                        // if it doesnt, create the first entry
                    } else {
                        HashMap<Integer, Double> averageCount = averageCounters.get(hint);
                        averageCount.put(profileNum, 1.0);
                        PDHA.put(profileNum, Double.parseDouble((String) rawValue));
                        averageCounters.put(hint, averageCount);
                        ordered.put(hint, PDHA);
                    }
                }
            }
        }

        // for each profile
        for (int i = 0; i < numOfProfiles; i++) {

            logger.debug("Updating hints for Profile: " + i + "\n");

            // run through the hints getting each averageMap
            Set<String> hint = ordered.keySet();
            Iterator<String> iterator = hint.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                HashMap<Integer, ?> profilesHintAverages = ordered.get(key);

                if (key.equalsIgnoreCase("globalScore")) {
                    Object value = profilesHintAverages.get(i);
                    Double intValue = (Double) value;
                    currentGenerationOfProfiles[i].setGlobalScore(intValue.intValue());
                    logger.info("Updated " + key + " : " + intValue.intValue());

                } else {

                    // apply the average value of the current profile to the profile
                    Object value = profilesHintAverages.get(i);

                    if (value instanceof Boolean) {
                        Boolean booleanValue = (Boolean) value;
                        hintProc = hints.get(key);
                        if (booleanValue) {
                            logger.info("Updated " + key + " : true");
                            currentGenerationOfProfiles[i] = hintProc.InterpretHintInProfile(currentGenerationOfProfiles[i], 0.0);
                        } else {
                            logger.info("Updated " + key + " : false");
                            currentGenerationOfProfiles[i] = hintProc.InterpretHintInProfile(currentGenerationOfProfiles[i], 1.0);
                        }
                    } else {
                        Double doubleValue = (Double) value;
                        hintProc = hints.get(key);
                        logger.info("Updated " + key + " : " + doubleValue);
                        currentGenerationOfProfiles[i] = hintProc.InterpretHintInProfile(currentGenerationOfProfiles[i], doubleValue);
                    }
                }
            }
        }
        return currentGenerationOfProfiles;
    }
}
