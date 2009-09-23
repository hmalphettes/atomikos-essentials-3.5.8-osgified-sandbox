
              
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import com.atomikos.datasource.RecoverableResource;
import com.atomikos.datasource.ResourceException;
import com.atomikos.datasource.TransactionalResource;
import com.atomikos.datasource.xa.TemporaryXATransactionalResource;
import com.atomikos.datasource.xa.XAResourceTransaction;
import com.atomikos.datasource.xa.XATransactionalResource;
import com.atomikos.diagnostics.Console;
import com.atomikos.icatch.CompositeTerminator;
import com.atomikos.icatch.CompositeTransaction;
import com.atomikos.icatch.HeurHazardException;
import com.atomikos.icatch.HeurMixedException;
import com.atomikos.icatch.HeurRollbackException;
import com.atomikos.icatch.SysException;
import com.atomikos.icatch.TransactionControl;
import com.atomikos.icatch.TxState;
import com.atomikos.icatch.system.Configuration;

/**
 * 
 * 
 * An adaptor for the javax transaction interface.
 */

class TransactionImp implements Transaction
{

    //
    protected CompositeTransaction ct_;

    protected Map xaresToTxMap_;

    // maps xaresources to restxs, needed for xasuspend/xaresume
    // INVARIANT: this map contains only restxs that are isActive() or
    // isXaSuspended!!!

    protected Stack suspendstack_;

    // after suspend, resume must be able to
    // supply the proper stack of local parent txs

    protected boolean autoRegistration_;

    // true if an unknown XAResource should be accepted
    // and recovered

    TransactionImp ( CompositeTransaction ct , boolean autoRegistration )
    {
        // System.err.println ( "TransactionImp: autoRegistration = " +
        // autoRegistration );
        ct_ = ct;
        autoRegistration_ = autoRegistration;
        xaresToTxMap_ = new HashMap ();
    }

    void setSuspendedStack ( Stack suspendstack )
    {
        suspendstack_ = suspendstack;
    }

    CompositeTransaction getCT ()
    {
        return ct_;
    }

    Stack getSuspendedStack ()
    {
        return suspendstack_;
    }

    private void printMsg ( String msg , int level )
    {
        if ( level == Console.WARN )
            Configuration.logWarning ( msg );
        else if ( level == Console.INFO )
            Configuration.logInfo ( msg );
        else
            Configuration.logDebug ( msg );
    }

    // MM patch: synchronized to allow threaded access
    private synchronized void addXAResourceTransaction (
            XAResourceTransaction restx , XAResource xares )
    {
        xaresToTxMap_.put ( new XAResourceKey ( xares ), restx );
    }

    // MM patch: synchronized to allow threaded access
    private synchronized XAResourceTransaction findXAResourceTransaction (
            XAResource xares )
    {
        XAResourceTransaction ret = null;
        ret = (XAResourceTransaction) xaresToTxMap_.get ( new XAResourceKey (
                xares ) );

        return ret;
    }

    // MM patch: synchronized to allow threaded access
    private synchronized void removeXAResourceTransaction ( XAResource xares )
    {
        xaresToTxMap_.remove ( new XAResourceKey ( xares ) );
    }

    /**
     * @see javax.transaction.Transaction
     */

    public void registerSynchronization ( javax.transaction.Synchronization s )
            throws java.lang.IllegalStateException,
            javax.transaction.SystemException
    {
        try {
            Sync2Sync adaptor = new Sync2Sync ( s );
            ct_.registerSynchronization ( adaptor );
        } catch ( SysException se ) {
        	String msg = "Unexpected error during registerSynchronization";
        	Configuration.logWarning ( msg , se );
            throw new ExtendedSystemException ( msg , se
                    .getErrors () );
        }

    }

    /**
     * @see javax.transaction.Transaction
     */

    public int getStatus ()
    {
        TxState state = (TxState) ct_.getState ();

        if ( state.equals ( TxState.IN_DOUBT ) )
            return Status.STATUS_PREPARED;
        else if ( state.equals ( TxState.PREPARING ) )
            return Status.STATUS_PREPARING;
        else if ( state.equals ( TxState.ACTIVE ) )
            return Status.STATUS_ACTIVE;
        else if ( state.equals ( TxState.MARKED_ABORT ) )
            return Status.STATUS_MARKED_ROLLBACK;
        else
            // other cases are either very short or irrelevant to user?
            return Status.STATUS_UNKNOWN;
    }
    
