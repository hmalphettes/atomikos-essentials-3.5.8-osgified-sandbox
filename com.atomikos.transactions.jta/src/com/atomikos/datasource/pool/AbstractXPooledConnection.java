
              
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
 
package com.atomikos.datasource.pool;

import java.util.ArrayList;
import java.util.List;

import com.atomikos.icatch.HeuristicMessage;
import com.atomikos.icatch.system.Configuration;

 
 /**
  * 
  * 
  * Abstract superclass with generic support for XPooledConnection.
  * 
  * @author guy
  *
  */

public abstract class AbstractXPooledConnection implements XPooledConnection {

	private long lastTimeAcquired = System.currentTimeMillis();
	private long lastTimeReleased = System.currentTimeMillis();
	private List poolEventListeners = new ArrayList();
	private Reapable currentProxy = null;
	private ConnectionPoolProperties props;
	
	protected AbstractXPooledConnection ( ConnectionPoolProperties props ) 
	{
		this.props = props;
	}

	public long getLastTimeAcquired() {
		return lastTimeAcquired;
	}

	public long getLastTimeReleased() {
		return lastTimeReleased;
	}
	
	public synchronized Reapable createConnectionProxy ( HeuristicMessage hmsg ) throws CreateConnectionException
	{
		updateLastTimeAcquired();
		testUnderlyingConnection();
		currentProxy = doCreateConnectionProxy ( hmsg );
		Configuration.logDebug ( this + ": returning proxy " + currentProxy );
		return currentProxy;
	}

	public void reap() {
		
		if ( currentProxy != null ) {
			Configuration.logWarning ( this + ": reaping connection..." );
			currentProxy.reap();
		}
	}

	public void registerXPooledConnectionEventListener(XPooledConnectionEventListener listener) {
		Configuration.logDebug ( this + ": registering listener " + listener );
		poolEventListeners.add(listener);
	}

	public void unregisterXPooledConnectionEventListener(XPooledConnectionEventListener listener) {
		Configuration.logDebug ( this + ": unregistering listener " + listener );
		poolEventListeners.remove(listener);
	}

	protected void fireOnXPooledConnectionTerminated() {
		for (int i=0; i<poolEventListeners.size() ;i++) {
			XPooledConnectionEventListener listener = (XPooledConnectionEventListener) poolEventListeners.get(i);
			Configuration.logDebug ( this + ": notifying listener: " + listener );
			listener.onXPooledConnectionTerminated(this);
		}
	}

	protected String getTestQuery() 
	{
		return props.getTestQuery();
	}
	
	protected void updateLastTimeReleased() {
		Configuration.logDebug ( this + ": updating last time released" );
		lastTimeReleased = System.currentTimeMillis();
	}
	
	protected void updateLastTimeAcquired() {
		Configuration.logDebug (  this + ": updating last time acquired" );
		lastTimeAcquired = System.currentTimeMillis();
		
	}
	
	protected Reapable getCurrentConnectionProxy() {
		return currentProxy;
	}

	public boolean canBeRecycledForCallingThread ()
	{
		//default is false
		return false;
	}

	protected int getDefaultIsolationLevel() 
	{
		return props.getDefaultIsolationLevel();
	}
	
	protected abstract Reapable doCreateConnectionProxy ( HeuristicMessage hmsg ) throws CreateConnectionException;

	protected abstract void testUnderlyingConnection() throws CreateConnectionException;
}
