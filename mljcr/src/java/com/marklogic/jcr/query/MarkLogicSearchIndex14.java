/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.DefaultQueryNodeFactory;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryParser;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameConstants;

import javax.jcr.query.InvalidQueryException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 20, 2008
 * Time: 10:54:54 AM
 */
public class MarkLogicSearchIndex14 extends MarkLogicSearchIndex
{
	private static final Name[] indexTypeNames = {
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

	public ExecutableQuery createExecutableQuery (SessionImpl session,
		ItemManager itemMgr, String statement, String language)
		throws InvalidQueryException
	{
		log.info ("lang=" + language + ", stmt=" + statement);

        System.out.println("lang="+language+", stmt="+statement);

		QueryRootNode root = QueryParser.parse (statement, language,
			session.getNamePathResolver(),
			new DefaultQueryNodeFactory (VALID_SYSTEM_INDEX_NODE_TYPE_NAMES));

		return new MLExecutableQuery (session, itemMgr, statement, language, root, mlfs);
	}
}
