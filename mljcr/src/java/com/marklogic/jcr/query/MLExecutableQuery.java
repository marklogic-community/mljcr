/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryRootNode;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.marklogic.jcr.fs.MarkLogicFileSystem;

/**
 * Mark Logic-specific implementation of ExecutableQuery
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 11, 2008
 * Time: 3:07:43 PM
 */
public class MLExecutableQuery implements ExecutableQuery
{
	private static final Logger logger = Logger.getLogger (MLExecutableQuery.class.getName());
	private static final String DEFAULT_LOG_LEVEL = "FINE";
	private final Level logLevel;
	private final MLQueryBuilder queryBuilder;
    private final MarkLogicFileSystem mlfs;

	public MLExecutableQuery(SessionImpl session, ItemManager itemMgr, String statement,
                             String language, QueryRootNode root, MarkLogicFileSystem mlfs)
		throws InvalidQueryException
	{
		queryBuilder = new MLQueryBuilder (root, session, itemMgr, statement, language);
		String levelName = System.getProperty ("mljcr.log.level", DEFAULT_LOG_LEVEL);
		logLevel = Level.parse (levelName);
        this.mlfs = mlfs;
	}

	// -------------------------------------------------------------
	// Implementation of ExecutableQuery

	public QueryResult execute (long offset, long limit)
		throws RepositoryException
	{
		logger.log (logLevel, "Executing query: \n" + queryBuilder.getRootNode().dump());

		Query query = queryBuilder.createQuery (offset, limit, mlfs);

		return query.execute();
	}
}
