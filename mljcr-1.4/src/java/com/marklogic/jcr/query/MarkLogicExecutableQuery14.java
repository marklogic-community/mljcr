/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import com.marklogic.jcr.fs.MarkLogicFileSystem;

import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.ItemManager;

import javax.jcr.query.QueryResult;
import javax.jcr.query.Query;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.RepositoryException;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 4, 2009
 * Time: 7:05:57 PM
 */
public class MarkLogicExecutableQuery14 implements ExecutableQuery
{
	private static final Logger logger = Logger.getLogger (MarkLogicExecutableQuery14.class.getName());
//	private static final String DEFAULT_LOG_LEVEL = "FINE";
	private final Level logLevel;
	private final MarkLogicQueryBuilder14 queryBuilder;
	private final MarkLogicFileSystem mlfs;

	public MarkLogicExecutableQuery14 (SessionImpl session, ItemManager itemMgr, String statement,
                             String language, QueryRootNode root, MarkLogicFileSystem mlfs)
		throws InvalidQueryException
	{
		queryBuilder = new MarkLogicQueryBuilder14 (root, session, itemMgr, statement, language);
//		String levelName = System.getProperty ("mljcr.log.level", DEFAULT_LOG_LEVEL);
//		logLevel = Level.parse (levelName);
logLevel = Level.INFO;
		this.mlfs = mlfs;
	}

	// -------------------------------------------------------------
	// Implementation of ExecutableQuery interface (JackRabit 1.4)

	public QueryResult execute (long offset, long limit) throws RepositoryException
	{
		logger.log (logLevel, "Executing query: \n" + queryBuilder.getRootNode().dump());

		Query query = queryBuilder.createQuery (offset, limit, mlfs);

		return query.execute();
	}
}
