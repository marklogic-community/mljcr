/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.state.NodeReferencesId;

import javax.jcr.query.QueryResult;
import java.io.InputStream;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 21, 2008
 * Time: 3:11:49 PM
 */
public interface MarkLogicFileSystem extends FileSystem
{
	/** @noinspection ConstantDeclaredInInterface*/
	String MAGIC_EMPTY_BLOB_ID = "@=-empty-=@";

	String getUriRoot();

	SerializedItemState getNodeState (String uri, NodeId nodeId) throws FileSystemException;

	SerializedItemState getPropertyState (String uri, PropertyId propertyId) throws FileSystemException;

	SerializedItemState getReferencesState (String uri, NodeId nodeId) throws FileSystemException;

	boolean itemExists (String uri, NodeId nodeId) throws FileSystemException;

	boolean itemExists (String uri, PropertyId propertyId) throws FileSystemException;

	boolean itemExists (String uri, NodeReferencesId referencesId) throws FileSystemException;

	InputStream nodeStateAsStream (String uri, NodeId nodeId) throws FileSystemException;

	InputStream propertyStateAsStream (String uri, PropertyId propertyId) throws FileSystemException;

	InputStream referencesStateAsStream (String uri, NodeId nodeId) throws FileSystemException;

	void applyStateUpdate (String workspaceDocUri, String changeListPath,
		String deltas, List contentList)
		throws FileSystemException;

    QueryResult runQuery(String query) throws FileSystemException;

    //another method for query (MLQuery,
}
