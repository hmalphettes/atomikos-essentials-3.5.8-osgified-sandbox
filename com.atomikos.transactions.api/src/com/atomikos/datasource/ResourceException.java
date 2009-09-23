
              
/*
 * Copyright 2000-2008, Atomikos (http://www.atomikos.com) 
 *
 * This code ("Atomikos TransactionsEssentials"), by itself, 
 * is being distributed under the 
 * Apache License, Version 2.0 ("License"), a copy of which may be found at 
 * http://www.atomikos.com/licenses/apache-license-2.0.txt . 
 * You may not use this file except in compliance with the License. 
 *             
 * While the License grants certain patent license rights, 
 * those patent license rights only extend to the use of 
 * Atomikos TransactionsEssentials by itself. 
 *             
 * This code (Atomikos TransactionsEssentials) contains certain interfaces 
 * in package (namespace) com.atomikos.icatch
 * (including com.atomikos.icatch.Participant) which, if implemented, may
 * infringe one or more patents held by Atomikos.  
 * It should be appreciated that you may NOT implement such interfaces; 
 * licensing to implement these interfaces must be obtained separately from Atomikos.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
 
package com.atomikos.datasource;

import java.util.Enumeration;
import java.util.Stack;

/**
*
*
*<p>
*Exception on the level of the resource manager.
*Contains more detailed info of actual protocol's exception.
*/

public class ResourceException extends com.atomikos.icatch.SysException{
    //private Stack errors_=null;
    
    public ResourceException(String msg){
        super(msg);
        
    }
    
    public ResourceException(String msg, Stack errors)
    {
        //this (msg);
//        errors_=(Stack) errors.clone();
        super ( msg , errors );
    }
    
    public ResourceException(Stack errors){
        
        super ( "ResourceException" , errors );
    }
    
    //TODO rename to getErrors (overrides SysException)
    //TODO add method printToConsole
    public Stack getDetails(){
      return getErrors();
    }
    
    public void printStackTrace()
    {
    	super.printStackTrace();
    	if ( ! ( getDetails() == null || getDetails().empty())) {
    		Enumeration enumm = getDetails().elements();
    		while ( enumm.hasMoreElements() ) {
    			Exception next = ( Exception ) enumm.nextElement();
    			next.printStackTrace();
    		}
    	}
    }
}

