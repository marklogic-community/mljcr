package com.marklogic.jcr.query;

import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.PropertyIterator;
import javax.jcr.Property;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.jcr.query.Row;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: paven
 * Date: Nov 26, 2008
 * Time: 8:21:37 AM
 */
public class QueryResultImpl implements QueryResult
{
	private static final String NS_JCR_URI = "http://www.jcp.org/jcr/1.0";
	private static final String SCORE_COL_NAME1 = "jcr:score";
	private static final String SCORE_COL_NAME2 =  "{" + NS_JCR_URI + "}:score";
	private static final String PATH_COL_NAME1 = "jcr:path";
	private static final String PATH_COL_NAME2 =  "{" + NS_JCR_URI + "}:path";
	private static final Value zeroValue = new LongValue (0);

	private final String[] uuids;
	private final Session session;
	private String [] columnNames = null;

	public QueryResultImpl (Session session, String[] propertySelectors, String[] uuids)
	{
		this.session = session;
		this.uuids = uuids;

		if ((propertySelectors != null) && (propertySelectors.length != 0)) {
			columnNames = propertySelectors;
		}
	}

	// --------------------------------------------------------
	// Implementation of QueryResult interface

	public String[] getColumnNames() throws RepositoryException
	{
		List propertyNames = new ArrayList();

		if (columnNames == null) {
			for (NodeIterator nit = getNodes(); nit.hasNext();) {
				Node node =  nit.nextNode();

				for (PropertyIterator pit = node.getProperties(); pit.hasNext();) {
					Property property = pit.nextProperty ();

					propertyNames.add (property.getName());
				}
			}

			propertyNames.add (PATH_COL_NAME1);

			columnNames = new String [propertyNames.size()];

			propertyNames.toArray (columnNames);
		}

		return columnNames;
	}

	public RowIterator getRows() throws RepositoryException
	{
		return new RowIteratorImpl (session, uuids);
	}

	public NodeIterator getNodes() throws RepositoryException
	{
		return new NodeIteratorImpl (session, uuids);
	}

	// -------------------------------------------------------

	private class RowIteratorImpl implements RowIterator
	{
		private final NodeIterator nodes;

		private RowIteratorImpl (Session session, String [] uuids)
		{
			nodes = new NodeIteratorImpl (session, uuids);
		}

		// -----------------------------------------------
		// Implementation of RowIterator interface

		public Row nextRow()
		{
			Node node = nodes.nextNode();

			if (node == null) return null;

			return new RowImpl (node);
		}

		// -----------------------------------------------
		// Implementation of RangeIterator interface

		public void skip (long l)
		{
			nodes.skip (l);
		}

		public long getSize()
		{
			return nodes.getSize();
		}

		public long getPosition()
		{
			return nodes.getPosition();
		}

		// -----------------------------------------------
		// Implementation of Iterator interface

		public boolean hasNext()
		{
			return nodes.hasNext();
		}

		public Object next()
		{
			return nextRow();
		}

		public void remove()
		{
			nodes.remove();
		}

		private class RowImpl implements Row
		{
			private final Node node;

			private RowImpl (Node node)
			{
				this.node = node;
			}

			public Value[] getValues() throws RepositoryException
			{
				String [] colNames = getColumnNames();
				Value [] values = new Value [colNames.length];

				for (int i = 0; i < colNames.length; i++) {
					values [i] = getValue (colNames [i]);
				}

				return values;
			}

			public Value getValue (String s) throws RepositoryException
			{
				if (SCORE_COL_NAME1.equals (s) || SCORE_COL_NAME2.equals (s)) {
					return zeroValue;
				}

				if (PATH_COL_NAME1.equals (s) || PATH_COL_NAME2.equals (s)) {
					return new StringValue (node.getPath());
				}

				return node.getProperty (s).getValue();
			}
		}
	}

	private class NodeIteratorImpl implements NodeIterator
	{
		private final String [] uuids;
		private final Session session;
		private int cursor = -1;

		private NodeIteratorImpl (Session session, String [] uuids)
		{
			this.session = session;
			this.uuids = uuids;
		}

		// ---------------------------------------------------
		// Implementation of NodeIterator interface

		public Node nextNode()
		{
			if (!hasNext()) {
				return null;
			}

			cursor++;

			try {
				return session.getNodeByUUID (uuids[cursor]);
			} catch (RepositoryException e) {
				throw new RuntimeException ("nextNode: " + e, e);
			}
		}

		// ---------------------------------------------------
		// Implementation of RangeIterator interface

		public void skip (long l)
		{
			if ((cursor + l) >= uuids.length) {
				throw new IllegalStateException ("Skipped too far, max =" + getSize());
			}

			cursor += l;
		}

		public long getSize()
		{
			return uuids.length;
		}

		public long getPosition()
		{
			return cursor;
		}

		// ---------------------------------------------------
		// Implementation of Iterator interface

		public boolean hasNext()
		{
			return (getSize() != 0) && (cursor < (getSize() - 1));
		}

		public Object next()
		{
			return nextNode();
		}

		public void remove()
		{
			throw new UnsupportedOperationException ("Can't remove");
		}
	}
}
