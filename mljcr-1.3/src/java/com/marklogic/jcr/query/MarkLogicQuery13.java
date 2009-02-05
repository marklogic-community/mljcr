/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
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
		super (statement, language, 0, Long.MAX_VALUE, mlfs, session);
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
			strings [i] = names [i].toString();	// TODO: verify that this is the right form needed
		}

		super.addPropertySelectors (strings);
	}

}
