/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 5:28:24 PM
 */
public class TestStringValue
{
	private Value value = null;

	@Before
	public void setup()
	{
		value = new StringValue ("foobaloo");
	}

	@Test
	public void stringValue() throws RepositoryException
	{
		assertNotNull (value);

		assertEquals (PropertyType.STRING, value.getType());
		assertEquals ("foobaloo", value.getString());
	}

	@Test(expected=ValueFormatException.class)
	public void getLong() throws RepositoryException
	{
		value.getLong();
	}

	@Test(expected=ValueFormatException.class)
	public void getDouble() throws RepositoryException
	{
		value.getDouble();
	}

	@Test(expected=ValueFormatException.class)
	public void getDate() throws RepositoryException
	{
		value.getDate();
	}

	@Test(expected=ValueFormatException.class)
	public void getBoolean() throws RepositoryException
	{
		value.getBoolean();
	}

	@Test(expected=IllegalStateException.class)
	public void valueBeforeStream() throws RepositoryException
	{
		assertEquals ("foobaloo", value.getString());
		value.getStream();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void valueAfterStream() throws RepositoryException
	{
		assertNotNull (value.getStream());
		value.getString();	// should throw IllegalStateException
	}
}
