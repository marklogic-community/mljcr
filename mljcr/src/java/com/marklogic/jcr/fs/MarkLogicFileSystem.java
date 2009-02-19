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

package com.marklogic.jcr.fs;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.state.NodeReferencesId;

import java.io.InputStream;
import java.util.List;

/**
 * This interface extends the standard JackRabbit Filesystem interface
 * to add extra methods for Mark Logic specifc actions.
 *
 * @noinspection ConstantDeclaredInInterface
 */
public interface MarkLogicFileSystem extends FileSystem
{
	String MAGIC_EMPTY_BLOB_ID = "@=-empty-=@";
	String URI_PLACEHOLDER = "@DOCURI@";

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

	String[] runQuery (String docUri, String variableName, String query) throws FileSystemException;
}
