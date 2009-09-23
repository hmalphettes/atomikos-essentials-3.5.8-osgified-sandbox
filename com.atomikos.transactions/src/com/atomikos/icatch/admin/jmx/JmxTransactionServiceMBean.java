
              
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
 
package com.atomikos.icatch.admin.jmx;

import javax.management.ObjectName;

/**
 * 
 * 
 * 
 * 
 * 
 * An MBean interface for the administration of the transaction service.
 */

public interface JmxTransactionServiceMBean
{
    /**
     * Get the pending transactions from the Transaction Service.
     * 
     * @return ObjectName[] An array of all MBean object names (one MBean is
     *         created for each transaction).
     */

    public ObjectName[] getTransactions ();

    /**
     * Sets whether ONLY heuristic transactions should be returned.
     * Default is false.
     * 
     * @param heuristicsOnly
     */
    
    public void setHeuristicsOnly ( boolean heuristicsOnly );
    
    /**
     * Gets the heuristic mode.
     * 
     * @return
     */
    public boolean getHeuristicsOnly();
}
