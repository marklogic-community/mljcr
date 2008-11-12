/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryParser;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.query.QueryNodeFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.InvalidQueryException;

import java.util.logging.Logger;

/**
 * Mark Logic-specific implementation of ExecutableQuery
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 11, 2008
 * Time: 3:07:43 PM
 */
public class QueryImpl implements ExecutableQuery
{
	private static final Logger log = Logger.getLogger (QueryImpl.class.getName());
	private final QueryRootNode root;
	private final ItemManager itemMgr;

	public QueryImpl (SessionImpl session, ItemManager itemMgr, String statement,
		String language, QueryNodeFactory queryNodeFactory)
		throws InvalidQueryException
	{
		this.itemMgr = itemMgr;

		this.root = QueryParser.parse (statement, language,
			session.getNamePathResolver(), queryNodeFactory);
	}

	public QueryResult execute (long offset, long limit)
		throws RepositoryException
	{
		log.info ("Executing query: \n" + root.dump());

		QueryBuilder builder = new QueryBuilder (root);

		builder.createQuery();

		throw new RepositoryException ("NOT YET IMPLEMENTED");
	}
}
