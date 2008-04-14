/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.values;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;


/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 5:12:10 PM
 */
public class TestValueFactory
{
	private ValueFactory factory;

	@Before
	public void setup() throws RepositoryException
	{
		factory = new ValueFactory();
	}

	@Test
	public void createString() throws RepositoryException
	{
		factory = new ValueFactory();

		Value value;

		value = factory.createValue ("foo");
		assertEquals (PropertyType.STRING, value.getType());
		assertEquals ("foo", value.getString());
	}

	@Test
	public void createStringByType() throws RepositoryException
	{
		factory = new ValueFactory();

		Value value;

		value = factory.createValue ("foo", PropertyType.STRING);
		assertEquals (PropertyType.STRING, value.getType());
		assertEquals ("foo", value.getString());
	}

	@Test
	public void createLong() throws RepositoryException
	{
		factory = new ValueFactory();

		Value value;

		value = factory.createValue (1234L);
		assertEquals (PropertyType.LONG, value.getType());
		assertEquals (1234, value.getLong());
	}

	@Test
	public void createDouble() throws RepositoryException
	{
		factory = new ValueFactory();

		Value value;

		value = factory.createValue (1234.56);
		assertEquals (PropertyType.DOUBLE, value.getType());
		assertEquals (1234.56, value.getDouble(), 0.0);
	}

	@Test
	public void createBoolean() throws RepositoryException
	{
		factory = new ValueFactory();

		Value value;

		value = factory.createValue (false);
		assertEquals (PropertyType.BOOLEAN, value.getType());
		assertEquals (false, value.getBoolean());
	}
}
