/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Src;

import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author kieran
 */
public class Kernel {
    
	/** 
         * The name of the Kernel item which is likely to repeat
         */
	private String name= "";
	
	/** 
         * The hashmap containing the IpatVariable(s) which are likely to
         * repeat.
         */
	private HashMap<String, IpatVariable> variables;

	/**
	 * Instantiates a new ipat kernel.
	 *
	 * @param name the name
	 * @param variables the variables
	 */
	public Kernel(String name, HashMap<String, IpatVariable> variables){
		this.name = name;
		this.variables = variables;
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
	 * Gets the variables.
	 *
	 * @return the variables
	 */
	public HashMap<String, IpatVariable> getVariables() {
		return variables;
	}
        
        public void randomiseValues(){
            Collection<IpatVariable> values = this.variables.values();
            values.stream().forEach((SA) -> {
                SA.randomiseValues();
            });
        }
        
}
