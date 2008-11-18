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
import javax.jcr.query.Query;

import java.util.logging.Logger;

/**
 * Mark Logic-specific implementation of ExecutableQuery
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 11, 2008
 * Time: 3:07:43 PM
 */
public class MLExecutableQuery implements ExecutableQuery
{
	private static final Logger log = Logger.getLogger (MLExecutableQuery.class.getName());
	private final MLQueryBuilder queryBuilder;

	public MLExecutableQuery (SessionImpl session, ItemManager itemMgr, String statement,
		String language, QueryNodeFactory queryNodeFactory)
		throws InvalidQueryException
	{
		QueryRootNode root = QueryParser.parse (statement, language,
			session.getNamePathResolver(), queryNodeFactory);

		queryBuilder = new MLQueryBuilder (root, session, itemMgr, statement, language);
	}

	// -------------------------------------------------------------
	// Implementation of ExecutableQuery

	public QueryResult execute (long offset, long limit)
		throws RepositoryException
	{
		log.info ("Executing query: \n" + queryBuilder.getRootNode().dump());

		Query query = queryBuilder.createQuery (offset, limit);

		return query.execute();
	}
}
