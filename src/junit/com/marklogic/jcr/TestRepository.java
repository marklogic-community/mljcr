/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr;

import com.marklogic.jcr.mock.MockRepository;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import static javax.jcr.Repository.*;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 1:56:51 PM
 */
public class TestRepository
{
	private final String [] requiredKeys = {
		 SPEC_VERSION_DESC,
		 SPEC_NAME_DESC,
		 REP_VENDOR_DESC,
		 REP_VENDOR_URL_DESC,
		 REP_NAME_DESC,
		 REP_VERSION_DESC,
		 LEVEL_1_SUPPORTED,
		 LEVEL_2_SUPPORTED,
		 OPTION_TRANSACTIONS_SUPPORTED,
		 OPTION_VERSIONING_SUPPORTED,
		 OPTION_OBSERVATION_SUPPORTED,
		 OPTION_LOCKING_SUPPORTED,
		 OPTION_QUERY_SQL_SUPPORTED,
		 QUERY_XPATH_POS_INDEX,
		 QUERY_XPATH_DOC_ORDER,
	};

	// --------------------------------------------------------------

	@Test
	public void testLoginMissingAtSign() throws URISyntaxException, RepositoryException
	{
		URI uri = new URI ("mockjcr://fred:localhost:9000");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);

		Session session = repo.login ("foobar");

		assertNull (session.getUserID());
		assertEquals ("foobar", session.getWorkspace().getName());
	}

	@Test
	public void testLoginEmptyPasswd() throws URISyntaxException, RepositoryException
	{
		URI uri = new URI ("mockjcr://fred:@localhost:9000");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);

		Session session = repo.login ("foobar");

		assertEquals ("fred", session.getUserID());
		assertEquals ("foobar", session.getWorkspace().getName());
	}

	@Test
	public void testLoginNoPasswd() throws URISyntaxException, RepositoryException
	{
		URI uri = new URI ("mockjcr://fred@localhost:9000");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);

		Session session = repo.login ("foobar");

		assertEquals ("fred", session.getUserID());
		assertEquals ("foobar", session.getWorkspace().getName());
	}

	@Test
	public void testLoginWithName() throws URISyntaxException, RepositoryException
	{
		URI uri = new URI ("mockjcr://fred:blah@localhost:9000");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);

		Session session = repo.login ("foobar");

		assertEquals ("fred", session.getUserID());
		assertEquals ("foobar", session.getWorkspace().getName());
	}

	@Test
	public void testLoginWithCredsAndName() throws URISyntaxException, RepositoryException
	{
		URI uri = new URI ("mockjcr://localhost:9000");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);
		Credentials credentials = new SimpleCredentials ("fred", "hush".toCharArray());

		Session session = repo.login (credentials, "foobar");

		assertEquals ("fred", session.getUserID());
		assertEquals ("foobar", session.getWorkspace().getName());
	}

	@Test
	public void testLoginWithCreds() throws URISyntaxException, RepositoryException
	{
		URI uri = new URI ("mockjcr://localhost:9000/test");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);
		Credentials credentials = new SimpleCredentials ("fred", "hush".toCharArray());

		Session session = repo.login (credentials);

		assertEquals ("fred", session.getUserID());
		assertEquals ("test", session.getWorkspace().getName());
	}

	@Test
	public void testURIFactory() throws URISyntaxException
	{
		URI uri = new URI ("mockjcr://user:pass@localhost:9000/test");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);

		assertNotNull (repo);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFactoryBadScheme() throws URISyntaxException
	{
		URI uri = new URI ("foobar://user:pass@localhost:9000/test");
		com.marklogic.jcr.RepositoryFactory.openRepository (uri);
	}

	@Test
	public void testURIMockFactory() throws URISyntaxException, RepositoryException
	{
		URI uri = new URI ("mockjcr://fred:hush@localhost:9000/test");
		Repository repo = com.marklogic.jcr.RepositoryFactory.openRepository (uri);

		assertTrue (repo instanceof MockRepository);
		assertEquals ("mock", repo.getDescriptor (REP_VERSION_DESC));

		Session session = repo.login();
		assertNotNull (session);

		assertEquals ("fred", session.getUserID());
		assertSame (repo, session.getRepository());

		Workspace workspace = session.getWorkspace();
		assertNotNull (workspace);

		assertEquals ("test", workspace.getName());
		assertSame (session, workspace.getSession());
		assertSame (workspace, session.getWorkspace());

		assertEquals (1, workspace.getAccessibleWorkspaceNames().length);
		assertEquals ("test", workspace.getAccessibleWorkspaceNames()[0]);
	}

	@Test
	public void testGetDescriptor() throws URISyntaxException
	{
		Repository rep = RepositoryFactory.openRepository (new URI ("mljcr://localhost:8080/"));

		assertNull (rep.getDescriptor ("xxFOOxx"));

		for (int i = 0; i < requiredKeys.length; i++) {
			assertNotNull (rep.getDescriptor (requiredKeys [i]));
		}

		assertEquals ("1.0", rep.getDescriptor (SPEC_VERSION_DESC));
		assertEquals ("Mark Logic Corporation", rep.getDescriptor (REP_VENDOR_DESC));
		assertEquals ("http://marklogic.com/", rep.getDescriptor (REP_VENDOR_URL_DESC));
		assertEquals ("4.0-1", rep.getDescriptor (REP_VERSION_DESC));
	}

	@Test
	public void testGetDescriptorsKeys() throws URISyntaxException
	{
		Repository rep = RepositoryFactory.openRepository (new URI ("mljcr://localhost:8080/"));

		String [] keys = rep.getDescriptorKeys();

		assertEquals (requiredKeys.length, keys.length);

		for (int i = 0; i < requiredKeys.length; i++) {
			String key = requiredKeys[i];

			assertTrue (arrayContainsKey (keys, key));
		}
	}

	// --------------------------------------------------------------

	private boolean arrayContainsKey (String[] keys, String key)
	{
		for (int i = 0; i < keys.length; i++) {

			if (key.equals (keys [i])) {
				return true;
			}
		}

		return false;
	}
}
