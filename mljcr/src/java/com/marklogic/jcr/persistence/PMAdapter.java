/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.persistence;

import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.NodeId;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Jul 16, 2008
 * Time: 6:47:58 PM
 */
public interface PMAdapter
{
	void setNodeTypeName (NodeState state, String name);

	boolean sameNodeTypeName (NodeState state, String name);

	boolean samePropertyName (PropertyState state, String name);

	void addPropertyName (NodeState state, String name);

	void addChildNodeEntry (NodeState state, String childName, NodeId nodeId);

	void addName (Set set, String name);
}
