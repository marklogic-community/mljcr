/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;


import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;

import java.util.Calendar;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 4:59:51 PM
 */
public class ValueFactory implements javax.jcr.ValueFactory
{
	public Value createValue (String value)
	{
		return new StringValue (value);
	}

	public Value createValue (String value, int type) throws ValueFormatException
	{
		switch (type) {
		case PropertyType.STRING:
			return new StringValue (value);

		}

		throw new ValueFormatException ("Unrecognized type: " + type);
	}

	public Value createValue (long value)
	{
		return new LongValue (value);
	}

	public Value createValue (double value)
	{
		return new DoubleValue (value);
	}

	public Value createValue (boolean value)
	{
		return new BooleanValue (value);
	}

	public Value createValue (Calendar value)
	{
		return null;  // FIXME: auto-generated
	}

	public Value createValue (InputStream value)
	{
		return null;  // FIXME: auto-generated
	}

	public Value createValue (Node value) throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}
}
