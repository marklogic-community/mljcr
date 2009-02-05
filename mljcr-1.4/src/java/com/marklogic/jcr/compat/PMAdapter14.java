/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.compat;

import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.value.BLOBFileValue;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Jul 16, 2008
 * Time: 6:53:40 PM
 */
public class PMAdapter14 implements PMAdapter
{
	private final NameFactory factory = NameFactoryImpl.getInstance();

	public NodeState newNodeState (NodeId id)
	{
		return new NodeState (id, null, null, NodeState.STATUS_NEW, false);
	}

	public void setNodeTypeName (NodeState state, String name)
	{
		state.setNodeTypeName (factory.create (name));
	}

	public String getTypeNameAsString (NodeState state)
	{
		return state.getNodeTypeName().toString();
	}

	public String getPropertyStateNameAsString (PropertyState state)
	{
		return state.getName().toString();
	}

	public String getChildNodeEntryAsString (NodeState.ChildNodeEntry entry)
	{
		return entry.getName().toString();
	}

	public String getPropertyIdNameAsString (PropertyId propId)
	{
		return propId.getName().toString();
	}

	public void addPropertyName (NodeState state, String name)
	{
		state.addPropertyName (factory.create (name));
	}

	public void addChildNodeEntry (NodeState state, String childName, NodeId nodeId)
	{
		state.addChildNodeEntry (factory.create (childName), nodeId);
	}

	public void addName (Set set, String name)
	{
		set.add (factory.create (name));
	}

	public boolean sameNodeTypeName (NodeState state, String name)
	{
		// FIXME: I think this may be broken, it tests identity in Object.equals
		return factory.create (name).equals (state.getNodeTypeName());
	}

	public boolean samePropertyName (PropertyState state, String name)
	{
		// FIXME: I think this may be broken, it tests identity in Object.equals
		return state.getName().equals (factory.create (name));
	}

	public BLOBFileValue getBlobFileValue (InternalValue val)
	{
		return val.getBLOBFileValue();
	}
}
