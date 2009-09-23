
              
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
 
package com.atomikos.icatch.jta;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * 
 * 
 * 
 * Instances of this class can be used to lookup resource transactions in a
 * hashtable that uses the XAResource instance for mapping. This is needed
 * because otherwise the JTA wouldn't work with XAResource implementations that
 * have overridden equals.
 * 
 * 
 * 
 * 
 */
class XAResourceKey
{

    private XAResource xares;

    public XAResourceKey ( XAResource xares )
    {
        super ();
        this.xares = xares;
    }

    public boolean equals ( Object o )
    {
        // System.out.println ( "EQUALS OF XAResourceKey");
        boolean ret = false;
        if ( o instanceof XAResourceKey ) {
            XAResourceKey other = (XAResourceKey) o;
            // ret = other.xares.toString().equals ( xares.toString() );
            try {
                ret = (other.xares == xares || other.xares.isSameRM ( xares ));
            } catch ( XAException e ) {
                // just return false
            }
            // System.out.println ( "COMPARING XARESOURCES: " + xares + " and "
            // + other.xares );
            // System.out.println ( "RESULT OF COMPARISON: " + ret );
        }

        return ret;
    }

    public int hashCode ()
    {
        // System.out.println ( "hashCode OF XAResourceKey: returning " +
        // xares.toString().hashCode());
        return xares.getClass ().getName ().toString ().hashCode ();
    }

    public String toString ()
    {
        return xares.toString ();
    }

}
