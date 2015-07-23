/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Src;

import Algorithms.Hint;
import java.util.HashMap;

/**
 *
 * @author kieran
 */
public abstract class Display {
    
    
    public abstract HashMap<String, Object> loadDisplay(HashMap<String, Hint> hintMap, Artifact[] artifacts, int noOfProfiles);
}
