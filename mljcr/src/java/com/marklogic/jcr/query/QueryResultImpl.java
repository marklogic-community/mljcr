package com.marklogic.jcr.query;


import com.marklogic.jcr.fs.MarkLogicFileSystem;
import com.marklogic.jcr.persistence.AbstractPersistenceManager;

import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.uuid.UUID;

import javax.jcr.*;
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

	private final MarkLogicFileSystem mlfs;
	private final String [] uuids;
    private final Session session;

	public QueryResultImpl(MarkLogicFileSystem mlfs, String[] uuids, Session session)
	{
		this.uuids = uuids;
		this.mlfs = mlfs;
        this.session = session;

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
	public String[] getColumnNames () throws RepositoryException
	{
		return new String[0];  // FIXME: auto-generated

	}

	public RowIterator getRows () throws RepositoryException
	{
		return new RowIteratorAdapter (new ArrayList ());
	}

	public NodeIterator getNodes () throws RepositoryException
	{
		//System.out.println("SIZE"+rs.size());

		return new NodeIteratorImpl (mlfs, uuids, session);  //new NodeIteratorAdapter(new ArrayList());
	}

	private static class NodeIteratorImpl implements NodeIterator
	{
		private final MarkLogicFileSystem mlfs;
		private final String [] uuids;
        private final Session session;
		private int cursor = -1;

		private NodeIteratorImpl (MarkLogicFileSystem mlfs, String[] uuids, Session session)
		{
			this.mlfs = mlfs;
			this.uuids = uuids;
            this.session = session;
		}

		// ---------------------------------------------------
		// Implementation of NodeIterator interface

		public Node nextNode ()
		{
			if ( ! hasNext()) return null;

			cursor++;

			String uuid = uuids [cursor];


            Node n = null;
            try {
                n = session.getNodeByUUID(uuid);
            } catch (RepositoryException e) {
                //To change body of catch statement use File | Settings | File Templates.
            }
            return n;





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
			if ((cursor + l) > uuids.length) throw new IllegalStateException ("Skipped too far, max =" + getSize());

			cursor += l;
		}

		public long getSize ()
		{
			return uuids.length;
		}

		public long getPosition ()
		{
			return cursor;
		}

		// ---------------------------------------------------
		// Implementation of Iterator interface

		public boolean hasNext ()
		{
			return (getSize() != 0) && (cursor < getSize());
		}

		public Object next()
		{
			return nextNode();
		}

		public void remove ()
		{
			throw new UnsupportedOperationException ("Can't remove");
		}
	}


}