    private CompositeTerminator getTerminator() 
    {
    	TransactionControl control =  ct_.getTransactionControl ();
        if ( control == null ) {
        	String msg = "No TransactionControl object found?";
        	Configuration.logWarning ( msg );
        	throw new SecurityException ( msg );
        }
            
        CompositeTerminator term = control.getTerminator ();
        if ( term == null ) {
        	String msg = "No TransactionTerminator object found?";
        	Configuration.logWarning ( msg );
        	throw new SecurityException ( msg );
        }
        return term;
    }

    /**
     * @see javax.transaction.Transaction.
     */

    public void commit () throws javax.transaction.RollbackException,
            javax.transaction.HeuristicMixedException,
            javax.transaction.HeuristicRollbackException,
            javax.transaction.SystemException, java.lang.SecurityException
    {

        CompositeTerminator term = getTerminator();
        try {
            term.commit ();
        } catch ( HeurHazardException hh ) {
            throw new HeuristicMixedException ( hh.getMessage () );
        } catch ( HeurRollbackException hr ) {
            throw new HeuristicRollbackException ( hr.getMessage () );
        } catch ( HeurMixedException hm ) {
            throw new HeuristicMixedException ( hm.getMessage () );
        } catch ( SysException se ) {
        	Configuration.logWarning ( se.getMessage() , se );
            throw new ExtendedSystemException ( se.getMessage (), se
                    .getErrors () );
        } catch ( com.atomikos.icatch.RollbackException rb ) {
        	//see case 29708: all statements have been closed
        	String msg = rb.getMessage ();
            throw new javax.transaction.RollbackException ( msg );
        }
    }

    /**
     * @see javax.transaction.Transaction.
     */

    public void rollback () throws IllegalStateException, SystemException
    {

    	CompositeTerminator term = getTerminator();
        try {
            term.rollback ();
        } catch ( SysException se ) {
        	Configuration.logWarning ( se.getMessage() , se );
            throw new ExtendedSystemException ( se.getMessage (), se
                    .getErrors () );
        }

    }

    /**
     * @see javax.transaction.Transaction.
     */

    public void setRollbackOnly () throws IllegalStateException,
            SystemException
    {
        // rollback();
        ct_.getTransactionControl ().setRollbackOnly ();
    }

    /**
     * @see javax.transaction.Transaction.
     */

    public boolean enlistResource ( XAResource xares )
            throws javax.transaction.RollbackException,
            javax.transaction.SystemException, IllegalStateException
    {
        TransactionalResource res = null;
        XATransactionalResource xatxres = null;
        XAResourceTransaction restx = null;
        Stack errors = new Stack ();

        if ( getStatus () == Status.STATUS_MARKED_ROLLBACK ) {
        	String msg =  "Transaction is already marked for rollback - enlisting more resources is useless.";
        	Configuration.logWarning ( msg );
            throw new javax.transaction.RollbackException ( msg );
        }

        Enumeration enumm = Configuration.getResources ();

        while ( enumm.hasMoreElements () ) {
            RecoverableResource rres = (RecoverableResource) enumm
                    .nextElement ();
            if ( rres instanceof XATransactionalResource ) {
                xatxres = (XATransactionalResource) rres;

                if ( xatxres.usesXAResource ( xares ) )
                    res = xatxres;

            }

        }

        printMsg ( "enlistResource ( " + xares + " ) with transaction "
                + toString (), Console.INFO );

        if ( res == null ) {

            if ( autoRegistration_ ) {

                // ADDED with new recovery: unknown resources can be tolerated
                // by
                // adding a new TemporaryXATransactionalResource
                printMsg ( "constructing new temporary resource "
                        + "for unknown XAResource: " + xares, Console.DEBUG );
                res = new TemporaryXATransactionalResource ( xares );
                Configuration.addResource ( res );

            } else {
            	String msg = "There is no registered resource that can recover the given XAResource instance. " + "\n" +
                "Either enable automatic resource registration, or register a corresponding resource.";
            	Configuration.logWarning ( msg );
                throw new javax.transaction.SystemException ( msg );
            }
        }

        
        // if this xares was suspended then it will still be in the map
        XAResourceTransaction active = findXAResourceTransaction ( xares );

        if ( active != null ) {

            // following violates XA state tables
            // and the invariant of the xaresToTxMap table
            if ( !active.isXaSuspended () ) {
            	String msg = "The given XAResource instance is being enlisted a second time without delist in between?";
            	Configuration.logWarning ( msg );
                throw new IllegalStateException ( msg );
            }

            // note: for suspended XAResources, the lookup MUST SUCCEED
            // since the TMRESUME must be called on the SAME XAResource
            // INSTANCE, and lookup also works on the instance level
            try {
                // ADDED: resume should also refresh the xaresource
                restx.setXAResource ( xares );
                active.xaResume ();
            } catch ( XAException xaerr ) {
                if ( (XAException.XA_RBBASE <= xaerr.errorCode)
                        && (xaerr.errorCode <= XAException.XA_RBEND) )
                    throw new javax.transaction.RollbackException (
                            "Transaction was already rolled back inside the back-end resource. Further enlists are useless." );

                errors.push ( xaerr );
                throw new ExtendedSystemException ( "Unexpected error during enlist", errors );
            }

        } else {

            try {
                restx = (XAResourceTransaction) res
                        .getResourceTransaction ( ct_ );

                // next, we MUST set the xa resource again,
                // because ONLY the instance we got as argument
                // is available for use now !
                // older instances (set in restx from previous sibling)
                // have connections that may be in reuse already
                // ->old xares not valid except for 2pc operations

                restx.setXAResource ( xares );
                restx.resume ();
            } catch ( ResourceException re ) {
                // re.printStackTrace();
                Stack nested = re.getDetails ();
                if ( !nested.empty ()
                        && (nested.peek () instanceof XAException) ) {
                    XAException xaerr = (XAException) nested.peek ();
                    if ( (XAException.XA_RBBASE <= xaerr.errorCode)
                            && (xaerr.errorCode <= XAException.XA_RBEND) )
                        throw new javax.transaction.RollbackException (
                                "The transaction was rolled back in the back-end resource. Further enlists are useless." );
                }

                errors.push ( re );
                throw new ExtendedSystemException ( "Unexpected error during enlist", errors );
            } catch ( RuntimeException e ) {
                // e.printStackTrace();
                throw e;
            }

            addXAResourceTransaction ( restx, xares );
        }

        return true;
    }

