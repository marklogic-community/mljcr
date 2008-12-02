package com.marklogic.jcr.query;

import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

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

	/*      public QueryResultImpl(SearchIndex index,
				   ItemManager itemMgr,
				   NamePathResolver resolver,
				   AccessManager accessMgr,
				   AbstractQueryImpl queryImpl,
				   Query query,
				   SpellSuggestion spellSuggestion,
				   Name[] selectProps,
				   Name[] orderProps,
				   boolean[] orderSpecs,
				   boolean documentOrder,
				   long offset,
				   long limit) throws RepositoryException {
		this.index = index;
		this.itemMgr = itemMgr;
		this.resolver = resolver;
		this.accessMgr = accessMgr;
		this.queryImpl = queryImpl;
		this.query = query;
		this.spellSuggestion = spellSuggestion;
		this.selectProps = selectProps;
		this.orderProps = orderProps;
		this.orderSpecs = orderSpecs;
		this.docOrder = orderProps.length == 0 && documentOrder;
		this.offset = offset;
		this.limit = limit;
		// if document order is requested get all results right away
		getResults(docOrder ? Integer.MAX_VALUE : index.getResultFetchSize());
	    }
	    */

	private final String[] uuids;
	private final Session session;

	public QueryResultImpl (Session session, String[] uuids)
	{
		this.session = session;
		this.uuids = uuids;

//            System.out.println("======================= "+rs.size());
//
//            String x[] = rs.asStrings();
//            for(int i=0; i<x.length;i++){
//            System.out.println(x[i]);
//            }
//
//            System.out.println("-------------------------------DO MAPPING----------------------------");
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getColumnNames() throws RepositoryException
	{
		return new String[0];  // FIXME: auto-generated
	}

	public RowIterator getRows() throws RepositoryException
	{
		return new RowIteratorAdapter (new ArrayList());
	}

	public NodeIterator getNodes() throws RepositoryException
	{
		//System.out.println("SIZE"+rs.size());

		return new NodeIteratorImpl (session, uuids);  //new NodeIteratorAdapter(new ArrayList());
	}

	private static class NodeIteratorImpl implements NodeIterator
	{
		private final String[] uuids;
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
			if (!hasNext ()) {
				return null;
			}

			cursor++;

			try {
				return session.getNodeByUUID (uuids [cursor]);
			} catch (RepositoryException e) {
				throw new RuntimeException ("nextNode: " + e, e);
			}

			//implementation of next node, takes id, and queries for node

			//get the next item from xcc result sequence
			//parse it
			//build a Node
			//return Node
//			return null;  //To change body of implemented methods use File | Settings | File Templates.
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
			return (getSize() != 0) && (cursor < getSize());
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