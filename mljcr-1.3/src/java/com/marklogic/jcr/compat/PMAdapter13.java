/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.compat;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.value.BLOBFileValue;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.name.QName;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Jul 16, 2008
 * Time: 7:59:50 PM
 */
public class PMAdapter13 implements PMAdapter
{
	public NodeState newNodeState (NodeId id)
	{
		return new NodeState (id, null, null, NodeState.STATUS_NEW, false);
	}

	public void setNodeTypeName (NodeState state, String name)
	{
		state.setNodeTypeName (QName.valueOf (name));
	}

	public String getTypeNameAsString (NodeState state)
	{
		return state.getNodeTypeName().toString();
	}

	public void addPropertyName (NodeState state, String name)
	{
		state.addPropertyName (QName.valueOf (name));
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

	public void addChildNodeEntry (NodeState state, String childName, NodeId nodeId)
	{
		state.addChildNodeEntry (QName.valueOf (childName), nodeId);
	}

	public void addName (Set set, String name)
	{
		set.add (QName.valueOf (name));
	}

	public boolean sameNodeTypeName (NodeState state, String name)
	{
		// FIXME: I think this may be broken, it tests identity in Object.equals
		return QName.valueOf (name).equals (state.getNodeTypeName());
	}

	public boolean samePropertyName (PropertyState state, String name)
	{
		// FIXME: I think this may be broken, it tests identity in Object.equals
		return state.getName().equals(QName.valueOf (name));
	}

	public BLOBFileValue getBlobFileValue (InternalValue val)
	{
		return (BLOBFileValue) val.internalValue();
	}
}
