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

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.query.AbstractQueryHandler;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.state.NodeState;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import java.io.IOException;
import java.util.logging.Logger;

import com.marklogic.jcr.fs.MarkLogicFileSystem;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Oct 13, 2008
 * Time: 4:26:45 PM
 */
abstract public class AbstractSearchIndex extends AbstractQueryHandler
{
	protected static final Logger log = Logger.getLogger (AbstractSearchIndex.class.getName());

	protected MarkLogicFileSystem mlfs = null;

	// ---------------------------------------------------------------

	protected void doInit() throws IOException
	{
		QueryHandlerContext context = super.getContext();
		FileSystem fs = context.getFileSystem ();

		if (!(fs instanceof MarkLogicFileSystem)) {
			throw new IOException ("Filesystem must be an instance of MarkLogicFileSystem");
		}

		this.mlfs = (MarkLogicFileSystem) fs;
	}

	public void addNode (NodeState node) throws RepositoryException, IOException
	{
		// Do nothing for now, MarkLogic indexes content
	}

	public void deleteNode (NodeId id) throws IOException
	{
		// Do nothing for now, MarkLogic indexes content
	}

	public void close() throws IOException
	{
	}

	abstract public ExecutableQuery createExecutableQuery (SessionImpl session,
		ItemManager itemMgr, String statement, String language)
		throws InvalidQueryException;
}
