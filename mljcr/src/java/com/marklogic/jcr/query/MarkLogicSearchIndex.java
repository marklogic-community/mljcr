/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.AbstractQueryHandler;
import org.apache.jackrabbit.core.query.DefaultQueryNodeFactory;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameConstants;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Oct 13, 2008
 * Time: 4:26:45 PM
 */
public class MarkLogicSearchIndex extends AbstractQueryHandler
{
	private static final Logger log = Logger.getLogger (MarkLogicSearchIndex.class.getName());

	private static final Name [] indexTypeNames = {
		NameConstants.NT_CHILDNODEDEFINITION,
		NameConstants.NT_FROZENNODE,
		NameConstants.NT_NODETYPE,
		NameConstants.NT_PROPERTYDEFINITION,
		NameConstants.NT_VERSION,
		NameConstants.NT_VERSIONEDCHILD,
		NameConstants.NT_VERSIONHISTORY,
		NameConstants.NT_VERSIONLABELS,
		NameConstants.REP_NODETYPES,
		NameConstants.REP_SYSTEM,
		NameConstants.REP_VERSIONSTORAGE,
		// Supertypes
		NameConstants.NT_BASE,
		NameConstants.MIX_REFERENCEABLE
	};

	private static final List VALID_SYSTEM_INDEX_NODE_TYPE_NAMES =
		Collections.unmodifiableList (Arrays.asList (indexTypeNames));

	// ---------------------------------------------------------------

	protected void doInit() throws IOException
	{
		// FIXME: auto-generated
	}

	public void addNode (NodeState node) throws RepositoryException, IOException
	{
		throw new UnsupportedOperationException ("addNode");
	}

	public void deleteNode (NodeId id) throws IOException
	{
		throw new UnsupportedOperationException ("deleteNode");
	}

	public void close() throws IOException
	{
		// FIXME: auto-generated
	}

	public ExecutableQuery createExecutableQuery (SessionImpl session,
		ItemManager itemMgr, String statement, String language)
		throws InvalidQueryException
	{
		log.info ("lang=" + language + ", stmt=" + statement);

		return new MLExecutableQuery (session, itemMgr, statement, language, getQueryNodeFactory());
	}

	protected DefaultQueryNodeFactory getQueryNodeFactory() {
		return new DefaultQueryNodeFactory (VALID_SYSTEM_INDEX_NODE_TYPE_NAMES);
	}
}
