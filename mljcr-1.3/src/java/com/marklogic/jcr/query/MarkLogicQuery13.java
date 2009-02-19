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

import org.apache.jackrabbit.name.QName;
import org.apache.jackrabbit.name.Path;

import javax.jcr.Session;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 4, 2009
 * Time: 7:28:56 PM
 */
public class MarkLogicQuery13 extends AbstractQuery
{
	public MarkLogicQuery13 (String statement, String language,
		MarkLogicFileSystem mlfs, Session session)
	{
		super (statement, language, mlfs, session);
	}

	// -----------------------------------------------------------

	public void addPropertyValueTest (Path relPath, String opString, String value, String operand, String functionName)
	{
		Path.PathElement[] elements = relPath.getElements();
		String [] strings = new String [elements.length];

		for (int i = 0; i < strings.length; i++) {
			strings [i] = elements [i].toString();
		}

		super.addPropertyValueTest (strings, opString, value, operand, functionName);
	}

	void addPropertySelectors (QName[] names)
	{
		String [] strings = new String [names.length];

		for (int i = 0; i < names.length; i++) {
			strings [i] = names [i].toString();
		}

		super.addPropertySelectors (strings);
	}

}
