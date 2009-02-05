/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;


import com.marklogic.jcr.fs.MarkLogicFileSystem;
import com.marklogic.jcr.fs.AbstractMLFileSystem;
import com.marklogic.jcr.persistence.AbstractPersistenceManager;

import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.query.OrderQueryNode;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * https://rootwiki.marklogic.com/JSPWiki/Wiki.jsp?page=WhatIsReST
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 12, 2008
 * Time: 3:50:47 PM
 */
abstract class AbstractQuery implements Query
{
	private static final Logger logger = Logger.getLogger (AbstractQuery.class.getName ());
//	private static final String DEFAULT_LOG_LEVEL = "FINE";
	private final String statement;
	private final String language;
	private final long offset;
	private final long limit;

	private final Level logLevel;
	private final MarkLogicFileSystem mlfs;
	private final Session session;
	// TODO: Handle this properly in result and as query constraint
	private String [] propertySelectors = new String [0];
	private final List orderSpecs = new ArrayList (5);
	private StringBuffer xpathBuffer = new StringBuffer();

	public AbstractQuery (String statement, String language, long offset, long limit, MarkLogicFileSystem mlfs, Session session)
	{
		this.statement = statement;
		this.language = language;
		this.offset = offset;
		this.limit = limit;
		this.mlfs = mlfs;
		this.session = session;

//		String levelName = System.getProperty ("mljcr.log.level", DEFAULT_LOG_LEVEL);
//		logLevel = Level.parse (levelName);
logLevel = Level.INFO;
	}

	// --------------------------------------------------------------
	// ---------------------------------------------------------------
	// methods used only by subclasses

	protected void addPropertySelectors (String [] names)
	{
		propertySelectors = names;
	}

	// ---------------------------------------------------------------

	void addNamedNodePathStep (String step, boolean descendants)
	{
		String name = (step.equals ("{}")) ? "" : step;

		if (descendants) {
			xpathBuffer.append ("/");
		}

		xpathBuffer.append ("/(node[@name=\"").append (name).append (("\"])"));
	}

	void addAnyNodePathStep (boolean descendants)
	{
		if (descendants) {
			xpathBuffer.append ("/");
		}

		xpathBuffer.append ("/node");
	}

	public void addPropertyStepPath(String path)
	{
		xpathBuffer.append ("/").append (path);
	}

	public void addPropertyValuePredicate (String propName, String propValue)
	{
 		xpathBuffer.append ("[property[@name = \"");
		xpathBuffer.append (propName);
		xpathBuffer.append ("\"]/values/value[. = \"");
		xpathBuffer.append (propValue);
		xpathBuffer.append ("\"]]");
	}

	public void addPositionPredicate (int position)
	{
		xpathBuffer.append ("[").append (position).append ("]");
	}

	public void addPredicate (String pred)
	{
		xpathBuffer.append ("[").append (pred).append ("]");
	}

	public void addOrderBySpec (OrderQueryNode.OrderSpec orderspec)
	{
		orderSpecs.add (orderspec);
	}

	protected void addPropertyValueTest (String [] pathElements,
		String opString, String value, String operand, String functionName)
	{
		xpathBuffer.append ("[");

		if (functionName != null) {
			xpathBuffer.append (functionName).append ("(");
		}

		for (int i = 0; i < pathElements.length; i++) {
			if (i != 0) {
				xpathBuffer.append ("/");
			}

			if (i == pathElements.length - 1) {
				xpathBuffer.append ("property[@name=\"").append (pathElements [i]).append ("\"]");
			} else {
				xpathBuffer.append (pathElements [i]);
			}
		}

		xpathBuffer.append ("/values/value");

		if (functionName == null) {
			xpathBuffer.append ("[").append (value);
			xpathBuffer.append (opString).append (" ");
			xpathBuffer.append (operand);
			xpathBuffer.append ("]");
		} else {
			xpathBuffer.append (")");
		}

		xpathBuffer.append ("]");
	}

	public void addFullTextSearch (String relPathStr, String rawQuery)
	{
		TextQueryParser textQuery = new TextQueryParser (rawQuery);
		String posTest = textQuery.getPositiveTest();
		String negTest = textQuery.getNegativeTest();

		if (posTest != null) {
			logger.log (logLevel, "positive test: " + posTest);
			addPredicate ("cts:contains(" + relPathStr + "/property/values, " + posTest + ")");
		}

		if (negTest != null) {
			logger.log (logLevel, "negative test: " + negTest);
			addPredicate ("fn:not(cts:contains(" + relPathStr + "/property/values, " + negTest + "))");
		}

	}

	// ---------------------------------------------------------------

//	private void insertPropertyRelations (List relations, StringBuffer sb)
//	{
//		for (Iterator it = relations.iterator (); it.hasNext ();) {
//			String s = (String) it.next ();
//			sb.append ("[property[@name=\"").append (s).append ("\"]]");
//		}
//	}

	private void insertOrderSpecs (List orderSpecs, StringBuffer sb)
	{
		boolean notFirst = false;
		if (orderSpecs.size() == 0) return;

		sb.append ("order by");

		for (Iterator it = orderSpecs.iterator(); it.hasNext();) {
			OrderQueryNode.OrderSpec orderSpec = (OrderQueryNode.OrderSpec) it.next ();

			if (notFirst) {
				sb.append (",");
			} else {
				notFirst = true;
			}

			sb.append (" $node/property[@name=\"");
			sb.append (orderSpec.getProperty().toString());
			sb.append ("\"]/values/value ");
			sb.append ((orderSpec.isAscending ()) ? "ascending" : "descending");
		}

		sb.append ("\n");
	}

	String getXQuery()
	{
		StringBuffer sb = new StringBuffer();

		sb.append ("for $node in ");
		sb.append ("fn:doc (\"").append (AbstractMLFileSystem.URI_PLACEHOLDER).append ("\")");
		sb.append ("/workspace");

		sb.append (xpathBuffer);

//		insertPropertyRelations (propertySelectors, sb);

		sb.append ("\n");

		insertOrderSpecs (orderSpecs, sb);

		sb.append ("return $node/@uuid\n");

		return sb.toString ();
	}

	// ---------------------------------------------------------------
	// Implementation of Query interface

	public QueryResult execute() throws RepositoryException
	{
		String xqry = "xquery version '1.0-ml';\n" +
			"declare namespace mljcr = 'http://marklogic.com/jcr'; \n" +
			"declare default element namespace 'http://marklogic.com/jcr'; \n" +
			getXQuery() + "\n";

		logger.log (logLevel, "ML Query String: \n" + xqry);

		try {
			String [] resultUUIDs = mlfs.runQuery (AbstractPersistenceManager.WORKSPACE_DOC_NAME, xqry);

logger.log (logLevel, "resultUUIDS size: " + resultUUIDs.length);
for (int i = 0; i < resultUUIDs.length; i++) {
	System.out.println (resultUUIDs[i]);
}
			return new QueryResultImpl (session, resultUUIDs);
		} catch (FileSystemException e) {
			throw new RepositoryException ("unable to runQuery()", e);
		}
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
