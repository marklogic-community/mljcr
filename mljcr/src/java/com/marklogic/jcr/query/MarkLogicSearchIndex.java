/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.query.AbstractQueryHandler;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryImpl;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.ItemManager;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Oct 13, 2008
 * Time: 4:26:45 PM
 */
public class MarkLogicSearchIndex extends AbstractQueryHandler
{
	protected void doInit () throws IOException
	{
		// FIXME: auto-generated
	}

	public void addNode (NodeState node) throws RepositoryException, IOException
	{
		throw new UnsupportedOperationException("addNode");
	}

	public void deleteNode (NodeId id) throws IOException
	{
		throw new UnsupportedOperationException("deleteNode");
	}

	public void close () throws IOException
	{
		// FIXME: auto-generated
	}

	public ExecutableQuery createExecutableQuery (SessionImpl session,
		ItemManager itemMgr, String statement, String language)
		throws InvalidQueryException
	{
		QueryImpl query = new QueryImpl();

		query.init (session, itemMgr, this, statement, language);

//		return query;

		throw new InvalidQueryException ("FIXME createExecutableQuery");
	}
}
