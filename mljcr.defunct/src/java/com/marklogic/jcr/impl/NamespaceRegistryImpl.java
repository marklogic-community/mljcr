/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 3, 2008
 * Time: 2:58:13 PM
 */
public class NamespaceRegistryImpl implements NamespaceRegistry
{
	private final Map<String,String> prefixToUri = new HashMap<String, String>();
	private final Map<String,String> uriToPrefix = new HashMap<String, String>();

	public synchronized void registerNamespace (String prefix, String uri)
	{
		prefixToUri.put (prefix, uri);
		uriToPrefix.put (uri, prefix);
	}

	public synchronized void unregisterNamespace (String prefix)
	{
		String uri = prefixToUri.get (prefix);

		prefixToUri.remove (prefix);
		uriToPrefix.remove (uri);
	}

	public synchronized String[] getPrefixes()
	{
		String [] prefixes = new String [prefixToUri.keySet().size()];

		return (prefixToUri.keySet().toArray (prefixes));
	}

	public synchronized String[] getURIs () throws RepositoryException
	{
		String [] uris = new String [uriToPrefix.keySet().size()];

		return (uriToPrefix.keySet().toArray (uris));
	}

	public synchronized String getURI (String prefix) throws RepositoryException
	{
		return prefixToUri.get (prefix);
	}

	public synchronized String getPrefix (String uri) throws RepositoryException
	{
		return uriToPrefix.get (uri);
	}
}
