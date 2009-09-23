
              
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
 
package com.atomikos.diagnostics;

import java.io.IOException;

/**
 *
 *
 *A cascaded message console for system output warnings.
 *
 */

public class  CascadedConsole implements Console
{
    private Console first_ , last_ ;
    
    /**
    *Construct a new instance.
    *
    *@param first The console where messages are displayed first.
    *@param last The console where messages are cascaded 
    *after display on first console.
    */
    
    public CascadedConsole ( Console first , Console last )
    {
        first_ = first;
        last_ = last;
    }
    
    /**
     *Print a message to the output of the console(s).
     *This message is cascaded internally.
     *
     *@param string The message to output.
     *@param java.io.IOException On failure.
     */

    public void println(String string) throws java.io.IOException
    {
        first_.println ( string );
        last_.println ( string );	
    }
    
     /**
     *Print a message to the output of the console(s).
     *This message is cascaded internally.
     *
     *@param string The message to output.
     *@param java.io.IOException On failure.
     */

    public void print(String string) throws java.io.IOException
    {
        first_.print ( string );
        last_.print ( string );	
    }
    
    /**
     *Closes the underlying consoles.
     */
     
    public void close() throws java.io.IOException
    {
        first_.close();
        last_.close();	
    }

  
    public void println(String string, int level) throws IOException
    {
		first_.println ( string , level );
		last_.println ( string , level );
        
    }

 
    public void print(String string, int level) throws IOException
    {
        first_.print( string , level );
        last_.print ( string , level );
        
    }

    
    public void setLevel(int level)
    {
        first_.setLevel ( level );
        last_.setLevel ( level );
        
    }

    /**
     * @see com.atomikos.diagnostics.Console#getLevel()
     */
    public int getLevel()
    {
        return first_.getLevel();
    }

    
}
