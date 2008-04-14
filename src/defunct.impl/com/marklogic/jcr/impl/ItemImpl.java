/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.ItemVisitor;
import javax.jcr.ItemNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 2, 2008
 * Time: 8:02:02 PM
 */
public abstract class ItemImpl implements Item
{
	private final Session session;
	private final Node parent;
	private final String name;
	private final String path;
	private final int depth;

	// ------------------------------------------------------------

	public ItemImpl (Session session, Node parent, String name, String path, int depth)
	{
		this.session = session;
		this.parent = parent;
		this.name = name;
		this.path = path;
		this.depth = depth;
	}

	// ------------------------------------------------------------
	// Implementation of Item interface

	public abstract boolean isNode();

	public String getPath() throws RepositoryException
	{
		return path;
	}

	public String getName() throws RepositoryException
	{
		return name;
	}

	public Item getAncestor (int depth) throws RepositoryException
	{
		if ((depth == 0) && (parent == null)) {
			return this;
		}

		throw new ItemNotFoundException ("No ancestor found for depth " + 0);
	}

	public Node getParent() throws RepositoryException
	{
		return parent;
	}

	public int getDepth() throws RepositoryException
	{
		return depth;
	}

	public Session getSession() throws RepositoryException
	{
		return session;
	}

	public boolean isNew()
	{
		return false;  // FIXME: auto-generated
	}

	public boolean isModified()
	{
		return false;  // FIXME: auto-generated
	}

	public boolean isSame (Item otherItem) throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public void accept (ItemVisitor visitor) throws RepositoryException
	{
		// FIXME: auto-generated
	}

	public void save()
	{
		// FIXME: auto-generated
	}

	public void refresh (boolean keepChanges)
	{
		// FIXME: auto-generated
	}

	public void remove()
	{
		// FIXME: auto-generated
	}
}
