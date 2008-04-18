/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

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
public class TestDoubleValue
{
	private Value value = null;

	@Before
	public void setup()
	{
		value = new DoubleValue (1234.56);
	}

	@Test
	public void stringValue() throws RepositoryException
	{
		Assert.assertEquals ("1234.56", value.getString());
	}

	@Test
	public void getDouble() throws RepositoryException
	{
		Assert.assertNotNull (value);

		Assert.assertEquals (PropertyType.DOUBLE, value.getType());
		Assert.assertEquals (1234.56, value.getDouble(), 0.0);
	}

	@Test(expected=ValueFormatException.class)
	public void longValue() throws RepositoryException
	{
		value.getLong();
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
		Assert.assertEquals (1234.56, value.getDouble(), 0.0);
		value.getStream();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void stringValueBeforeStream() throws RepositoryException
	{
		Assert.assertEquals ("1234.56", value.getString());
		value.getStream();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void valueAfterStream() throws RepositoryException
	{
		Assert.assertNotNull (value.getStream());
		value.getDouble();	// should throw IllegalStateException
	}

	@Test(expected=IllegalStateException.class)
	public void stringValueAfterStream() throws RepositoryException
	{
		Assert.assertNotNull (value.getStream());
		value.getString();	// should throw IllegalStateException
	}
}
