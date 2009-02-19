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

import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.value.BLOBFileValue;
import org.apache.jackrabbit.core.value.InternalValue;

import java.util.Set;

/**
 * This interface contains methods that adapt the generic MLJCR code
 * to JackRabbit 1.3.
 */
public interface PMAdapter
{
	NodeState newNodeState (NodeId id);

	void setNodeTypeName (NodeState state, String name);

	String getTypeNameAsString (NodeState state);
	String getPropertyStateNameAsString (PropertyState state);

	String getChildNodeEntryAsString (NodeState.ChildNodeEntry entry);
	String getPropertyIdNameAsString (PropertyId propId);

	boolean sameNodeTypeName (NodeState state, String name);

	boolean samePropertyName (PropertyState state, String name);

	void addPropertyName (NodeState state, String name);

	void addChildNodeEntry (NodeState state, String childName, NodeId nodeId);

	void addName (Set set, String name);

	abstract BLOBFileValue getBlobFileValue (InternalValue val);

	String propertyHashKey (PropertyState state);
}
