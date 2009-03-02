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

package com.marklogic.jcr.query;

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

	private final QueryRootNode root;

	protected final Logger logger = Logger.getLogger (AbstractQueryBuilder.class.getName());
	protected final Level logLevel;
	protected final Session session;
	protected final String statement;
	protected final String language;

	public AbstractQueryBuilder (QueryRootNode root, Session session,
		String statement, String language)
	{
		this.root = root;
		this.session = session;
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
	abstract protected String queryNodeValueAsString (ExactQueryNode node);
	abstract protected String propertyNameAsString (ExactQueryNode node);

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

	// FIXME: This is untested, no TCK query tests execute this code path
	public Object visit (OrQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		QueryNode [] operands = node.getOperands();

		if (operands.length < 2) {
			node.acceptOperands (this, data);
			return data;
		}

		AbstractQuery query = (AbstractQuery) data;

		query.pushPredicateLevel();

		node.acceptOperands (this, data);

		query.popPredicateLevel ("(", ") or (", ")");

		return data;
	}

	public Object visit (AndQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		// multiple predicates are 'and'ed together in XQuery
		node.acceptOperands (this, data);

		return data;
	}

	public Object visit (NotQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		if (node.getOperands().length == 0) {
			return data;
		}

		AbstractQuery query = (AbstractQuery) data;

		query.pushPredicateLevel();

		node.acceptOperands (this, data);

		query.popPredicateLevel ("fn:not((", ",", "))");

		return data;
	}

	public Object visit (ExactQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		AbstractQuery query = (AbstractQuery) data;

		query.addPropertyValuePredicate (propertyNameAsString (node), queryNodeValueAsString (node), "eq");

		return data;
	}

	public Object visit (NodeTypeQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		AbstractQuery query = (AbstractQuery) data;

		String typeName = queryNodeValueAsString (node);

		if ( ! NT_BASE_TYPE_NAME.equals (typeName)) {
			query.addPropertyValuePredicate (propertyNameAsString (node), queryNodeValueAsString (node), "eq");
		}

		return data;
	}

	public Object visit (PathQueryNode node, Object data)
	{
		LocationStepQueryNode [] stepNodes = node.getPathSteps();

		logger.log (logLevel, node.getClass().getName());

		for (int i = 0; i < stepNodes.length; i++) {
			LocationStepQueryNode stepNode = stepNodes [i];

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

	protected abstract String [] relationQueryPathStrings (RelationQueryNode RelationQueryNodenode);

	public Object visit (RelationQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		AbstractQuery query = (AbstractQuery) data;

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
		String value = ".";
		String operand = "";

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
			operand = node.getDateValue().toString();
			break;

		case 0:
			break;

		default:
			throw new RuntimeException ("bad type: " + node.getValueType() + ", opString: " + opString);
		}

		query.addPropertyValueTest (relationQueryPathStrings (node),
			opString, value, operand, function);

		return data;
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

		// FIXME: Unimplemented, no TCK tests exercise this path

		return data;
	}

	public Object visit (PropertyFunctionQueryNode node, Object data)
	{
		logger.log (logLevel, node.getClass().getName());

		// FIXME: Unimplemented, no TCK tests exercise this path

		return data;
	}

	// -------------------------------------------------------------

	// TODO: Need to catch other prefixes here?  General lookup?  Patch the parser?
	private String nsResolverHack (String stringValue)
	{
		if ( ! stringValue.startsWith ("nt:")) {
			return stringValue;
		}

		return "{" + NT_NS_NAME + "}" + stringValue.substring (3);
	}
}
