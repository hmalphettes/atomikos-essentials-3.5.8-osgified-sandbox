/**
 * 
 */

              
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
 
package com.atomikos.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * A helper class for class loading.
 *
 * 
 */

public class ClassLoadingHelper 
{
	
	/**
	 * Creates a new dynamic proxy instance for the given delegate.
	 * 
	 * @param classLoadersToTry The class loaders to try, in the specified list order.
	 * @param interfaces The interfaces to add to the returned proxy.
	 * @param delegate The underlying object that will receive the calls on the proxy.
	 * @return The proxy.
	 * 
	 * @exception IllegalArgumentException If any of the interfaces involved could 
	 * not be loaded.
	 */
	
	public static Object newProxyInstance ( 
			List classLoadersToTry , Class[] interfaces , InvocationHandler delegate ) 
		 	throws IllegalArgumentException
	{
		Object ret = null;
		ClassLoader cl = ( ClassLoader ) classLoadersToTry.get ( 0 );
		List remainingClassLoaders = classLoadersToTry.subList ( 1, classLoadersToTry.size() );
		
		try {
			return Proxy.newProxyInstance ( cl , interfaces , delegate );
		} catch ( IllegalArgumentException someClassNotFound ) {
			if ( remainingClassLoaders.size() > 0 ) {
				//try with remaining class loaders
				ret = newProxyInstance ( remainingClassLoaders , interfaces , delegate );
			} else {
				//rethrow to caller
				throw someClassNotFound;
			}
		}
		
		return ret;
	}

	/**
	 * Loads a class with the given name.
	 * 
	 * @param className
	 * @return The class object
	 * @throws ClassNotFoundException If not found
	 */
	public static Class loadClass ( String className ) throws ClassNotFoundException
	{
		Class clazz = null;
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass( className );
		} catch ( ClassNotFoundException nf ) {
			clazz = Class.forName ( className );
		}
		return clazz;
	}
}
