/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import com.marklogic.jcr.RepositoryFactory;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Credentials;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;


/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 3:56:58 PM
 */
@SuppressWarnings({"ClassWithTooManyMethods"})
public class TestSession
{
	Repository repo = null;
	Session session = null;

	@Before
	public void setup() throws URISyntaxException, RepositoryException
	{
		repo = RepositoryFactory.openRepository (new URI ("mockjcr://admin:admin@localhost:8080/testws"));
		session = repo.login();
	}

	// --------------------------------------------------

	@Test
	public void testImpersonate() throws RepositoryException
	{
		SimpleCredentials creds = new SimpleCredentials ("barney", "foo".toCharArray());
		Session impSession = session.impersonate (creds);

		assertNotNull (impSession);
		assertNotSame (session, impSession);
		assertNotSame (session.getWorkspace(), impSession.getWorkspace());
		assertEquals ("barney", impSession.getUserID());
	}

	@Test
	public void testGetPrefixes() throws RepositoryException
	{
		session.setNamespacePrefix ("foobar", "namespace");
		session.setNamespacePrefix ("blazboo", "blazns");
		session.setNamespacePrefix ("furble", "furbns");

		String [] prefixes = session.getNamespacePrefixes();

		assertEquals (3, prefixes.length);
		assertTrue (containsString ("blazboo", prefixes));
		assertTrue (containsString ("furble", prefixes));
		assertTrue (containsString ("foobar", prefixes));
	}

	private boolean containsString (String s, String [] array)
	{
		for (int i = 0; i < array.length; i++) {
			if (array [i].equals (s)) {
				return true;
			}
		}

		return false;
	}

	@Test
	public void testPrefixToNS() throws RepositoryException
	{
		session.setNamespacePrefix ("foobar", "namespace");
		session.setNamespacePrefix ("blazboo", "blazns");
		session.setNamespacePrefix ("furble", "furbns");
		assertEquals ("namespace", session.getNamespaceURI ("foobar"));
		assertEquals ("blazns", session.getNamespaceURI ("blazboo"));
		assertEquals ("furbns", session.getNamespaceURI ("furble"));
	}

	@Test
	public void testNStoPrefix() throws RepositoryException
	{
		session.setNamespacePrefix ("foobar", "namespace");
		session.setNamespacePrefix ("blazboo", "blazns");
		session.setNamespacePrefix ("furble", "furbns");
		assertEquals ("foobar", session.getNamespacePrefix ("namespace"));
		assertEquals ("furble", session.getNamespacePrefix ("furbns"));
		assertEquals ("blazboo", session.getNamespacePrefix ("blazns"));
	}

	@Test
	public void testSetPrefix() throws RepositoryException
	{
		assertNull (session.getNamespacePrefix ("foobar"));
		assertEquals (0, session.getNamespacePrefixes().length);
		session.setNamespacePrefix ("foobar", "namespace");
		assertEquals (1, session.getNamespacePrefixes().length);
		assertEquals ("namespace", session.getNamespaceURI ("foobar"));
	}

	@Test
	public void testGetEmptyPrefix() throws RepositoryException
	{
		assertNull (session.getNamespacePrefix ("foobar"));
		assertEquals (0, session.getNamespacePrefixes ().length);
	}

	@Test
	public void testGetRootNode() throws RepositoryException
	{
		Node node = session.getRootNode();

		assertNotNull (node);
		assertEquals ("", node.getPath());
	}

	@Test(expected=PathNotFoundException.class)
	public void testItem() throws RepositoryException
	{
		session.getItem ("/foo/bar/baz/bop/blither");
	}

	@Test(expected=ItemNotFoundException.class)
	public void testGetNodeByUUID() throws RepositoryException
	{
		session.getNodeByUUID ("2344-2344-2342");
	}

	@Test
	public void testItemExists() throws RepositoryException
	{
		assertFalse (session.itemExists("/foo/bar/baz"));
	}

	@Test
	public void testPendingChanges() throws RepositoryException
	{
		assertFalse (session.hasPendingChanges());
		session.save();
		assertFalse (session.hasPendingChanges());
	}

	@Test
	public void testGetSession() throws Throwable
	{
		Workspace ws = session.getWorkspace();

		assertNotNull (ws);
		assertEquals ("testws", ws.getName());
	}

	@Test
	public void testGetValueFactory() throws RepositoryException
	{
		ValueFactory factory = session.getValueFactory();

		assertNotNull (factory);

		Value val = factory.createValue ("foobar", PropertyType.STRING);

		assertNotNull (val);
		assertEquals ("foobar", val.getString ());
	}

