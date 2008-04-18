/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import com.marklogic.jcr.RepositoryFactory;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 3, 2008
 * Time: 3:34:16 PM
 */
public class TestNamespaceRegistry
{
	private NamespaceRegistry registry;

	@Before
	public void setup() throws URISyntaxException, RepositoryException
	{
		Repository repo = RepositoryFactory.openRepository (new URI ("mockjcr://admin:admin@localhost:8080/testws"));
		Session session = repo.login();
		registry = session.getWorkspace().getNamespaceRegistry();
	}

	// ----------------------------------------------------------

	@Test
	public void testUnregister() throws RepositoryException
	{
		registry.registerNamespace ("foobar", "namespace");
		registry.registerNamespace ("blazboo", "blazns");
		registry.registerNamespace ("furble", "furbns");

		assertEquals ("namespace", registry.getURI ("foobar"));
		assertEquals ("blazns", registry.getURI ("blazboo"));
		assertEquals ("furbns", registry.getURI ("furble"));
		assertEquals (3, registry.getPrefixes().length);
		assertEquals (3, registry.getURIs ().length);

		registry.unregisterNamespace ("blazboo");
		assertNull (registry.getURI ("blazboo"));
		assertEquals (2, registry.getPrefixes().length);
		assertEquals (2, registry.getURIs ().length);

		registry.unregisterNamespace ("foobar");
		assertNull (registry.getURI ("foobar"));
		assertEquals (1, registry.getPrefixes().length);
		assertEquals (1, registry.getURIs ().length);
		
		registry.unregisterNamespace ("furble");
		assertNull (registry.getURI ("furble"));
		assertEquals (0, registry.getPrefixes().length);
		assertEquals (0, registry.getURIs ().length);
	}

	@Test
	public void testGetUris() throws RepositoryException
	{
		registry.registerNamespace ("foobar", "namespace");
		registry.registerNamespace ("blazboo", "blazns");
		registry.registerNamespace ("furble", "furbns");

		String [] uris = registry.getURIs();

		assertEquals (3, uris.length);
		assertTrue (containsString ("namespace", uris));
		assertTrue (containsString ("blazns", uris));
		assertTrue (containsString ("furbns", uris));
	}

	@Test
	public void testGetPrefixes() throws RepositoryException
	{
		registry.registerNamespace ("foobar", "namespace");
		registry.registerNamespace ("blazboo", "blazns");
		registry.registerNamespace ("furble", "furbns");

		String [] prefixes = registry.getPrefixes();

		assertEquals (3, prefixes.length);
		assertTrue (containsString ("blazboo", prefixes));
		assertTrue (containsString ("furble", prefixes));
		assertTrue (containsString ("foobar", prefixes));
	}

	@Test
	public void testPrefixToNS() throws RepositoryException
	{
		registry.registerNamespace ("foobar", "namespace");
		registry.registerNamespace ("blazboo", "blazns");
		registry.registerNamespace ("furble", "furbns");
		assertEquals ("namespace", registry.getURI ("foobar"));
		assertEquals ("blazns", registry.getURI ("blazboo"));
		assertEquals ("furbns", registry.getURI ("furble"));
	}

	@Test
	public void testNStoPrefix() throws RepositoryException
	{
		registry.registerNamespace ("foobar", "namespace");
		registry.registerNamespace ("blazboo", "blazns");
		registry.registerNamespace ("furble", "furbns");
		assertEquals ("foobar", registry.getPrefix ("namespace"));
		assertEquals ("furble", registry.getPrefix ("furbns"));
		assertEquals ("blazboo", registry.getPrefix ("blazns"));
	}

	@Test
	public void testSetPrefix() throws RepositoryException
	{
		assertNull (registry.getPrefix ("foobar"));
		assertEquals (0, registry.getPrefixes ().length);
		registry.registerNamespace ("foobar", "namespace");
		assertEquals (1, registry.getPrefixes ().length);
		assertEquals ("namespace", registry.getURI ("foobar"));
	}

	@Test
	public void testNonExist() throws RepositoryException
	{
		assertNull (registry.getURI ("foobar"));
		assertNull (registry.getPrefix ("foobar"));
		assertEquals (0, registry.getPrefixes().length);
		assertEquals (0, registry.getURIs().length);
	}

	// --------------------------------------------------

	private boolean containsString (String s, String [] array)
	{
		for (int i = 0; i < array.length; i++) {
			if (array [i].equals (s)) {
				return true;
			}
		}

		return false;
	}
}
