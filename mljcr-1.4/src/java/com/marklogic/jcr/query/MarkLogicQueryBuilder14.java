/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import com.marklogic.jcr.fs.MarkLogicFileSystem;

import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.query.TextsearchQueryNode;
import org.apache.jackrabbit.core.query.LocationStepQueryNode;
import org.apache.jackrabbit.core.query.RelationQueryNode;
import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.Name;

import javax.jcr.Session;
import javax.jcr.query.Query;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 4, 2009
 * Time: 7:37:56 PM
 */
public class MarkLogicQueryBuilder14 extends MarkLogicQueryBuilder
{
	public MarkLogicQueryBuilder14 (QueryRootNode root, Session session,
		ItemManager itemMgr, String statement, String language)
	{
		super (root, session, itemMgr, statement, language);
	}

	// -------------------------------------------------------

	public Query createQuery (long offset, long limit, MarkLogicFileSystem mlfs)
	{
		MarkLogicQuery query = new MarkLogicQuery14 (statement, language, offset, limit, mlfs, session);

		return (Query) getRootNode().accept (this, query);
	}

	protected String locationStepNameTest (LocationStepQueryNode node)
	{
		Name nameTest = node.getNameTest();

		if (nameTest == null) return null;

		if ("".equals (nameTest.getNamespaceURI()) && "".equals (nameTest.getLocalName ()))
		{
			return "";
		}

		return nameTest.toString();
	}
	// -------------------------------------------------------

	public Object visit (QueryRootNode node, Object data)
	{
		MarkLogicQuery14 query = (MarkLogicQuery14) data;

		// These are the columns reported by the QueryResult
		query.addPropertySelectors (node.getSelectProperties());

		return visitRoot (node, data);
	}

	public Object visit (TextsearchQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		MarkLogicQuery query = (MarkLogicQuery) data;
		Path relPath = node.getRelativePath();
		// FIXME: This is probably broken for the general case
		String relPathStr = (node.getRelativePath() == null) ? "." : relPath.getString();

		query.addFullTextSearch (relPathStr, node.getQuery());

		return data;
	}

	// ---------------------------------------------------------

	protected String [] relationQueryPathToStrings (RelationQueryNode node)
	{
		Path.Element[] elements = node.getRelativePath().getElements();
		String [] strings = new String [elements.length];

		for (int i = 0; i < strings.length; i++) {
			strings [i] = elements [i].toString();
		}

		return strings;
	}
}
