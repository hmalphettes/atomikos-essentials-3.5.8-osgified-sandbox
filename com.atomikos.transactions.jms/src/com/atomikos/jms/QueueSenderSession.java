
              
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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;

import com.atomikos.icatch.system.Configuration;


/**
 * 
 * 
 * 
 * This is a <b>long-lived</b> queue sender session, representing a
 * self-refreshing JMS session that can be used to send JMS messages in a
 * transactional way. The client code does not have to worry about refreshing or
 * closing JMS objects explicitly: this is all handled in this class. All the
 * client needs to do is indicate when it wants to start or stop using the
 * session.
 * <p>
 * Note that instances are not meant for concurrent use by different threads:
 * each thread should use a private instance instead.
 * <p>
 * <b>Important: if you change any properties AFTER sending on the session, then
 * you will need to explicitly stop and restart the session to have the changes
 * take effect!</b>
 * 
 */

public class QueueSenderSession
extends MessageProducerSession
{
    /**
     * Default constructor in JavaBean style. Needed to ensure compatibility
     * with third-party frameworks such as Spring.
     */

    public QueueSenderSession ()
    {


    }

    /**
     * If this session is used for sending request/reply messages, then this
     * property indicates the queue to where the replies are to be sent (optional). The
     * session uses this to set the JMSReplyTo header accordingly. This property
     * can be omitted if no reply is needed.
     * 
     * <p>
     * The replyToQueue should be in the same JMS vendor domain as the send
     * queue. To cross domains, configure a bridge for both the request and the
     * reply channels.
     * 
     * @param queue
     *            The queue where a reply should go.
     */

    public void setReplyToQueue ( Queue queue )
    {
        setReplyToDestination ( queue );
    }

    /**
     * Gets the queue where replies are expected, or null if not applicable
     * (or if the replyToDestination is not a queue but a topic).
     * 
     * @return
     */

    public Queue getReplyToQueue ()
    {
    	    Queue ret = null;
    	    Destination dest = getReplyToDestination();
    	    if ( dest instanceof Queue ) {
    	    		ret = ( Queue ) dest;
    	    }
        return ret;
    }

    /**
     * @return The queue to send to.
     */

    public Queue getQueue ()
    {
        return ( Queue ) getDestination();
    }

    /**
     * @return The queue connection factory bean.
     */

    public QueueConnectionFactoryBean getQueueConnectionFactoryBean ()
    {
        return ( QueueConnectionFactoryBean ) getAbstractConnectionFactoryBean();
    }

    /**
     * Set the queue to use for sending (required).
     * 
     * @param queue
     *            The queue.
     */

    public void setQueue ( Queue queue )
    {
        setDestination ( queue );
    }

    /**
     * Set the queue connection factory, needed to create or refresh
     * connections (required).
     * 
     * @param bean
     */

    public void setQueueConnectionFactoryBean ( QueueConnectionFactoryBean bean )
    {
        setAbstractConnectionFactoryBean ( bean );
    }

	protected String getDestinationName() 
	{
		String ret = null;
		Queue q = getQueue();
		if ( q != null ) {
			try {
				ret = q.getQueueName();
			} catch ( JMSException e ) {
				Configuration.logDebug ( "QueueSenderSession: error retrieving queue name" , e );
			}
		}
		return ret;
	}

	protected String getReplyToDestinationName() 
	{
		String ret = null;
		Queue q = getReplyToQueue();
		if ( q != null ) {
			try {
				ret = q.getQueueName();
			} catch ( JMSException e ) {
				Configuration.logDebug ( "QueueSenderSession: error retrieving queue name" , e );
			}
		}
		return ret;
	}

}
