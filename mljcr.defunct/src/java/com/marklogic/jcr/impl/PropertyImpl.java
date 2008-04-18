/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import javax.jcr.Property;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.RepositoryException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 2, 2008
 * Time: 8:06:32 PM
 */
public class PropertyImpl extends ItemImpl implements Property
{
	public PropertyImpl (Session session, Node parent, String name, String path)
	{
		super (session, parent, name, path, 0);
	}

	// -------------------------------------------------------
	// Implementation of Property interface

	public boolean isNode()
	{
		return false;
	}

	public void setValue (Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setValue (Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public Value getValue () throws ValueFormatException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Value[] getValues () throws ValueFormatException, RepositoryException
	{
		return new Value[0];  // FIXME: auto-generated
	}

	public String getString () throws ValueFormatException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public InputStream getStream () throws ValueFormatException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public long getLong () throws ValueFormatException, RepositoryException
	{
		return 0;  // FIXME: auto-generated
	}

	public double getDouble () throws ValueFormatException, RepositoryException
	{
		return 0;  // FIXME: auto-generated
	}

	public Calendar getDate () throws ValueFormatException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public boolean getBoolean () throws ValueFormatException, RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public Node getNode () throws ValueFormatException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public long getLength () throws ValueFormatException, RepositoryException
	{
		return 0;  // FIXME: auto-generated
	}

	public long[] getLengths () throws ValueFormatException, RepositoryException
	{
		return new long[0];  // FIXME: auto-generated
	}

	public PropertyDefinition getDefinition () throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public int getType () throws RepositoryException
	{
		return 0;  // FIXME: auto-generated
	}
}
