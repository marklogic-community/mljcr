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

import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.query.TextsearchQueryNode;
import org.apache.jackrabbit.core.query.LocationStepQueryNode;
import org.apache.jackrabbit.core.query.RelationQueryNode;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.Name;

import javax.jcr.Session;
import javax.jcr.query.Query;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 4, 2009
 * Time: 7:37:56 PM
 */
public class MarkLogicQueryBuilder14 extends AbstractQueryBuilder
{
	public MarkLogicQueryBuilder14 (QueryRootNode root, Session session,
		String statement, String language)
	{
		super (root, session, statement, language);
	}

	// -------------------------------------------------------

	public Query createQuery (long offset, long limit, MarkLogicFileSystem mlfs)
	{
		AbstractQuery query = new MarkLogicQuery14 (statement, language, offset, limit, mlfs, session);

		return (Query) getRootNode().accept (this, query);
	}

	protected String locationStepNameTest (LocationStepQueryNode node)
	{
		Name nameTest = node.getNameTest();

		if (nameTest == null) return null;

		if ("".equals (nameTest.getNamespaceURI()) && "".equals (nameTest.getLocalName ()))
		{
			return "";
		}

		return nameTest.toString();
	}

	// -------------------------------------------------------
	// These two Visitor methods have dependencies on JackRabbit
	// classes that vary between 1.3 and 1.4 so that are implemented
	// here and marked as abstract in AbstractQueryBuilder.

	public Object visit (QueryRootNode node, Object data)
	{
		MarkLogicQuery14 query = (MarkLogicQuery14) data;

		// These are the columns reported by the QueryResult
		query.addPropertySelectors (node.getSelectProperties());

		return visitRoot (node, data);
	}

	public Object visit (TextsearchQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		AbstractQuery query = (AbstractQuery) data;
		Path relPath = node.getRelativePath();
		// FIXME: This is probably broken for the general case
		String relPathStr = (node.getRelativePath() == null) ? "." : relPath.getString();

		query.addFullTextSearch (relPathStr, node.getQuery());

		return data;
	}

	// ---------------------------------------------------------

	protected String [] relationQueryPathStrings (RelationQueryNode node)
	{
		Path.Element[] elements = node.getRelativePath().getElements();
		String [] strings = new String [elements.length];

		for (int i = 0; i < strings.length; i++) {
			strings [i] = elements [i].toString();
		}

		return strings;
	}
}
