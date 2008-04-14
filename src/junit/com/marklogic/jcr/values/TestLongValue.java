/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import javax.jcr.Value;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import javax.jcr.ValueFormatException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 6:01:12 PM
 */
public class TestLongValue
{
	private Value value = null;

	@Before
	public void setup()
	{
		value = new LongValue (1234567L);
	}

	@Test
	public void longValue() throws RepositoryException
	{
		assertNotNull (value);

		assertEquals (PropertyType.LONG, value.getType());
		assertEquals (1234567L, value.getLong());
	}

	@Test
	public void stringValue() throws RepositoryException
	{
		assertEquals ("1234567", value.getString());
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
		assertEquals (1234567L, value.getLong());
		value.getStream();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void stringValueBeforeStream() throws RepositoryException
	{
		assertEquals ("1234567", value.getString());
		value.getStream();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void valueAfterStream() throws RepositoryException
	{
		assertNotNull (value.getStream());
		value.getLong();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void stringValueAfterStream() throws RepositoryException
	{
		assertNotNull (value.getStream());
		value.getString();	// should throw IllegalStateException
	}
}
