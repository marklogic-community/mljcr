package com.marklogic.jcr.query;


import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import javax.jcr.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.types.XdmItem;
import com.marklogic.jcr.fs.MarkLogicFileSystem;

/**
 * Created by IntelliJ IDEA.
 * User: paven
 * Date: Nov 26, 2008
 * Time: 8:21:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class QueryResultImpl implements QueryResult {
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

    private final ResultSequence rs;
    private final MarkLogicFileSystem mlfs;
    
    public QueryResultImpl(ResultSequence rs, MarkLogicFileSystem mlfs){

        this.rs = rs;
        this.mlfs= mlfs;
        
            System.out.println("======================= "+rs.size());

            String x[] = rs.asStrings();
            for(int i=0; i<x.length;i++){
            System.out.println(x[i]);
            }

            System.out.println("-------------------------------DO MAPPING----------------------------");

    }

    /**
     * {@inheritDoc}
     */
	public String[] getColumnNames () throws RepositoryException
	{
        Iterator i = rs.iterator();
        while (i.hasNext()){
            XdmItem ri = (XdmItem)i.next();

        }
		return  new String[0];  // FIXME: auto-generated

	}

	public RowIterator getRows () throws RepositoryException
	{
		return new RowIteratorAdapter(new ArrayList());
	}

	public NodeIterator getNodes () throws RepositoryException
	{
        //System.out.println("SIZE"+rs.size());
        
		return new NodeIteratorAdapter(rs.iterator());  //new NodeIteratorAdapter(new ArrayList());
	}

    private static class NodeIteratorImpl implements NodeIterator
    {
       private final ResultSequence rs;
       private final Iterator rsIter;

        private NodeIteratorImpl(ResultSequence rs) {
            this.rs = rs;
            this.rsIter = rs.iterator();

        }

        // ---------------------------------------------------
        // Implementation of NodeIterator

        public Node nextNode() {

            //implementation of next node, takes id, and queries for node


            //get the next item from xcc result sequence
            //parse it
            //build a Node
            //return Node
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        // ---------------------------------------------------
        // Implementation of RangeIterator

        public void skip(long l) {
            for (int i = 0; (rsIter.hasNext() && (i < l)); i++) {
                rsIter.next();
            }
        }

        public long getSize() {
            return rs.size();
        }

        public long getPosition() {
            return  (rs.current() == null) ? -1 : rs.current().getIndex();
        }

        // ---------------------------------------------------
        // IMplementation of Iterator interface

        public boolean hasNext() {
            return rsIter.hasNext();
        }

        public Object next() {
            return rsIter.next();
        }

        public void remove() {
            rsIter.remove();
        }
    }


}
