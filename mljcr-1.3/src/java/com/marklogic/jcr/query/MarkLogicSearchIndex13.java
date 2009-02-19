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

import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.query.QueryParser;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.ItemManager;

import javax.jcr.query.InvalidQueryException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 20, 2008
 * Time: 1:07:06 PM
 */
public class MarkLogicSearchIndex13 extends AbstractSearchIndex
{
	public ExecutableQuery createExecutableQuery (SessionImpl session,
		ItemManager itemMgr, String statement, String language)
		throws InvalidQueryException
	{
		log.fine ("lang=" + language + ", stmt=" + statement);

		QueryRootNode root = QueryParser.parse (statement, language,
			session.getNamespaceResolver());

		return new MarkLogicExecutableQuery13 (session, statement, language, root, mlfs);
	}
}
