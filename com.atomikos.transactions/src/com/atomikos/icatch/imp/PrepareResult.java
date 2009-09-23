
              
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import com.atomikos.icatch.HeurCommitException;
import com.atomikos.icatch.HeurHazardException;
import com.atomikos.icatch.HeurMixedException;

/**
 * 
 * 
 * A result for prepare messages.
 */

class PrepareResult extends Result
{
    protected Hashtable readonlytable_ = new Hashtable ();
    // for read only voters
    protected Hashtable indoubts_ = new Hashtable ();
    // for indoubt participants
    // should be rolled back in case of failure!

    protected Hashtable heuristics_ = new Hashtable ();

    protected boolean analyzed_ = false;

    protected Vector msgvector_ = new Vector ();

    /**
     * Constructor.
     * 
     * @param count
     *            The number of replies to deal with.
     */

    public PrepareResult ( int count )
    {
        super ( count );
    }

    protected synchronized void analyze () throws IllegalStateException,
            InterruptedException
    {
        if ( analyzed_ )
            return;

        boolean allReadOnly = true;
        boolean allYes = true;
        boolean heurmixed = false;
        boolean heurhazards = false;
        boolean heurcommits = false;
        Stack replies = getReplies ();
        Enumeration enumm = replies.elements ();

        while ( enumm.hasMoreElements () ) {
            boolean yes = false;
            boolean readonly = false;

            Reply reply = (Reply) enumm.nextElement ();

            if ( reply.hasFailed () ) {
                yes = false;
                readonly = false;

                Exception err = reply.getException ();
                if ( err instanceof HeurMixedException ) {
                    heurmixed = true;
                    HeurMixedException hm = (HeurMixedException) err;
                    addMessages ( hm.getHeuristicMessages () );
                } else if ( err instanceof HeurCommitException ) {
                    heurcommits = true;
                    HeurCommitException hc = (HeurCommitException) err;
                    addMessages ( hc.getHeuristicMessages () );
                    heurmixed = (heurmixed || heurhazards);
                } else if ( err instanceof HeurHazardException ) {
                    heurhazards = true;
                    heurmixed = (heurmixed || heurcommits);
                    HeurHazardException hr = (HeurHazardException) err;
                    indoubts_.put ( reply.getParticipant (),
                            new Boolean ( true ) );
                    // REMEMBER: might be indoubt, so HAS to be notified
                    // during rollback!
                    addMessages ( hr.getHeuristicMessages () );
                }

            }// if failed

            else {
                readonly = (reply.getResponse () == null);
                Boolean answer = new Boolean ( false );
                if ( !readonly ) {
                    answer = (Boolean) reply.getResponse ();
                }
                yes = (readonly || answer.booleanValue ());

                // if readonly: remember this fact for logging and second phase
                if ( readonly )
                    readonlytable_.put ( reply.getParticipant (), new Boolean (
                            true ) );
                else
                    indoubts_.put ( reply.getParticipant (),
                            new Boolean ( true ) );
            }

            allYes = (allYes && yes);
            allReadOnly = (allReadOnly && readonly);

        }

        if ( heurmixed )
            result_ = HEUR_MIXED;
        else if ( heurcommits )
            result_ = HEUR_COMMIT;
        else if ( heurhazards )
            result_ = HEUR_HAZARD;
        else if ( allReadOnly )
            result_ = ALL_READONLY;
        else if ( allYes )
            result_ = ALL_OK;

        analyzed_ = true;
    }

    /**
     * Test if all answers represent a yes vote. Blocks until all results
     * arrived.
     * 
     * @return boolean True if all are yes, false if not.
     * @exception InterruptedException
     *                If interrupt on wait.
     */

    public boolean allYes () throws InterruptedException
    {
        analyze ();
        return (result_ == ALL_OK || result_ == ALL_READONLY);

    }

    /**
     * Test if all answers were readonly votes. Blocks till all results known.
     * 
     * @return boolean True if all readonly, false otherwise.
     * @exception InterruptedException
     *                If interrupted.
     */

    public boolean allReadOnly () throws InterruptedException
    {
        analyze ();
        return (result_ == ALL_READONLY);
    }

    /**
     * Get a table of readonly voting participants.
     * 
     * @return Hashtable Contains a key per readonly participant.
     * @exception InterruptedException
     *                If interrupted on wait.
     */

    public Hashtable getReadOnlyTable () throws InterruptedException
    {
        analyze ();
        return readonlytable_;
    }

    /**
     * Get a table of indoubt participants, which have to be notified of commit
     * or rollback.
     * 
     * @return Hashtable A key per indoubt participant.
     * @exception InterruptedException
     *                If interrupted on wait.
     */

    public Hashtable getIndoubtTable () throws InterruptedException
    {
        analyze ();
        return indoubts_;
    }

}
