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

import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.SessionImpl;

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
	private static final String DEFAULT_LOG_LEVEL = "FINE";
	private final Level logLevel;
	private final MarkLogicQueryBuilder14 queryBuilder;
	private final MarkLogicFileSystem mlfs;

	public MarkLogicExecutableQuery14 (SessionImpl session, String statement,
		String language, QueryRootNode root, MarkLogicFileSystem mlfs)
		throws InvalidQueryException
	{
		queryBuilder = new MarkLogicQueryBuilder14 (root, session, statement, language);
		String levelName = System.getProperty ("mljcr.log.level", DEFAULT_LOG_LEVEL);
		logLevel = Level.parse (levelName);
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
