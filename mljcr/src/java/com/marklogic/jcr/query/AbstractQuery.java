/*
 *  Copyright (c) 2009,  Mark Logic Corporation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  The use of the Apache License does not indicate that this project is
 *  affiliated with the Apache Software Foundation.
 */

package com.marklogic.jcr.query;

import com.marklogic.jcr.fs.MarkLogicFileSystem;
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
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 12, 2008
 * Time: 3:50:47 PM
 * @noinspection ClassWithTooManyFields,ClassWithTooManyMethods
 */
abstract class AbstractQuery implements Query
{
	private static final String STATE_DOC_VAR_NAME = "state-doc-uri";

	private static final Logger logger = Logger.getLogger (AbstractQuery.class.getName());
	private static final String DEFAULT_LOG_LEVEL = "FINE";
	private final String statement;
	private final String language;
	private final long offset;
	private final long limit;

	private final Level logLevel;
	private final MarkLogicFileSystem mlfs;
	private final Session session;
	private String [] propertySelectors = new String [0];
	private final List orderSpecs = new ArrayList (5);
	private final List textQueries = new ArrayList (5);
	private final LinkedList pendingPredicateContexts = new LinkedList();
	private StringBuffer xpathBuffer = new StringBuffer();

	public AbstractQuery (String statement, String language,
		long offset, long limit, MarkLogicFileSystem mlfs, Session session)
	{
		this.statement = statement;
		this.language = language;
		this.offset = offset;
		this.limit = limit;
		this.mlfs = mlfs;
		this.session = session;

		String levelName = System.getProperty ("mljcr.log.level", DEFAULT_LOG_LEVEL);
		logLevel = Level.parse (levelName);
	}

	public AbstractQuery (String statement, String language,
		MarkLogicFileSystem mlfs, Session session)
	{
		this (statement, language, 0, Long.MAX_VALUE, mlfs, session);
	}

	// --------------------------------------------------------------
	// ---------------------------------------------------------------
	// methods used only by subclasses

	protected void addPropertySelectors (String [] names)
	{
		propertySelectors = names;
	}

	// ---------------------------------------------------------------

	private void handlePredicate (String pred)
	{
		if (pendingPredicateContexts.size() == 0) {
			xpathBuffer.append ("[");
			xpathBuffer.append (pred);
			xpathBuffer.append ("]");
			return;
		}

		((LinkedList) pendingPredicateContexts.getLast()).add (pred);
	}

	public void pushPredicateLevel()
	{
		pendingPredicateContexts.add (new LinkedList());
	}

