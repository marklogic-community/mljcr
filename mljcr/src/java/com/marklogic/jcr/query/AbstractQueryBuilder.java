/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.query.*;

import javax.jcr.Session;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 11, 2008
 * Time: 6:15:52 PM
 */
abstract class AbstractQueryBuilder implements QueryNodeVisitor
{
	private static final String NT_NS_NAME = "http://www.jcp.org/jcr/nt/1.0";
	private static final String NT_BASE_TYPE_NAME = "{" + NT_NS_NAME + "}base";

	private static final String DEFAULT_LOG_LEVEL = "FINE";


	//	private final ItemManager itemMgr;
	private final QueryRootNode root;

	protected final Logger logger = Logger.getLogger (AbstractQueryBuilder.class.getName());
	protected final Level logLevel;
	protected final Session session;
	protected final String statement;
	protected final String language;

	public AbstractQueryBuilder (QueryRootNode root, Session session, ItemManager itemMgr,
		String statement, String language)
	{
		this.root = root;
		this.session = session;
//		this.itemMgr = itemMgr;
		this.statement = statement;
		this.language = language;
		String levelName = System.getProperty ("mljcr.log.level", DEFAULT_LOG_LEVEL);
		logLevel = Level.parse (levelName);
	}

	// --------------------------------------------------------------

	public QueryRootNode getRootNode()
	{
		return root;
	}

	// --------------------------------------------------------------
	// Implementation of QueryNodeVisitor interface

	abstract public Object visit (TextsearchQueryNode node, Object data);
	abstract public Object visit (QueryRootNode node, Object data);

	// internal method that subclasses call for common operations
	protected Object visitRoot (QueryRootNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		if (node.getLocationNode() != null) {
			node.getLocationNode().accept (this, data);
		}

		if (node.getOrderNode() != null) {
			node.getOrderNode ().accept (this, data);
		}

		return data;
	}

	public Object visit (OrQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		QueryNode [] operands = node.getOperands();

		if (operands.length < 2) {
			node.acceptOperands (this, data);
			return data;
		}


		// TODO
//		for (int i = 0; i < operands.length; i++) {
//			QueryNode operand = operands[i];
//
//		}

		// TODO: push to add-expr state
		node.acceptOperands (this, data);
		// TODO: pop from add-expr state

		return data;
	}

	public Object visit (AndQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		if (node.getNumOperands() < 2) {
			node.acceptOperands (this, data);
			return data;
		}

		// TODO: push to add-expr state
		node.acceptOperands (this, data);
		// TODO: pop from add-expr state

		return data;
	}

	public Object visit (NotQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		// TODO: push not-expr

		node.acceptOperands (this, data);

		// TODO: pop not-expr

		return data;
	}

	public Object visit (ExactQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());
//		log ("  propname=" + node.getPropertyName().toString());
//		log ("  propval=" + node.getValue().toString());

		return data;
	}

	public Object visit (NodeTypeQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		AbstractQuery query = (AbstractQuery) data;

		String typeName = node.getValue().toString();

		if ( ! NT_BASE_TYPE_NAME.equals (typeName)) {
			query.addPropertyValuePredicate (node.getPropertyName().toString(), node.getValue().toString());
		}

		return data;
	}


	public Object visit (PathQueryNode node, Object data)
	{
		LocationStepQueryNode [] stepNodes = node.getPathSteps();

		logger.log (logLevel, node.getClass().getName());
//		log ("  abs=" + node.isAbsolute ());
//		log ("  stepscount=" + stepNodes.length);

		for (int i = 0; i < stepNodes.length; i++) {
			LocationStepQueryNode stepNode = stepNodes [i];

//			log ("    stepNode=" + stepNode.toString());

			stepNode.accept (this, data);
		}

		return data;
	}

	protected abstract String locationStepNameTest (LocationStepQueryNode node);

	public Object visit (LocationStepQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		AbstractQuery query = (AbstractQuery) data;
		String nameTest = locationStepNameTest (node);

		int index = node.getIndex();
		boolean hasindexPredicate = index != LocationStepQueryNode.NONE;

		if (nameTest == null) {
			query.addAnyNodePathStep (node.getIncludeDescendants());
		} else {
			query.addNamedNodePathStep (nameTest, node.getIncludeDescendants());
		}

		// FIXME: should this operate like RelationQueryNode?
		if (hasindexPredicate) {
			if (index == LocationStepQueryNode.LAST) {
				query.addPredicate ("position() = last()");
			} else {
				query.addPositionPredicate (node.getIndex());
			}
		}

		node.acceptOperands (this, data);

		return data;
	}

	protected abstract String [] relationQueryPathToStrings (RelationQueryNode RelationQueryNodenode);

	public Object visit (RelationQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());
logger.log (Level.INFO, node.getClass().getName());

		AbstractQuery query = (AbstractQuery) data;

