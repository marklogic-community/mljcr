/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 5:23:02 PM
 */
class StringValue implements Value
{
	private final String value;
	private boolean valueFetched = false;
	private boolean streamFetched = false;

	StringValue (String value)
	{
		this.value = value;
	}

	public String getString()
	{
		if (streamFetched) {
			throw new IllegalStateException ("stream has already been created");
		}

		valueFetched = true;

		return value;
	}

	public InputStream getStream() throws IllegalStateException, RepositoryException
	{
		if (valueFetched) {
			// this is stupid, but required by the spec
			throw new IllegalStateException ("value has already been read");
		}

		streamFetched = true;

		byte [] bytes;

		try {
			bytes = value.getBytes ("utf-8");
		} catch (UnsupportedEncodingException e) {
			bytes = value.getBytes();
		}

		return new ByteArrayInputStream (bytes);
	}

	public long getLong() throws ValueFormatException
	{
		throw new ValueFormatException ("String property");
	}

	public double getDouble() throws ValueFormatException
	{
		throw new ValueFormatException ("String property");
	}

	public Calendar getDate() throws ValueFormatException
	{
		throw new ValueFormatException ("String property");
	}

	public boolean getBoolean() throws ValueFormatException
	{
		throw new ValueFormatException ("String property");
	}

	public int getType ()
	{
		return PropertyType.STRING;
	}
}
