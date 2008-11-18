/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.persistence;

import org.apache.jackrabbit.core.NodeId;
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
	public void setNodeTypeName (NodeState state, String name)
	{
		state.setNodeTypeName (QName.valueOf (name));
	}

	public void addPropertyName (NodeState state, String name)
	{
		state.addPropertyName (QName.valueOf (name));
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
}