//		log ("  op=" + node.getOperation());
//		log ("  valtype=" + node.getValueType());
//		log ("  pos=" + node.getPositionValue());

		int op = node.getOperation();
		String opString = null;
		String function = null;

		if (op == QueryConstants.OPERATION_BETWEEN) {
		    opString = ("BETWEEN");
		} else if (op == QueryConstants.OPERATION_EQ_GENERAL) {
		    opString = (" = ");
		} else if (op == QueryConstants.OPERATION_EQ_VALUE) {
		    opString = (" eq ");
		} else if (op == QueryConstants.OPERATION_GE_GENERAL) {
		    opString = (" >= ");
		} else if (op == QueryConstants.OPERATION_GE_VALUE) {
		    opString = (" ge ");
		} else if (op == QueryConstants.OPERATION_GT_GENERAL) {
		    opString = (" > ");
		} else if (op == QueryConstants.OPERATION_GT_VALUE) {
		    opString = (" gt ");
		} else if (op == QueryConstants.OPERATION_IN) {
		    opString = (" IN ");
		} else if (op == QueryConstants.OPERATION_LE_GENERAL) {
		    opString = (" <= ");
		} else if (op == QueryConstants.OPERATION_LE_VALUE) {
		    opString = (" le ");
		} else if (op == QueryConstants.OPERATION_LIKE) {
		    opString = (" LIKE ");
		} else if (op == QueryConstants.OPERATION_LT_GENERAL) {
		    opString = (" < ");
		} else if (op == QueryConstants.OPERATION_LT_VALUE) {
		    opString = (" lt ");
		} else if (op == QueryConstants.OPERATION_NE_GENERAL) {
		    opString = (" != ");
		} else if (op == QueryConstants.OPERATION_NE_VALUE) {
		    opString = (" ne ");
		} else if (op == QueryConstants.OPERATION_NOT_NULL) {
		    function = "fn:exists";
		} else if (op == QueryConstants.OPERATION_NULL) {
			function = "fn:empty";
//		} else if (op == QueryConstants.OPERATION_SIMILAR) {
//		    opString = (" similarity ");
//		} else if (op == QueryConstants.OPERATION_SPELLCHECK) {
//		    opString = (" spellcheck ");
		} else {
		    opString = (" !!UNKNOWN OPERATION!! ");
		}

		int valueType = node.getValueType();
//		String propName = propPath (relpath);
		String value = ".";
		String operand = "";

		// TODO: add type tests here?
		switch (valueType) {
		case QueryConstants.TYPE_DATE:
			operand = node.getDateValue().toString();
			break;

		case QueryConstants.TYPE_DOUBLE:
			operand = "" + node.getDoubleValue();
			break;

		case QueryConstants.TYPE_LONG:
			operand = "" + node.getLongValue();
			break;

		case QueryConstants.TYPE_POSITION:
			if (node.getPositionValue() == LocationStepQueryNode.LAST) {
				operand = "last()";
			} else {
				operand = "" + node.getPositionValue();
			}

			query.addPredicate ("position()" + opString + operand);

			return data;

		case QueryConstants.TYPE_STRING:
			operand = "\"" + nsResolverHack (node.getStringValue()) + "\"";
			break;

		case QueryConstants.TYPE_TIMESTAMP:
			// FIXME:
			operand = node.getDateValue().toString();
			break;

		case 0:
			break;

		default:
			throw new RuntimeException ("bad type: " + node.getValueType() + ", opString: " + opString);
		}

		query.addPropertyValueTest (relationQueryPathToStrings (node),
			opString, value, operand, function);

//		query.addPredicate (propName + opString + operand);

		return data;
	}

	// TODO: Need to catch other prefixes here?  General lookup?  Patch the parser?
	private String nsResolverHack (String stringValue)
	{
		if ( ! stringValue.startsWith ("nt:")) {
			return stringValue;
		}

		return "{" + NT_NS_NAME + "}" + stringValue.substring (3);
	}

	public Object visit (OrderQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());
		AbstractQuery query = (AbstractQuery) data;

		OrderQueryNode.OrderSpec [] orderspecs = node.getOrderSpecs();

		for (int i = 0; i < orderspecs.length; i++) {
			OrderQueryNode.OrderSpec orderspec = orderspecs[i];

			query.addOrderBySpec (orderspec);
		}
		return data;
	}

	public Object visit (DerefQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

//		log ("  refprop=" + node.getRefProperty().toString());

		return data;
	}

	public Object visit (PropertyFunctionQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

//		log ("  funcname=" + node.getFunctionName());

		return data;
	}

	// -------------------------------------------------------------

//	private String propPath (Path relPath)
//	{
//		StringBuffer sb = new StringBuffer ();
//		Path.Element[] elements = relPath.getElements ();
//
//		for (int i = 0; i < elements.length; i++) {
//			if (i != 0) {
//				sb.append ("/");
//			}
//
//			if (i == elements.length - 1) {
//				sb.append ("property[@name=\"").append (elements[i]).append ("\"]");
//			} else {
//				sb.append (elements[i]);
//			}
//		}
//
//		return sb.toString ();
//	}
//
//	private String positionName (int pos)
//	{
//		if (pos == LocationStepQueryNode.LAST) {
//			return "last()";
//		}
//
//		return "" + pos;
//	}
}
