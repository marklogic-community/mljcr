/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import com.marklogic.jcr.fs.MarkLogicFileSystem;

import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;

import javax.jcr.Session;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 4, 2009
 * Time: 7:33:56 PM
 */
public class MarkLogicQuery14 extends AbstractQuery
{
	public MarkLogicQuery14 (String statement, String language,
		long offset, long limit, MarkLogicFileSystem mlfs, Session session)
	{
		super (statement, language, offset, limit, mlfs, session);
	}

	// -----------------------------------------------------------

	public void addPropertyValueTest (Path relPath, String opString, String value, String operand, String functionName)
	{
		Path.Element[] elements = relPath.getElements();
		String [] strings = new String [elements.length];

		for (int i = 0; i < strings.length; i++) {
			strings [i] = elements [i].toString();
		}

		super.addPropertyValueTest (strings, opString, value, operand, functionName);
	}

	void addPropertySelectors (Name[] names)
	{
		String [] strings = new String [names.length];

		for (int i = 0; i < names.length; i++) {
			strings [i] = names [i].toString();	// TODO: verify that this is the right form needed
		}

		super.addPropertySelectors (strings);
	}
}
