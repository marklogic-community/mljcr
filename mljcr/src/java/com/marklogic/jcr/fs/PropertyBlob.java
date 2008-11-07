/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.BLOBFileValue;

/**
 * Created by IntelliJ IDEA.
* User: ron
* Date: Sep 29, 2008
* Time: 2:51:43 PM
*/
public class PropertyBlob
{
	private final PropertyState propertyState;
	private final BLOBFileValue blobFileValue;
	private final String blobId;
	private final int valueIndex;

	public PropertyBlob (PropertyState propertyState, int valueIndex, BLOBFileValue blobFileValue, String blobId)
	{
		this.propertyState = propertyState;
		this.blobFileValue = blobFileValue;
		this.blobId = blobId;
		this.valueIndex = valueIndex;
	}

	public String getPropertyHashKey()
	{
		return propertyState.getParentId().getUUID().toString() + "|" + propertyState.getName().toString();
	}

	public PropertyState getPropertyState()
	{
		return propertyState;
	}

	public BLOBFileValue getBlobFileValue()
	{
		return blobFileValue;
	}

	public String getBlobId()
	{
		return blobId;
	}

	public int getValueIndex()
	{
		return valueIndex;
	}
}
