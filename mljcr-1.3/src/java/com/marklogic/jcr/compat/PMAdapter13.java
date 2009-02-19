
/*
 *  Copyright (c) 2009,  Mark Logic Corporation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  The use of the Apache License does not indicate that this project is
 *  affiliated with the Apache Software Foundation.
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
 * This class contains methods that adapt the generic MLJCR code
 * to JackRabbit 1.3.
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
		return QName.valueOf (name).equals (state.getNodeTypeName());
	}

	public boolean samePropertyName (PropertyState state, String name)
	{
		return state.getName().equals(QName.valueOf (name));
	}

	public BLOBFileValue getBlobFileValue (InternalValue val)
	{
		return (BLOBFileValue) val.internalValue();
	}

	public String propertyHashKey (PropertyState state)
	{
		return state.getParentId().getUUID().toString() + "|" + state.getName().toString();
	}
}
