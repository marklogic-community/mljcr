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
import org.apache.jackrabbit.name.Path;
import org.apache.jackrabbit.name.QName;

import javax.jcr.Session;
import javax.jcr.query.Query;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 4, 2009
 * Time: 7:36:08 PM
 */
public class MarkLogicQueryBuilder13 extends MarkLogicQueryBuilder
{
	public MarkLogicQueryBuilder13 (QueryRootNode root, Session session,
		ItemManager itemMgr, String statement, String language)
	{
		super (root, session, itemMgr, statement, language);
	}

	// ---------------------------------------------------------

	public Query createQuery (MarkLogicFileSystem mlfs)
	{
		AbstractQuery query = new MarkLogicQuery13 (statement, language, mlfs, session);

		return (Query) getRootNode().accept (this, query);
	}

	protected String locationStepNameTest (LocationStepQueryNode node)
	{
		QName nameTest = node.getNameTest();

		if (nameTest == null) return null;

		if ("".equals (nameTest.getNamespaceURI()) && "".equals (nameTest.getLocalName()))
		{
			return "";
		}

		return nameTest.toString();
	}

	// ---------------------------------------------------------

	public Object visit (QueryRootNode node, Object data)
	{
		MarkLogicQuery13 query = (MarkLogicQuery13) data;

		// These are the columns reported by the QueryResult
		query.addPropertySelectors (node.getSelectProperties());

		return visitRoot (node, data);
	}

	public Object visit (TextsearchQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		MarkLogicQuery13 query = (MarkLogicQuery13) data;
		Path relPath = node.getRelativePath();
		// FIXME: This is probably broken for the general case
		String relPathStr = (relPath == null) ? "." : relPath.toString();	// TODO: Check this

		query.addFullTextSearch (relPathStr, node.getQuery());

		return data;
	}

	// ---------------------------------------------------------

	protected String [] relationQueryPathToStrings (RelationQueryNode node)
	{
		Path.PathElement[] elements = node.getRelativePath().getElements();
		String [] strings = new String [elements.length];

		for (int i = 0; i < strings.length; i++) {
			strings [i] = elements [i].toString();
		}

		return strings;
	}
}
