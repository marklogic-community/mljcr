/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;


import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 12, 2008
 * Time: 3:50:47 PM
 */
public class MLQuery implements Query
{
	private static final Logger log = Logger.getLogger (MLQuery.class.getName());
	private final String statement;
	private final String language;
	private final long offset;
	private final long limit;

	private StringBuffer xpathBuffer = new StringBuffer();
	private List propertySelectors = new ArrayList (5);

	public MLQuery (String statement, String language, long offset, long limit)
	{
		this.statement = statement;
		this.language = language;
		this.offset = offset;
		this.limit = limit;
	}

	// --------------------------------------------------------------

	private static final Level LOG_LEVEL = Level.INFO;

	private void log (String msg)
	{
		log.log (LOG_LEVEL, msg);
	}

	// --------------------------------------------------------------
	// ---------------------------------------------------------------

	void addNamedNodePathStep (String step, boolean descendants)
	{
		String name = (step.equals ("{}")) ? "" : step;

		if (descendants) xpathBuffer.append ("/");

		xpathBuffer.append ("/node[@name=\"").append (name).append (("\"]"));
	}

	void addAnyNodePathStep (boolean descendants)
	{
		if (descendants) xpathBuffer.append ("/");
		xpathBuffer.append ("/node");
	}

	public void addPropertyValuePredicate (String propName, String propValue)
	{
		xpathBuffer.append ("[property[@name = \"");
		xpathBuffer.append (propName);
		xpathBuffer.append ("\"]/values/value[. = \"");
		xpathBuffer.append (propValue);
		xpathBuffer.append ("\"]]");
	}

	void addPropertySelector (String s)
	{
		propertySelectors.add (s);
	}

	public void addPositionPredicate (int position)
	{
		xpathBuffer.append ("[").append (position).append ("]");
	}

	public void addPredicate (String pred)
	{
		xpathBuffer.append ("[").append (pred).append ("]");
	}

	// ---------------------------------------------------------------

	private void listify (List selectors, StringBuffer sb)
	{
		int size = selectors.size ();

		if (size != 0) sb.append ("/");
		if (size > 1) sb.append ("(");

		for (int i = 0; i < size; i++) {
			String selector = (String) selectors.get (i);
			if (i != 0) sb.append ("|");

			sb.append ("property[@name\"").append (selector).append ("\"]");
		}

		if (size > 1) sb.append (")");
	}

	String getXQuery ()
	{
		StringBuffer sb = new StringBuffer();

		sb.append (xpathBuffer);

		listify (propertySelectors, sb);

		return sb.toString();
	}

	// ---------------------------------------------------------------
	// Implementation of Query interface

	public QueryResult execute() throws RepositoryException
	{
		log ("ML Query String: " + getXQuery());

		throw new RepositoryException ("NOT YET IMPLEMENTED");
	}

	public String getStatement()
	{
		return statement;
	}

	public String getLanguage()
	{
		return language;
	}

	public String getStoredQueryPath()
		throws RepositoryException
	{
		throw new RepositoryException ("Not implemented for ML");
	}

	public Node storeAsNode (String string)
		throws RepositoryException
	{
		throw new RepositoryException ("Not implemented for ML");
	}
}
