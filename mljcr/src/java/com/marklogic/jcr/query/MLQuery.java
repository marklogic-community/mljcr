/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;


import com.marklogic.jcr.fs.MarkLogicFileSystem;
import com.marklogic.jcr.fs.AbstractMLFileSystem;
import com.marklogic.jcr.persistence.AbstractPersistenceManager;

import org.apache.jackrabbit.core.fs.FileSystemException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * https://rootwiki.marklogic.com/JSPWiki/Wiki.jsp?page=WhatIsReST
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 12, 2008
 * Time: 3:50:47 PM
 */
public class MLQuery implements Query
{
	private static final Logger logger = Logger.getLogger (MLQuery.class.getName ());
	private static final String DEFAULT_LOG_LEVEL = "FINE";
	private final String statement;
	private final String language;
	private final long offset;
	private final long limit;

	private final Level logLevel;
	private final MarkLogicFileSystem mlfs;
	private final Session session;

	private StringBuffer xpathBuffer = new StringBuffer ();
	private List propertySelectors = new ArrayList (5);
    private boolean debug = false;

	public MLQuery(String statement, String language, long offset, long limit, MarkLogicFileSystem mlfs, Session session)
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

	void addNamedNodePathStep (String step, boolean descendants, boolean hasPredicates)
	{
		String name = (step.equals ("{}")) ? "" : step;

		if (descendants) {
			xpathBuffer.append ("/");
		}

		xpathBuffer.append ((hasPredicates) ? "/(" : "/");
		xpathBuffer.append ("node[@name=\"").append (name).append (("\"]"));
		if (hasPredicates) xpathBuffer.append (")");
	}

	void addAnyNodePathStep (boolean descendants)
	{
		if (descendants) {
			xpathBuffer.append ("/");
		}

		xpathBuffer.append ("/node");

       // returns "//node"
       // String x = xpathBuffer.toString();
       // System.out.println("BUFFER AT aadAnyNodePathStep: "+x);
	}

	public void addPropertyStepPath(String path)
	{
		xpathBuffer.append ("/").append (path);
	}

	public void addPropertyValuePredicate (String propName, String propValue)
	{
        if(debug){
            System.out.println("addPropertyValuePredicate PROP NAME"+propName);
            System.out.println("addPropertyValuePredicate PROP VALUE"+ propValue);
        }
		xpathBuffer.append ("[property[@name = \"");
		xpathBuffer.append (propName);
		xpathBuffer.append ("\"]/values/value[. = \"");
		xpathBuffer.append (propValue);
		xpathBuffer.append ("\"]]");

        if(debug)
            System.out.println("BUFFER IN addPropertyValuePredicate "+xpathBuffer.toString());
	}

	void addPropertySelector (String s)
	{
		propertySelectors.add (s);
	}

	public void addPositionPredicate (int position)
	{
		xpathBuffer.append ("[").append (position).append ("]");

        String x = xpathBuffer.toString();
        if(debug)
            System.out.println("BUFFER AT addPositionPredicate "+x);
	}

	public void addPredicate (String pred)
	{
		xpathBuffer.append ("[").append (pred).append ("]");
        if(debug)
            System.out.println("IN ADDPREDICATE "+xpathBuffer.toString());
	}

	// ---------------------------------------------------------------

	private void listify (List selectors, StringBuffer sb)
	{
		int size = selectors.size ();

		if (size != 0) {
			sb.append ("/");
		}
		if (size > 1) {
			sb.append ("(");
		}

		for (int i = 0; i < size; i++) {
			String selector = (String) selectors.get (i);
			if (i != 0) {
				sb.append ("|");
			}

			sb.append ("node/property[@name=\"").append (selector).append ("\"]");
		}

		if (size > 1) {
			sb.append (")");
		}
	}

	String getXQuery ()
	{
		StringBuffer sb = new StringBuffer();

		sb.append ("/workspace");

		sb.append (xpathBuffer);

		listify (propertySelectors, sb);

		sb.append ("/@uuid");

		return sb.toString ();
	}

	// ---------------------------------------------------------------
	// Implementation of Query interface

	public QueryResult execute() throws RepositoryException
	{
		//dummy query until getXQuery() is sorted out
		String xqry = "xquery version '1.0-ml';\n" +
			"declare namespace mljcr = 'http://marklogic.com/jcr'; \n" +
			"declare default element namespace 'http://marklogic.com/jcr'; \n" +
			"fn:doc ('" + AbstractMLFileSystem.URI_PLACEHOLDER + "')" +
			getXQuery();

		logger.log (logLevel, "ML Query String: \n" + xqry);


        //System.out.println("THE QUERY: "+xqry);
		//write xquery that generates ids (state.xml)  //@uuid  551b7712-69f4-4f2b-9ad6-51051464f4fe
		//query result with sequence of strings
		//implementation of next node, takes id, and queries for node (Go Look at QueryResultImpl.NextNode()

		try {
//			String [] resultUUIDs = mlfs.runQuery (AbstractPersistenceManager.WORKSPACE_DOC_NAME, getXQuery());
			String [] resultUUIDs = mlfs.runQuery (AbstractPersistenceManager.WORKSPACE_DOC_NAME, xqry);
logger.log (logLevel, "resultUUIDS size: " + resultUUIDs.length);
			return new QueryResultImpl (session, resultUUIDs);
		} catch (FileSystemException e) {
			throw new RepositoryException ("unable to runQuery()", e);
		}
	}

	public String getStatement ()
	{
		return statement;
	}

	public String getLanguage ()
	{
		return language;
	}

	public String getStoredQueryPath ()
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
