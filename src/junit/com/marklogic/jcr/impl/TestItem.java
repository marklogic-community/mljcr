/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import com.marklogic.jcr.RepositoryFactory;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ItemNotFoundException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 3, 2008
 * Time: 4:27:29 PM
 */
public class TestItem
{
	Repository repo = null;
	Session session = null;
	Node root = null;

	@Before
	public void setup() throws URISyntaxException, RepositoryException
	{
		repo = RepositoryFactory.openRepository (new URI ("mockjcr://admin:admin@localhost:8080/testws"));
		session = repo.login();
		root = session.getRootNode();
	}

	@Test
	public void testAddChildNode() throws RepositoryException
	{
		Node child = root.addNode ("child");

		assertNotNull (child);
		assertEquals ("child", child.getName());
		assertEquals (1, child.getDepth());
		assertEquals ("/child", child.getPath());
		assertSame (root, child.getParent());
		assertSame (session, child.getSession());
	}

	@Test
	public void testDepth() throws RepositoryException
	{
		assertEquals (0, root.getDepth());
	}

	@Test(expected=ItemNotFoundException.class)
	public void testAncestorWayTooBig() throws RepositoryException
	{
		root.getAncestor (100);
	}

	@Test(expected=ItemNotFoundException.class)
	public void testAncestorTooBig() throws RepositoryException
	{
		root.getAncestor (1);
	}

	@Test(expected=ItemNotFoundException.class)
	public void testZeroAncestor() throws RepositoryException
	{
		root.getAncestor (-1);
	}

	@Test
	public void testGetAncestor() throws RepositoryException
	{
		assertSame (root, root.getAncestor (0));
	}

	@Test
	public void testSession() throws RepositoryException
	{
		assertSame (session, root.getSession());
	}

	@Test
	public void testIsNode() throws RepositoryException
	{
		assertTrue (root.isNode());
	}
}
