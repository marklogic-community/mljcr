/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.PropertyType;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 6:01:12 PM
 */
public class TestBooleanValue
{
	private Value value = null;

	@Before
	public void setup()
	{
		value = new BooleanValue (true);
	}

	@Test
	public void getBoolean() throws RepositoryException
	{
		Assert.assertNotNull (value);

		Assert.assertEquals (PropertyType.BOOLEAN, value.getType());
		Assert.assertEquals (true, value.getBoolean());
	}

	@Test
	public void stringValue() throws RepositoryException
	{
		Assert.assertEquals ("true", value.getString());
	}

	@Test(expected=ValueFormatException.class)
	public void longValue() throws RepositoryException
	{
		value.getLong();
	}

	@Test(expected= ValueFormatException.class)
	public void getDouble() throws RepositoryException
	{
		value.getDouble();
	}

	@Test(expected=ValueFormatException.class)
	public void getDate() throws RepositoryException
	{
		value.getDate();
	}

	@Test(expected=IllegalStateException.class)
	public void valueBeforeStream() throws RepositoryException
	{
		Assert.assertEquals (true, value.getBoolean());
		value.getStream();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void stringValueBeforeStream() throws RepositoryException
	{
		Assert.assertEquals ("true", value.getString());
		value.getStream();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void valueAfterStream() throws RepositoryException
	{
		Assert.assertNotNull (value.getStream());
		value.getBoolean();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void stringValueAfterStream() throws RepositoryException
	{
		Assert.assertNotNull (value.getStream());
		value.getString();	// should throw IllegalStateException
	}
}
