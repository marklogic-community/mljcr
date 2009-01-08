package com.marklogic.jcr.query;

import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ItemNotFoundException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.jcr.query.Row;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: paven
 * Date: Nov 26, 2008
 * Time: 8:21:37 AM
 */
public class QueryResultImpl implements QueryResult
{
	//  java.lang.String[] getColumnNames() throws javax.jcr.RepositoryException;

	// javax.jcr.query.RowIterator getRows() throws javax.jcr.RepositoryException;

	//  javax.jcr.NodeIterator getNodes() throws javax.jcr.RepositoryException;

	private final String[] uuids;
	private final Session session;

	public QueryResultImpl (Session session, String[] uuids)
	{
		this.session = session;
		this.uuids = uuids;

//        System.out.println("WORKSPACE NAME: "+session.getWorkspace().getName());
//        System.out.println("UUIDS LENGTH: "+this.uuids.length);
//        System.out.println("-------------------------------DO MAPPING----------------------------");
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getColumnNames () throws RepositoryException
	{
		return new String[0];  // FIXME: auto-generated
	}

	public RowIterator getRows() throws RepositoryException
	{
		return new RowIteratorImpl (session, uuids);
	}

	public NodeIterator getNodes () throws RepositoryException
	{
		//System.out.println("SIZE"+rs.size());

		return new NodeIteratorImpl (session, uuids);  //new NodeIteratorAdapter(new ArrayList());
	}

	private static class RowIteratorImpl implements RowIterator
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

		private static class RowImpl implements Row
		{
			private final Node node;

			private RowImpl (Node node)
			{
				this.node = node;
			}

			public Value[] getValues() throws RepositoryException
			{
				return new Value[0];  // FIXME: auto-generated
			}

			public Value getValue (String s) throws RepositoryException
			{
				return node.getProperty (s).getValue ();
			}
		}
	}

	private static class NodeIteratorImpl implements NodeIterator
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
