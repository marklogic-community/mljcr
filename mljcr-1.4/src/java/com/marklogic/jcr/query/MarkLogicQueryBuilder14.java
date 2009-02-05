/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import com.marklogic.jcr.fs.MarkLogicFileSystem;

import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.query.TextsearchQueryNode;
import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.spi.Path;

import javax.jcr.Session;
import javax.jcr.query.Query;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 4, 2009
 * Time: 7:37:56 PM
 */
public class MarkLogicQueryBuilder14 extends MLQueryBuilder
{
	public MarkLogicQueryBuilder14 (QueryRootNode root, Session session,
		ItemManager itemMgr, String statement, String language)
	{
		super (root, session, itemMgr, statement, language);
	}

	// -------------------------------------------------------

	public Query createQuery (long offset, long limit, MarkLogicFileSystem mlfs)
	{
		MLQuery query = new MarkLogicQuery14 (statement, language, offset, limit, mlfs, session);

		return (Query) getRootNode().accept (this, query);
	}

	// -------------------------------------------------------

	public Object visit (QueryRootNode node, Object data)
	{
		MarkLogicQuery14 query = (MarkLogicQuery14) data;

		// These are the columns reported by the QueryResult
		query.addPropertySelectors (node.getSelectProperties());

		return super.visit (node, data);
	}

	public Object visit (TextsearchQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		MLQuery query = (MLQuery) data;
		Path relPath = node.getRelativePath();
		// FIXME: This is probably broken for the general case
		String relPathStr = (node.getRelativePath() == null) ? "." : relPath.getString();

		query.addFullTextSearch (relPathStr, node.getQuery());

		return data;
	}
}
