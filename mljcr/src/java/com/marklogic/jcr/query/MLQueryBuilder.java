/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.query.*;
import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;

import javax.jcr.query.Query;
import javax.jcr.Session;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 11, 2008
 * Time: 6:15:52 PM
 */
public class MLQueryBuilder implements QueryNodeVisitor
{
	private static final Logger log = Logger.getLogger (MLQueryBuilder.class.getName());

	private final QueryRootNode root;
	private final Session session;
	private final ItemManager itemMgr;
	private final String statement;
	private final String language;

	public MLQueryBuilder (QueryRootNode root, Session session, ItemManager itemMgr,
		String statement, String language)
	{
		this.root = root;
		this.session = session;
		this.itemMgr = itemMgr;
		this.statement = statement;
		this.language = language;
	}

	// --------------------------------------------------------------

	private static final Level LOG_LEVEL = Level.INFO;

	private void log (String msg)
	{
		log.log (LOG_LEVEL, msg);
	}

	// --------------------------------------------------------------

	public Query createQuery (long offset, long limit)
	{
		MLQuery query = new MLQuery (statement, language, offset, limit);

		return (Query) root.accept (this, query);
	}

	public QueryRootNode getRootNode()
	{
		return root;
	}

	// --------------------------------------------------------------
	// Implementation of QueryNodeVisitor interface

	public Object visit (QueryRootNode node, Object data)
	{
		log (node.getClass().getName());

		MLQuery query = (MLQuery) data;

//		log ("  order=" + ((node.getOrderNode () == null) ? "NONE" :  node.getOrderNode().toString()));
//		log ("  abs=" + node.getLocationNode().isAbsolute());

		Name [] names = node.getSelectProperties();

		for (int i = 0; i < names.length; i++) {
			Name name = names[i];
//			log ("    selprop(" + i + ")=" + name.toString());

			query.addPropertySelector (name.toString());
		}

		if (node.getLocationNode() != null) {
			node.getLocationNode().accept (this, data);
		}

		return data;
	}

