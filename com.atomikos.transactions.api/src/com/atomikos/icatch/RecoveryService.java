
              
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
 
package com.atomikos.icatch;

/**
 * 
 * 
 * 
 * 
 *
 * A handle to the TM that resources can use to recover.
 */

public interface RecoveryService
{
	/**
	 * Ask the TM to recover.
	 * Resources should call this method whenever 
	 * recovery is needed. The earliest time at which
	 * resources can call this method is when
	 * <b>setRecoveryService</b> is called on the resource.
	 * Note: resources that are registered <b>before</b> the
	 * TM starts up will not need to call this method.
	 * On the other hand, resources that are registered
	 * when the transaction service is already running should use
	 * this method to trigger recovery. It is up to the implementation
	 * of the resource to determine whether this is desirable.
	 *
	 */
	
	public void recover();
	
	/**
	 * Get the transaction service's unique name.
	 * Resources can use this name to determine what resource 
	 * transactions need to be considered for recovery by this
	 * transaction service.
	 * 
	 * @return String The name of the TM.
	 */
	
	public String getName();
}