    /**
     * @see javax.transaction.Transaction.
     */

    public boolean delistResource ( XAResource xares , int flag )
            throws java.lang.IllegalStateException,
            javax.transaction.SystemException
    {
        Stack errors = new Stack ();

        TransactionalResource res = null;
        Enumeration enumm = Configuration.getResources ();
        while ( enumm.hasMoreElements () ) {
            RecoverableResource rres = (RecoverableResource) enumm
                    .nextElement ();
            if ( rres instanceof XATransactionalResource ) {
                XATransactionalResource xatxres = (XATransactionalResource) rres;
                if ( xatxres.usesXAResource ( xares ) )
                    res = xatxres;
            }

        }

        printMsg ( "delistResource ( " + xares + " ) with transaction "
                + toString (), Console.INFO );

        if ( res == null ) {
        	String msg =  "There is no registered resource that can recover the given XAResource instance. " + "\n" +
    		"Either enable automatic resource registration, or register a corresponding resource.";
        	Configuration.logWarning ( msg );
            throw new javax.transaction.SystemException ( msg );
        }

    
        XAResourceTransaction active = findXAResourceTransaction ( xares );

        // NOTE: the lookup MUST have succeeded since the delist must be
        // done by the same XAResource INSTANCE as the enlist before,
        // and lookup also uses instance comparison.
        if ( active == null ) {
        	String msg = "Illegal attempt to delist an XAResource instance that was not previously enlisted.";
        	Configuration.logWarning ( msg );
            throw new IllegalStateException ( msg );
        }

        if ( flag == XAResource.TMSUCCESS || flag == XAResource.TMFAIL ) {

            try {
                active.suspend ();
            } catch ( ResourceException re ) {
                errors.push ( re );
                throw new ExtendedSystemException ( "Error in delisting the given XAResource", errors );
            }
            removeXAResourceTransaction ( xares );

            // NOTE: if failure, then make sure no rollback can happen
            if ( flag == XAResource.TMFAIL )
                setRollbackOnly ();
        } else if ( flag == XAResource.TMSUSPEND ) {
            // call suspend on active xaresource.

            try {
                active.xaSuspend ();
            } catch ( XAException xaerr ) {
                errors.push ( xaerr );
                throw new ExtendedSystemException ( "Error in delisting the given XAResource", errors );
            }

        } else {
        	String msg = "Unknown delist flag: " + flag;
        	Configuration.logWarning ( msg );
            throw new javax.transaction.SystemException ( msg );
        }
        return true;
    }



    /**
     * Compares to another object.
     * 
     * @param o
     *            The other object to compare to.
     * 
     * @return boolean True iff the underlying tx is the same.
     */

    public boolean equals ( Object o )
    {
        if ( o == null || !(o instanceof TransactionImp) )
            return false;
        TransactionImp other = (TransactionImp) o;
        return ct_.isSameTransaction ( other.ct_ );
    }

    /**
     * Computes a hash value for the object.
     * 
     * @return int The hash value.
     */

    public int hashCode ()
    {
        return ct_.hashCode ();
    }

    public String toString ()
    {
        return ct_.getTid ().toString ();
    }
}
