
              
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
 
package com.atomikos.icatch.imp;

import java.util.Properties;
import java.util.Stack;

import com.atomikos.icatch.CompositeCoordinator;
import com.atomikos.icatch.CompositeTransaction;
import com.atomikos.icatch.HeuristicMessage;
import com.atomikos.icatch.RecoveryCoordinator;
import com.atomikos.icatch.SysException;

/**
 * 
 * 
 * A composite transaction adaptor for interposition on an imported instance.
 * This allows substitution of the recovery coordinator adaptor.
 */

public class CompositeTransactionAdaptor extends AbstractCompositeTransaction
        implements CompositeCoordinator
{

    private RecoveryCoordinator adaptor_;
    // the adaptor to use for replay requests

    private String root_;
    
    private Boolean isRecoverableWhileActive_;

    // the root TID

    /**
     * Create a new instance.
     * 
     * @param lineage
     *            The parent info, <b>not including</b> the instance being
     *            constructed here!
     * @param serial
     *            True if serial.
     * @param adaptor
     *            The adaptor for replay requests.
     * @param isRecoverableWhileActive 
     *            Whether recoverable in active state or not. Null if not known.
     */

    public CompositeTransactionAdaptor ( Stack lineage , String tid ,
            boolean serial , RecoveryCoordinator adaptor , Boolean isRecoverableWhileActive )
    {
        super ( tid , (Stack) lineage.clone () , serial  );
        adaptor_ = adaptor;
        Stack tmp = (Stack) lineage.clone ();
        CompositeTransaction parent = null;
        while ( !tmp.empty () ) {
            parent = (CompositeTransaction) tmp.pop ();
        }
        root_ = parent.getTid ();
        isRecoverableWhileActive_ = isRecoverableWhileActive;
    }
    
    /**
     * Constructor for testin.
     * @param root
     * @param serial
     * @param adaptor
     */
    
    public CompositeTransactionAdaptor ( String root , boolean serial ,
            RecoveryCoordinator adaptor )
    {
    	 this ( root , serial , adaptor , null );
    }

    /**
     * Constructs a new instance for an imported ROOT. This constructor is
     * needed for message-based propagation where only the root TID is passed.
     * 
     * @param root
     *            The root URI.
     * @param serial
     *            Flag for serial mode.
     * @param adaptor
     *            The adaptor for recovery.
     */

    public CompositeTransactionAdaptor ( String root , boolean serial ,
            RecoveryCoordinator adaptor , Properties properties )
    {
        super ( root , null , serial );
        adaptor_ = adaptor;
        root_ = root;
        if ( properties != null ) properties_ =  ( Properties ) properties.clone();
    }

    /**
     * @see CompositeCoordinator.
     */

    public CompositeCoordinator getCompositeCoordinator () throws SysException
    {
        return this;
    }

    /**
     * @see CompositeCoordinator.
     */

    public String getCoordinatorId ()
    {
        return root_;
    }

    /**
     * @see CompositeCoordinator.
     */

    public HeuristicMessage[] getTags ()
    {
        return null;
    }

    /**
     * @see CompositeCoordinator.
     */

    public RecoveryCoordinator getRecoveryCoordinator ()
    {
        return adaptor_;
    }

    public Boolean isRecoverableWhileActive ()
    {
        return isRecoverableWhileActive_;
    }

    public void setRecoverableWhileActive()
    {
        throw new UnsupportedOperationException();
    }
   
}