	public Object visit (OrQueryNode node, Object data)
	{
		log (node.getClass().getName());
//		log ("  type=" + node.getType());

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (AndQueryNode node, Object data)
	{
		log (node.getClass().getName());
//		log ("  type=" + node.getType());

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (NotQueryNode node, Object data)
	{
		log (node.getClass().getName());
//		log ("  type=" + node.getType());

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (ExactQueryNode node, Object data)
	{
		log (node.getClass().getName());
//		log ("  propname=" + node.getPropertyName().toString());
//		log ("  propval=" + node.getValue().toString());

		return data;
	}

	public Object visit (NodeTypeQueryNode node, Object data)
	{
		log (node.getClass().getName());
//		log ("  type=" + node.getType());

		MLQuery query = (MLQuery) data;

		query.addPropertyValuePredicate (node.getPropertyName().toString(), node.getValue().toString());

		return data;
	}

	public Object visit (TextsearchQueryNode node, Object data)
	{
		log (node.getClass().getName());
//		log ("  query=" + node.getRelativePath());
//		log ("  query=" + node.getQuery ());
//		log ("  refsprop=" + node.getReferencesProperty ());
//		log ("  needsystree=" + node.needsSystemTree());
//		log ("  refsprop=" + node.getRelativePath());


		return data;
	}

	public Object visit (PathQueryNode node, Object data)
	{
		LocationStepQueryNode [] stepNodes = node.getPathSteps();

		log (node.getClass().getName());
//		log ("  abs=" + node.isAbsolute ());
//		log ("  stepscount=" + stepNodes.length);

		for (int i = 0; i < stepNodes.length; i++) {
			LocationStepQueryNode stepNode = stepNodes [i];

//			log ("    stepNode=" + stepNode.toString());

			stepNode.accept (this, data);
		}

		return data;
	}

	public Object visit (LocationStepQueryNode node, Object data)
	{
		log (node.getClass().getName());

		MLQuery query = (MLQuery) data;
		Name nameTest = node.getNameTest();

//		log ("  nametest=" + ((nameTest == null) ? "*" : nameTest.toString()));
//		log ("  predcount=" + node.getPredicates ().length);
//		log ("  incldescendents=" + node.getIncludeDescendants ());

		if (nameTest == null) {
			query.addAnyNodePathStep (node.getIncludeDescendants());
		} else if ("".equals (nameTest.getLocalName()) && "".equals (nameTest.getNamespaceURI())) {
			query.addNamedNodePathStep ("", node.getIncludeDescendants());
		} else {
			query.addNamedNodePathStep (nameTest.toString(), node.getIncludeDescendants());
		}

		int index = node.getIndex();

		// FIXME: should this operate like RelationQueryNode?
		if (index != LocationStepQueryNode.NONE) {
			if (index == LocationStepQueryNode.LAST) {
				query.addPredicate ("position() = last()");
			}
			query.addPositionPredicate (node.getIndex());
		}

		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (RelationQueryNode node, Object data)
	{
		log (node.getClass().getName());

		MLQuery query = (MLQuery) data;

//		log ("  op=" + node.getOperation());
//		log ("  valtype=" + node.getValueType());
//		log ("  pos=" + node.getPositionValue());

		int op = node.getOperation();
		String opString = null;

		if (op == QueryConstants.OPERATION_BETWEEN) {
		    opString = ("BETWEEN");
		} else if (op == QueryConstants.OPERATION_EQ_GENERAL) {
		    opString = ("=");
		} else if (op == QueryConstants.OPERATION_EQ_VALUE) {
		    opString = ("eq");
		} else if (op == QueryConstants.OPERATION_GE_GENERAL) {
		    opString = (">=");
		} else if (op == QueryConstants.OPERATION_GE_VALUE) {
		    opString = ("ge");
		} else if (op == QueryConstants.OPERATION_GT_GENERAL) {
		    opString = ("> ");
		} else if (op == QueryConstants.OPERATION_GT_VALUE) {
		    opString = ("gt");
		} else if (op == QueryConstants.OPERATION_IN) {
		    opString = ("IN");
		} else if (op == QueryConstants.OPERATION_LE_GENERAL) {
		    opString = ("<=");
		} else if (op == QueryConstants.OPERATION_LE_VALUE) {
		    opString = ("le");
		} else if (op == QueryConstants.OPERATION_LIKE) {
		    opString = ("LIKE");
		} else if (op == QueryConstants.OPERATION_LT_GENERAL) {
		    opString = ("< ");
		} else if (op == QueryConstants.OPERATION_LT_VALUE) {
		    opString = ("lt");
		} else if (op == QueryConstants.OPERATION_NE_GENERAL) {
		    opString = ("<>");
		} else if (op == QueryConstants.OPERATION_NE_VALUE) {
		    opString = ("ne");
		} else if (op == QueryConstants.OPERATION_NOT_NULL) {
		    opString = ("NOT NULL");
		} else if (op == QueryConstants.OPERATION_NULL) {
		    opString = ("IS NULL");
		} else if (op == QueryConstants.OPERATION_SIMILAR) {
		    opString = ("similarity");
		} else if (op == QueryConstants.OPERATION_SPELLCHECK) {
		    opString = ("spellcheck");
		} else {
		    opString = ("!!UNKNOWN OPERATION!!");
		}

		Path relpath = node.getRelativePath();
		int valueType = node.getValueType();
		String propName = propPath (relpath);

		switch (valueType) {
		case QueryConstants.TYPE_DATE:
			break;

		case QueryConstants.TYPE_DOUBLE:
			break;

		case QueryConstants.TYPE_LONG:
			break;

		case QueryConstants.TYPE_POSITION:
			query.addPredicate (propName + "[position() " + opString + " " + positionName (node.getPositionValue()) + "]");
			break;

		case QueryConstants.TYPE_STRING:
			break;

		case QueryConstants.TYPE_TIMESTAMP:
			break;

		default:
			throw new RuntimeException ("bad type: " + node.getValueType());
		}

		return data;
	}

	public Object visit (OrderQueryNode node, Object data)
	{
		log (node.getClass().getName());

		OrderQueryNode.OrderSpec [] orderspecs = node.getOrderSpecs ();

		for (int i = 0; i < orderspecs.length; i++) {
			OrderQueryNode.OrderSpec orderspec = orderspecs[i];

//			log ("  " + orderspec.getProperty ().toString () + ", ascending=" + orderspec.isAscending ());
		}
		return data;
	}

	public Object visit (DerefQueryNode node, Object data)
	{
		log (node.getClass().getName());

//		log ("  refprop=" + node.getRefProperty().toString());

		return data;
	}

	public Object visit (PropertyFunctionQueryNode node, Object data)
	{
		log (node.getClass().getName());

//		log ("  funcname=" + node.getFunctionName());

		return data;
	}

	// -------------------------------------------------------------

	private String propPath (Path relPath)
	{
		StringBuffer sb = new StringBuffer ();
		Path.Element[] elements = relPath.getElements ();

		for (int i = 0; i < elements.length; i++) {
			if (i != 0) {
				sb.append ("/");
			}

			if (i == elements.length - 1) {
				sb.append ("property[@name=\"").append (elements[i]).append ("\"]");
			} else {
				sb.append (elements[i]);
			}
		}

		return sb.toString ();
	}

	private String positionName (int pos)
	{
		if (pos == LocationStepQueryNode.LAST) {
			return "last()";
		}

		return "" + pos;
	}


}
