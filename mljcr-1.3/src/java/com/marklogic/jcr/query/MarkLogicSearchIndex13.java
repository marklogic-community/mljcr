/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.query.QueryParser;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.ItemManager;

import javax.jcr.query.InvalidQueryException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 20, 2008
 * Time: 1:07:06 PM
 */
public class MarkLogicSearchIndex13 extends MarkLogicSearchIndex
{
	public ExecutableQuery createExecutableQuery (SessionImpl session,
		ItemManager itemMgr, String statement, String language)
		throws InvalidQueryException
	{
		log.info ("lang=" + language + ", stmt=" + statement);

		QueryRootNode root = QueryParser.parse (statement, language,
			session.getNamespaceResolver());

		return new MarkLogicExecutableQuery13 (session, itemMgr, statement, language, root, mlfs);
	}
}