	@Test(expected=UnsupportedRepositoryOperationException.class)
	public void testGetLockTokens() throws Throwable
	{
		try {
			session.getLockTokens();
		} catch (RuntimeException e) {
			throw e.getCause();
		}
	}

	@Test(expected=UnsupportedRepositoryOperationException.class)
	public void testRemoveLockToken() throws Throwable
	{
		try {
			session.removeLockToken ("foo");
		} catch (RuntimeException e) {
			throw e.getCause();
		}
	}

	@Test(expected=UnsupportedRepositoryOperationException.class)
	public void testAddLockToken() throws Throwable
	{
		try {
			session.addLockToken ("foo");
		} catch (RuntimeException e) {
			throw e.getCause();
		}
	}

	@Test
	public void testNullAttributes()
	{
		assertNull (session.getAttribute ("foo"));
		assertNull (session.getAttribute ("blather"));
		assertNull (session.getAttribute (""));
	}

	@Test
	public void testAttributes () throws RepositoryException
	{
		SimpleCredentials creds = new SimpleCredentials ("barney", "foo".toCharArray());
		Object obj = new Object();

		creds.setAttribute ("foo", "bar");
		creds.setAttribute ("barbie", "ken");
		creds.setAttribute ("gijoe", new BigInteger ("43210"));
		creds.setAttribute ("zoob", obj);
		session = repo.login (creds);

		assertEquals ("ken", session.getAttribute ("barbie"));
		assertEquals ("bar", session.getAttribute ("foo"));
		assertTrue (session.getAttribute ("gijoe") instanceof BigInteger);
		assertEquals ("43210", session.getAttribute ("gijoe").toString());
		assertEquals (43210, ((BigInteger) session.getAttribute ("gijoe")).longValue());
		assertSame (obj, session.getAttribute ("zoob"));
	}

	@Test
	public void testNullAttributeNames()
	{
		assertTrue (session.getAttributeNames() instanceof String []);
		assertEquals (0, session.getAttributeNames().length);
	}

	@Test
	public void testAttributeNames () throws RepositoryException
	{
		SimpleCredentials creds = new SimpleCredentials ("barney", "foo".toCharArray());
		session = repo.login (creds);

		creds.setAttribute ("attr1", "val1");
		creds.setAttribute ("attr2", "val2");
		creds.setAttribute ("attr3", "val3");
		creds.setAttribute ("attr4", "val4");
		creds.setAttribute ("attr5", "val5");

		assertEquals (5, session.getAttributeNames().length);

		String [] names = session.getAttributeNames();
		Arrays.sort (names);

		assertEquals ("attr1", names [0]);
		assertEquals ("attr2", names [1]);
		assertEquals ("attr3", names [2]);
		assertEquals ("attr4", names [3]);
		assertEquals ("attr5", names [4]);
	}

	@Test
	public void testUserIdBarney()
	{
		assertEquals ("admin", session.getUserID());
	}

	@Test
	public void testNullUserId() throws RepositoryException
	{
		//noinspection EmptyClass
		Credentials creds = new Credentials() { };
		session = repo.login (creds);

		assertNull (session.getUserID());
	}

	@Test
	public void getRepositorty()
	{
		assertSame (repo, session.getRepository());
	}

	@Test(expected=UnsupportedRepositoryOperationException.class)
	public void addLockToken() throws Throwable
	{
		try {
			session.addLockToken ("foo");
		} catch (RuntimeException e) {
			throw e.getCause();
		}
	}

	@Test(expected=UnsupportedRepositoryOperationException.class)
	public void getLockTokens() throws Throwable
	{
		try {
			session.getLockTokens();
		} catch (RuntimeException e) {
			throw e.getCause();
		}
	}

	@Test(expected=UnsupportedRepositoryOperationException.class)
	public void removeLockToken() throws Throwable
	{
		try {
			session.removeLockToken ("foo");
		} catch (RuntimeException e) {
			throw e.getCause();
		}
	}

	@Test
	public void getAttribute()
	{
		assertNull (session.getAttribute ("foo"));
	}

	@Test
	public void getAttributeNames()
	{
		assertEquals (0, session.getAttributeNames().length);
	}

	@Test
	public void isLive()
	{
		assertEquals (true, session.isLive());
		session.logout();
		assertEquals (false, session.isLive());
	}

	@Test
	public void getValueFactory() throws RepositoryException
	{
		assertTrue (session.getValueFactory() instanceof ValueFactory);
	}
}