	public void popPredicateLevel (String prefix, String sep, String suffix)
	{
		LinkedList preds = (LinkedList) pendingPredicateContexts.removeLast();
		StringBuffer sb = new StringBuffer();
		boolean notFirst = false;

		sb.append (prefix);

		for (Iterator it = preds.iterator(); it.hasNext();) {
			String pred = (String) it.next();

			if (notFirst) {
				sb.append (sep);
			} else {
				notFirst = true;
			}

			sb.append (pred);
		}

		sb.append (suffix);

		handlePredicate (sb.toString());
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

	public void addPropertyValuePredicate (String propName, String propValue, String op)
	{
		StringBuffer sb = new StringBuffer();

 		sb.append ("property[@name = \"");
		sb.append (propName);
		sb.append ("\"]/values/value[. ");
		sb.append (op);
		sb.append (" \"");
		sb.append (propValue);
		sb.append ("\"]");

		handlePredicate (sb.toString());
	}

	public void addPositionPredicate (int position)
	{
		handlePredicate ("" + position);
	}

	public void addPredicate (String pred)
	{
		handlePredicate (pred);
	}

	public void addOrderBySpec (OrderQueryNode.OrderSpec orderspec)
	{
		orderSpecs.add (orderspec);
	}

	protected void addPropertyValueTest (String [] pathElements,
		String opString, String value, String operand, String functionName)
	{
		StringBuffer sb = new StringBuffer();

		if (functionName != null) {
			sb.append (functionName).append ("(");
		}

		for (int i = 0; i < pathElements.length; i++) {
			if (i != 0) {
				sb.append ("/");
			}

			if (i == pathElements.length - 1) {
				sb.append ("property[@name=\"").append (pathElements [i]).append ("\"]");
			} else {
				sb.append (pathElements [i]);
			}
		}

		sb.append ("/values/value");

		if (functionName == null) {
			sb.append ("[").append (value);
			sb.append (opString).append (" ");
			sb.append (operand);
			sb.append ("]");
		} else {
			sb.append (")");
		}

		handlePredicate (sb.toString());
	}

	private static final String TEXT_SEARCH_FUNC_ROOT = "local:textContains";

	public void addFullTextSearch (String relPathStr, String rawQuery)
	{
		String functionName = TEXT_SEARCH_FUNC_ROOT + textQueries.size();
		String predicateFunctionCall = functionName + "(" + relPathStr + ")";
		TextQueryParser textQuery = new TextQueryParser (rawQuery, functionName);

		textQueries.add (textQuery);

		handlePredicate (predicateFunctionCall);
	}

	// ---------------------------------------------------------------

	private void generateTextFunctions (StringBuffer sb)
	{
		if (textQueries.size () == 0) return;

		sb.append ("\ndeclare function local:doc-node ($prop as element(property)) as node()\n{\n");
		sb.append ("	let $node-uri as xs:string := xdmp:node-uri ($prop)\n");
		sb.append ("	let $uri-root as xs:string := fn:substring-before ($node-uri, '/state.xml')\n");
		sb.append ("	let $uri := fn:concat ($uri-root, fn:string ($prop/values/value))\n");
		sb.append ("	return fn:doc ($uri)/node()\n");
		sb.append ("};\n");

		for (Iterator it = textQueries.iterator(); it.hasNext();) {
			TextQueryParser textQuery = (TextQueryParser) it.next();
			String posTest = textQuery.getPositiveTest();
			String negTest = textQuery.getNegativeTest();

			sb.append ("\ndeclare function ");
			sb.append (textQuery.getFunctionName());
			sb.append (" ($node as element (node)) as xs:boolean\n{\n");

			String nodesExpr =
				"cts:contains (\n" +
				"		for $prop in $node/property return" +
				" if ($prop/@type eq \"Binary\") then local:doc-node ($prop) else $prop/values,\n";

			if (posTest != null) {
				sb.append ("	").append (nodesExpr);
				sb.append ("		").append (posTest).append (")");
			}

			if ((posTest != null) && (negTest != null)) {
				sb.append ("\n	and\n");
			}

			if (negTest != null) {
				sb.append ("	fn:not (");
				sb.append (nodesExpr);
				sb.append ("		").append (negTest).append ("))");
			}

			sb.append ("\n};\n\n");
		}
	}

	// ---------------------------------------------------------------

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

	private boolean hasRange()
	{
		return (offset != 0) && (limit != 0) && (limit != Long.MAX_VALUE)
			&& (limit > offset);
	}

	String getXQuery()
	{
		boolean range = hasRange();
		StringBuffer sb = new StringBuffer();

		generateTextFunctions (sb);

		if (range) {
			sb.append ("(\n");
		}

		sb.append ("for $node in ");
		sb.append ("fn:doc ($").append (STATE_DOC_VAR_NAME).append (")");
		sb.append ("/workspace");

		sb.append (xpathBuffer);

		sb.append ("\n");

		insertOrderSpecs (orderSpecs, sb);

		sb.append ("return $node/@uuid\n");

		if (range) {
			sb.append (")[").append (offset).append (" to ").append (limit).append ("]\n");
		}

		return sb.toString();
	}

	// ---------------------------------------------------------------
	// Implementation of Query interface

	public QueryResult execute() throws RepositoryException
	{
		String xqry = "xquery version '1.0-ml';\n" +
			"declare namespace mljcr = 'http://marklogic.com/jcr'; \n" +
			"declare default element namespace 'http://marklogic.com/jcr'; \n" +
			"declare variable $" + STATE_DOC_VAR_NAME + " as xs:string external;\n\n" +
			getXQuery() + "\n";

		logger.log (logLevel, "ML Query String: \n" + xqry);

		try {
			String [] resultUUIDs = mlfs.runQuery (
				AbstractPersistenceManager.WORKSPACE_DOC_NAME, STATE_DOC_VAR_NAME, xqry);

			return new QueryResultImpl (session, propertySelectors, resultUUIDs);
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
