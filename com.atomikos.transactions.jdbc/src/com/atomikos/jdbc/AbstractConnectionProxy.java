
              
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
 
package com.atomikos.jdbc;

import java.lang.reflect.InvocationHandler;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.atomikos.icatch.system.Configuration;
import com.atomikos.util.DynamicProxy;

/**
 * Common logic for the different dynamic connection proxies.
 *
 */

public abstract class AbstractConnectionProxy 
implements InvocationHandler
{
	
	private List statements = new ArrayList();

	protected synchronized void addStatement ( Statement s )
	{
		statements.add ( s );
	}
	
	protected synchronized void forceCloseAllPendingStatements ( boolean warn ) 
	{
		Iterator it = statements.iterator();
		while ( it.hasNext() ) {
			Statement s = ( Statement ) it.next();
			try {
				if ( warn ) Configuration.logWarning ( "Forcing close of pending statement: " + s );
				s.close();
			} catch ( Exception e ) {
				//ignore but log
				Configuration.logWarning ( "Error closing pending statement: " , e );
			}
			//cf case 31275: also remove statement from list!
			it.remove();
		}
	}

}
