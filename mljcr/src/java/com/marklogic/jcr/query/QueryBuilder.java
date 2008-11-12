/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.query.QueryNodeVisitor;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.core.query.OrQueryNode;
import org.apache.jackrabbit.core.query.AndQueryNode;
import org.apache.jackrabbit.core.query.NotQueryNode;
import org.apache.jackrabbit.core.query.ExactQueryNode;
import org.apache.jackrabbit.core.query.NodeTypeQueryNode;
import org.apache.jackrabbit.core.query.TextsearchQueryNode;
import org.apache.jackrabbit.core.query.PathQueryNode;
import org.apache.jackrabbit.core.query.LocationStepQueryNode;
import org.apache.jackrabbit.core.query.RelationQueryNode;
import org.apache.jackrabbit.core.query.OrderQueryNode;
import org.apache.jackrabbit.core.query.DerefQueryNode;
import org.apache.jackrabbit.core.query.PropertyFunctionQueryNode;

import javax.jcr.query.Query;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 11, 2008
 * Time: 6:15:52 PM
 */
public class QueryBuilder implements QueryNodeVisitor
{
	private static final Logger log = Logger.getLogger (QueryBuilder.class.getName());

	private final QueryRootNode root;

	public QueryBuilder (QueryRootNode root)
	{
		this.root = root;
	}

	public Query createQuery()
	{
		return (Query) root.accept (this, null);
	}

	// --------------------------------------------------------------
	// Implementation of QueryNodeVisitor interface

	public Object visit (QueryRootNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  order=" + ((node.getOrderNode () == null) ? "NULL" :  node.getOrderNode().toString()));
		log.info ("  abs=" + node.getLocationNode().isAbsolute());

		// FIXME: This is not real
		if (node.getLocationNode() != null) {
		    return node.getLocationNode().accept (this, node.getLocationNode());
		}

		return data;
	}

	public Object visit (OrQueryNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  type=" + node.getType());

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (AndQueryNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  type=" + node.getType());

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (NotQueryNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  type=" + node.getType());

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (ExactQueryNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  propname=" + node.getPropertyName().toString());
		log.info ("  propval=" + node.getValue().toString());

		return data;
	}

	public Object visit (NodeTypeQueryNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  type=" + node.getType());

		return data;
	}

	public Object visit (TextsearchQueryNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  query=" + node.getRelativePath());
		log.info ("  query=" + node.getQuery ());
		log.info ("  refsprop=" + node.getReferencesProperty ());
		log.info ("  needsystree=" + node.needsSystemTree());
		log.info ("  refsprop=" + node.getRelativePath());

		return data;
	}

	public Object visit (PathQueryNode node, Object data)
	{
		LocationStepQueryNode [] stepNodes = node.getPathSteps();

		log.info (node.getClass().getName());
		log.info ("  abs=" + node.isAbsolute ());
		log.info ("  stepscount=" + stepNodes.length);

		for (int i = 0; i < stepNodes.length; i++) {
			LocationStepQueryNode stepNode = stepNodes [i];

			log.info ("    stepNode=" + stepNode.toString());

			stepNode.accept (this, stepNode);
		}

		return data;
	}

	public Object visit (LocationStepQueryNode node, Object data)
	{
		log.info (node.getClass().getName());

		log.info ("  nametest=" + ((node.getNameTest () == null) ? "*" : node.getNameTest().toString()));
		log.info ("  predcount=" + node.getPredicates ().length);
		log.info ("  incldescendents=" + node.getIncludeDescendants ());

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (RelationQueryNode node, Object data)
	{
		log.info (node.getClass().getName());
		log.info ("  valtype=" + node.getValueType ());
		log.info ("  pos=" + node.getPositionValue ());

		return data;
	}

	public Object visit (OrderQueryNode node, Object data)
	{
		log.info (node.getClass().getName());

		OrderQueryNode.OrderSpec [] orderspecs = node.getOrderSpecs ();

		for (int i = 0; i < orderspecs.length; i++) {
			OrderQueryNode.OrderSpec orderspec = orderspecs[i];

			log.info ("  " + orderspec.getProperty ().toString () + ", ascending=" + orderspec.isAscending ());
		}
		return data;
	}

	public Object visit (DerefQueryNode node, Object data)
	{
		log.info (node.getClass().getName());

		log.info ("  refprop=" + node.getRefProperty().toString());

		return data;
	}

	public Object visit (PropertyFunctionQueryNode node, Object data)
	{
		log.info (node.getClass().getName());

		log.info ("  funcname=" + node.getFunctionName());

		return data;
	}
}
