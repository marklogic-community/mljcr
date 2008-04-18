/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;

import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 5:56:39 PM
 */
class LongValue implements Value
{
	private final long value;
	private boolean valueFetched = false;
	private boolean streamFetched = false;

	LongValue (long value)
	{
		this.value = value;
	}

	public String getString()
	{
		return "" + getLong();
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
			bytes = getString().getBytes ("utf-8");
		} catch (UnsupportedEncodingException e) {
			bytes = getString().getBytes();
		}

		return new ByteArrayInputStream (bytes);
	}

	public long getLong()
	{
		if (streamFetched) {
			throw new IllegalStateException ("stream has already been created");
		}

		valueFetched = true;

		return value;
	}

	public double getDouble() throws ValueFormatException
	{
		throw new ValueFormatException ("Long property");
	}

	public Calendar getDate() throws ValueFormatException
	{
		throw new ValueFormatException ("Long property");
	}

	public boolean getBoolean() throws ValueFormatException
	{
		throw new ValueFormatException ("Long property");
	}

	public int getType ()
	{
		return PropertyType.LONG;
	}
}
