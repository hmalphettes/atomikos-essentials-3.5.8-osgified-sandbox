
              
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
 
package com.atomikos.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import com.atomikos.datasource.xa.session.SessionHandleState;
import com.atomikos.icatch.system.Configuration;

class AtomikosJmsMessageConsumerProxy extends ConsumerProducerSupport implements
		HeuristicMessageConsumer {
	
	
	private MessageConsumer delegate;
	
	public AtomikosJmsMessageConsumerProxy ( MessageConsumer delegate , SessionHandleState state ) {
		super ( state );
		this.delegate = delegate;
	}
	
	protected MessageConsumer getDelegate() 
	{
		return delegate;
	}
	
	public Message receive ( String hmsg ) throws JMSException {
		Configuration.logInfo ( this + ": receive ( " + hmsg + " )..." );
		Message ret = null;
		try {
			enlist ( hmsg );
			ret = delegate.receive();
		} catch (Exception e) {
			handleException ( e );
		}
		Configuration.logDebug ( this + ": receive returning " + ret );
		return ret;
	}

	public Message receive ( long timeout , String hmsg ) throws JMSException {
		Configuration.logInfo ( this + ": receive ( " + timeout + " , " + hmsg + " )..." );
		
		Message ret = null;
		try {
			enlist ( hmsg );
			ret = delegate.receive ( timeout );
		} catch (Exception e) {
			handleException ( e );
		}
		Configuration.logDebug ( this + ": receive returning " + ret );
		return ret;
	}

	public Message receiveNoWait ( String hmsg ) throws JMSException {
		Configuration.logInfo ( this + ": receiveNoWait ( " + hmsg + " )..." );
		
		Message ret = null;
		try {
			enlist ( hmsg );
			ret = delegate.receiveNoWait();
		} catch (Exception e) {
			handleException ( e );
		}
		Configuration.logDebug ( this + ": receiveNoWait returning " + ret );
		return ret;
	}

	public void close() throws JMSException {
		//note: delist is done at session level!
		Configuration.logInfo ( this + ": close..." );
		try {
			delegate.close();
		} catch (Exception e) {
			handleException ( e );
		}
		Configuration.logDebug ( this + ": close done." );	
	}

	public MessageListener getMessageListener() throws JMSException {
		Configuration.logInfo ( this + ": getMessageListener()..." );
		MessageListener ret = null;
		try {
			ret = delegate.getMessageListener();
		} catch (Exception e) {
			handleException ( e );
		}
		Configuration.logDebug ( this + ": getMessageListener() returning " + ret );
		return ret;
	}

	public String getMessageSelector() throws JMSException {
		Configuration.logInfo ( this + ": getMessageSelector()..." );
		String ret = null;
		try {
			ret = delegate.getMessageSelector();
		} catch (Exception e) {
			handleException ( e );
		}
		Configuration.logDebug ( this + ": getMessageSelector() returning " + ret );
		return ret;
	}

	public Message receive() throws JMSException {
		Configuration.logInfo ( this + ": receive()..." );
		Message ret = receive ( null );
		Configuration.logDebug ( this + ": receive() returning " + ret );
		return ret;
	}

	public Message receive ( long timeout ) throws JMSException {
		Configuration.logInfo ( this + ": receive ( " + timeout + " )..." );
		Message ret =  receive ( timeout , null );
		Configuration.logDebug ( this + ": receive() returning " + ret );
		return ret;
	}

	public Message receiveNoWait() throws JMSException {
		Configuration.logInfo ( this + ": receiveNoWait()..." );
		Message ret = receiveNoWait ( null );
		Configuration.logDebug ( this + ": receiveNoWait() returning " + ret );
		return ret;
	}

	public void setMessageListener ( MessageListener listener ) throws JMSException {
		Configuration.logInfo ( this + ": setMessageListener ( " + listener + " )..." );
		try {
			delegate.setMessageListener ( listener );
		}catch (Exception e) {
			handleException ( e );
		}
		Configuration.logDebug ( this + ": setMessageListener done." );
	}
	
	public String toString() 
	{
		return "atomikos MessageConsumer proxy for " + delegate;
	}

}
