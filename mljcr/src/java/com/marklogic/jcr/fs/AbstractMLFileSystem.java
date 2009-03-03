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

import com.marklogic.jcr.compat.PMAdapter;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XSBoolean;
import com.marklogic.xcc.types.XSInteger;
import com.marklogic.xcc.types.XdmVariable;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.fs.FileSystemPathUtil;
import org.apache.jackrabbit.core.fs.FileSystemResource;
import org.apache.jackrabbit.core.fs.RandomAccessOutputStream;
import org.apache.jackrabbit.core.state.NodeReferencesId;
import org.apache.jackrabbit.core.value.BLOBFileValue;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.util.TransientFileFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 4, 2008
 * Time: 8:24:03 PM
 * @noinspection ClassWithTooManyMethods,OverlyComplexClass
 */
abstract public class AbstractMLFileSystem implements MarkLogicFileSystem
{
	private static final Logger logger = Logger.getLogger (MarkLogicFileSystem.class.getName());
	private static final String DEFAULT_LOG_LEVEL = "FINE";

	private final Level logLevel;
	private static final int DEFAULT_METADATA_CACHE_SIZE = 256;
	private static final int DEFAULT_ITEMSTATE_CACHE_SIZE = 64;
	private static final String MODULES_ROOT = "/MarkLogic/jcr/";
	private static final String FS = "filesystem/";
	private static final String STATE = "state/";
	private final PMAdapter pmAdapter;

	private FileMetaDataLruCache metaDataCache = null;
	private ItemStateLruCache itemStateCache = null;

	// bean properties
	private String contentSourceUrl = null;
	private String uriRoot = null;
	private int metaDataCacheSize = DEFAULT_METADATA_CACHE_SIZE;
	private int itemStateCacheSize = DEFAULT_ITEMSTATE_CACHE_SIZE;

	private ContentSource contentSource = null;

	// ------------------------------------------------------------

	public AbstractMLFileSystem (PMAdapter pmAdapter)
	{
		this.pmAdapter = pmAdapter;
		String levelName = System.getProperty ("mljcr.log.level", DEFAULT_LOG_LEVEL);
		logLevel = Level.parse (levelName);
	}

	// ------------------------------------------------------------
	// bean properties set by RepositoryConfig object

	public String getContentSourceUrl()
	{
		return contentSourceUrl;
	}

	public void setContentSourceUrl (String contentSourceUrl)
	{
		this.contentSourceUrl = contentSourceUrl;
	}

	public String getUriRoot()
	{
		return uriRoot;
	}

	public void setUriRoot (String uriRootParam)
	{
		String uriRoot = uriRootParam.replaceAll("\\\\", "/");

		if (uriRoot.startsWith ("/")) {
			this.uriRoot = uriRoot;
		} else {
			this.uriRoot = "/" + uriRoot;
		}
	}

	public FileMetaDataLruCache getMetaDataCache()
	{
		return metaDataCache;
	}

	public void setMetaDataCache (FileMetaDataLruCache metaDataCache)
	{
		this.metaDataCache = metaDataCache;
	}

	// ------------------------------------------------------------

	public void init() throws FileSystemException
	{
		logger.log (logLevel, "init: path=" + uriRoot + ", uri=" + contentSourceUrl);

		try {
			URI uri = new URI (contentSourceUrl);
			contentSource = ContentSourceFactory.newContentSource (uri);
		} catch (URISyntaxException e) {
			throw new FileSystemException ("Bad XCC connection URL: " + e, e);
		} catch (XccConfigException e) {
			throw new FileSystemException ("Cannot create ContentSource: " + e, e);
		}

		sanityCheckConnection();

		metaDataCache = new FileMetaDataLruCache (metaDataCacheSize);
		itemStateCache = new ItemStateLruCache (itemStateCacheSize);
	}

	public void close()
	{
		// nothing to do here
	}

	// ------------------------------------------------------------
	// Implementation of FileSystem interface

	private static final String GET_METADATA_MODULE = FS + "get-metadata.xqy";

	private FileMetaData getFileMetaData (String path) throws FileSystemException
	{
		FileMetaData meta = metaDataCache.get (path);

		if (meta != null) {
			logger.log (logLevel, "getFileMetaData: found in cache, path=" + path);

			return meta;
		}

		if (path.endsWith (MAGIC_EMPTY_BLOB_ID)) {
			logger.log (logLevel, "getFileMetaData: creating dummy metadata object, path=" + path);

			meta = new FileMetaData (0, 0, false);

			metaDataCache.put (path, meta);

			return meta;
		}

		logger.log (logLevel, "getFileMetaData: querying for metadata, path=" + path);

		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		ResultSequence rs = runModule (GET_METADATA_MODULE, var);

		if (rs.size() == 0) {
			logger.log (logLevel, "getFileMetaData: does not exist in DB, path=" + path);

			return null;
		}

		long size = ((XSInteger) ((rs.next ().getItem ()))).asPrimitiveLong();
		long lastModified = ((XSInteger) ((rs.next ().getItem ()))).asPrimitiveLong();
		boolean isDir = ((XSBoolean) ((rs.next ().getItem ()))).asPrimitiveBoolean();

		meta = new FileMetaData (size, lastModified, isDir);

		metaDataCache.put (path, meta);

		logger.log (logLevel, "getFileMetaData: added to cache, path=" + path);

		return meta;
	}

	// ------------------------------------------------------------

	private static final String GET_DOC_MODULE = FS + "get-doc.xqy";

	public InputStream getInputStream (String filePath) throws FileSystemException
	{
		String uri = fullPath (filePath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		logger.log (logLevel, "getInputStream: filePath=" + uri);

		if (filePath.endsWith (MAGIC_EMPTY_BLOB_ID)) {
			return new ByteArrayInputStream (new byte [0]);
		}

		ResultSequence rs = runModule (GET_DOC_MODULE, var);

		if ( ! rs.hasNext()) {
			throw new FileSystemException ("Cannot fetch document: " + filePath);
		}

		return rs.next().asInputStream();
	}

	public OutputStream getOutputStream (String filePath) throws FileSystemException
	{
		metaDataCache.remove (filePath);

		final String uri = fullPath (filePath);

		logger.log (logLevel, "getOutputStream: filePath=" + uri);

		FileSystemPathUtil.checkFormat (filePath);

		if (isFolder (uri)) {
		    throw new FileSystemException ("Path is a folder: " + filePath);
		}

		try {
			TransientFileFactory fileFactory = TransientFileFactory.getInstance();
			final File tmpFile = fileFactory.createTransientFile ("jcrInsert", ".tmp", null);

			return new FilterOutputStream (new FileOutputStream (tmpFile)) {
				public void close() throws IOException
				{
					super.close();

					ContentCreateOptions options = optionsForUri (uri);
					Content content = ContentFactory.newContent (uri, tmpFile, options);
					Session session = contentSource.newSession();

					try {
						session.insertContent (content);
					} catch (RequestException e) {
						throw new IOException ("Inserting " + uri + ": " + e.getMessage());
					} finally {
						// temp file can now safely be removed
						//noinspection ResultOfMethodCallIgnored
						tmpFile.delete();
					}
				}
			};
		} catch (Exception e) {
		    String msg = "Failed to open output stream to file: " + filePath;
		    logger.log (Level.SEVERE, msg, e);
		    throw new FileSystemException (msg, e);
		}
	}

	public RandomAccessOutputStream getRandomAccessOutputStream (String filePath)
		throws FileSystemException, UnsupportedOperationException
	{
		metaDataCache.remove (filePath);

		logger.log (logLevel, "getRandomAccessOutputStream: filePath=" + filePath);

		throw new FileSystemException ("RandomAccessOutputStream NOT IMPLEMENTED");
	}

	private static final String CREATE_FOLDER_MODULE = FS + "create-folder.xqy";

	public void createFolder (String folderPath) throws FileSystemException
	{
		metaDataCache.remove (folderPath);

		String uri = fullDirPath (folderPath);

		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		logger.log (logLevel, "createFolder: folderPath=" + uri);

		runModule (CREATE_FOLDER_MODULE, var);
	}

	public boolean exists (String path) throws FileSystemException
	{
		FileMetaData meta = getFileMetaData (path);

		logger.log (logLevel, "exists: path=" + path + ": " + (meta != null));

		return (meta != null);
	}

	public boolean isFile (String path) throws FileSystemException
	{
		FileMetaData meta = getFileMetaData (path);

		if ((meta == null) || meta.isDirectory()) {
			logger.log (logLevel, "isFile: path=" + path + ": false");
			return false;
		}

		logger.log (logLevel, "isFile: path=" + path + ": true");

		return true;
	}

	public boolean isFolder (String path) throws FileSystemException
	{
		FileMetaData meta = getFileMetaData (path);

		if ((meta == null) || (! meta.isDirectory())) {
			logger.log (logLevel, "isFolder: path=" + path + ": false");
			return false;
		}

		logger.log (logLevel, "isFolder: path=" + path + ": true");

		return true;
	}

	public long length (String filePath) throws FileSystemException
	{
		FileMetaData meta = getFileMetaData (filePath);

		if (meta == null) {
			logger.log (logLevel, "length: path=" + filePath + ": does not exist, returning -1");
			return -1;
		}

		logger.log (logLevel, "length: path=" + filePath + ": " + meta.getSize());

		return meta.getSize();
	}

	public long lastModified (String path) throws FileSystemException
	{
		FileMetaData meta = getFileMetaData (path);

		if (meta == null) {
			return -1;
		}

		return meta.getLastModified();
	}

	public void touch (String filePath) throws FileSystemException
	{
		metaDataCache.remove (filePath);

		logger.log (logLevel, "touch: folderPath=" + filePath);

		throw new FileSystemException ("NOT IMPL");
	}

	private static final String HAS_CHILDREN_MODULE = FS + "has-children.xqy";

	public boolean hasChildren (String path) throws FileSystemException
	{
		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		boolean result = runBinaryModule (HAS_CHILDREN_MODULE, var);

		logger.log (logLevel, "hasChildren: folderPath=" + path + ", result=" + result);

		return result;
	}

	private static final String LIST_CHILDREN_MODULE = FS + "list-children.xqy";

	public String[] list (String folderPath) throws FileSystemException
	{
		logger.log (logLevel, "list: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		ResultSequence rs = runModule (LIST_CHILDREN_MODULE, var);

		return rs.asStrings();
	}

	private static final String LIST_FILES_MODULE = FS + "list-files.xqy";

	public String[] listFiles (String folderPath) throws FileSystemException
	{
		logger.log (logLevel, "listFiles: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		ResultSequence rs = runModule (LIST_FILES_MODULE, var);

		return rs.asStrings();
	}

	private static final String LIST_FOLDERS_MODULE = FS + "list-folders.xqy";

	public String[] listFolders (String folderPath) throws FileSystemException
	{
		logger.log (logLevel, "listFolders: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		ResultSequence rs = runModule (LIST_FOLDERS_MODULE, var);

		return rs.asStrings();
	}

	private static final String DELETE_FILE_MODULE = FS + "delete-file.xqy";

	public void deleteFile (String filePath) throws FileSystemException
	{
		metaDataCache.remove (filePath);

		logger.log (logLevel, "deleteFile: folderPath=" + filePath);

		String uri = fullPath (filePath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		runModule (DELETE_FILE_MODULE, var);
	}

	private static final String DELETE_FOLDER_MODULE = FS + "delete-folder.xqy";

	public void deleteFolder (String folderPath) throws FileSystemException
	{
		metaDataCache.clear();

		logger.log (logLevel, "deleteFolder: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		runModule (DELETE_FOLDER_MODULE, var);
	}

	public void move (String srcPath, String destPath) throws FileSystemException
	{
		logger.log (logLevel, "move: srcPath=" + srcPath + ", destPath" + destPath);

		throw new FileSystemException ("move NOT IMPLEMENTED");
	}

	public void copy (String srcPath, String destPath) throws FileSystemException
	{
		logger.log (logLevel, "copy: srcPath=" + srcPath + ", destPath" + destPath);

		throw new FileSystemException ("copy NOT IMPLEMENTED");
	}

	// ------------------------------------------------------------
	// ------------------------------------------------------------
	// Caching related stuff

	private SerializedItemState findItemState (String key)
	{
		SerializedItemState state = itemStateCache.get (key);

		if (state == null) {
			logger.log (logLevel, "(" + key + "): not in cache");
			return null;
		}

		logger.log (logLevel, "(" + key + "): found in cache");

		return state;
	}

	private void clearItemStateCache()
	{
		logger.log (logLevel, "clearing item state cache");
		itemStateCache.clear();
	}

	private String nodeHashKey (NodeId nodeId)
	{
		return nodeId.getUUID().toString();
	}

	private String propertyHashKey (PropertyId propertyId)
	{
		return propertyId.getParentId().getUUID().toString() + "|" + pmAdapter.getPropertyIdNameAsString (propertyId);
	}

	private String referencesHashKey (NodeId nodeId)
	{
		return "refs-" + nodeHashKey (nodeId);
	}

	private SerializedItemState cachedItemState (String hashKey, String moduleName,
		XdmVariable uri, XdmVariable uuid, XdmVariable name)
		throws FileSystemException
	{
		SerializedItemState state = findItemState (hashKey);

		if (state != null) {
			return state;
		}

		ResultSequence rs = runModule (moduleName, uri, uuid, name);

		if (rs.size() == 0) {
			logger.log (logLevel, "uri=" + hashKey + " not found in repo");
			return null;
		}

		logger.log (logLevel, "uri=" + hashKey + ", was found in repo");

		try {
			state = new SerializedItemState (hashKey, rs.next().asInputStream());
		} catch (IOException e) {
			throw new FileSystemException (e.getMessage(), e);
		}

		itemStateCache.put (hashKey, state);

		return state;
	}

	// ----------------------------------------------------------------

	private static final String QUERY_NODE_STATE_MODULE = STATE + "query-node-state.xqy";

	public SerializedItemState getNodeState (String uri, NodeId nodeId) throws FileSystemException
	{
		String hashKey = nodeHashKey (nodeId);
		String fullPathUri = fullPath (uri);
		XdmVariable uriVar = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPathUri));
		XdmVariable uuid = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (nodeId.getUUID().toString()));

		return cachedItemState (hashKey, QUERY_NODE_STATE_MODULE, uriVar, uuid, null);
	}

	private static final String QUERY_PROP_STATE_MODULE = STATE + "query-property-state.xqy";

	public SerializedItemState getPropertyState (String uri, PropertyId propertyId) throws FileSystemException
	{
		String hashKey = propertyHashKey (propertyId);
		String fullPathUri = fullPath (uri);
		XdmVariable uriVar = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPathUri));
		XdmVariable uuid = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (propertyId.getParentId().getUUID().toString()));
		XdmVariable name = ValueFactory.newVariable (new XName ("name"), ValueFactory.newXSString (FileSystemPathUtil.escapeName (pmAdapter.getPropertyIdNameAsString (propertyId))));

		return cachedItemState (hashKey, QUERY_PROP_STATE_MODULE, uriVar, uuid, name);
	}

	private static final String QUERY_REFS_STATE_MODULE = STATE + "query-references-state.xqy";

	public SerializedItemState getReferencesState (String uri, NodeId nodeId) throws FileSystemException
	{
		String hashKey = referencesHashKey (nodeId);
		String fullPathUri = fullPath (uri);
		XdmVariable uriVar = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPathUri));
		XdmVariable uuid = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (nodeId.getUUID().toString()));

		return cachedItemState (hashKey, QUERY_REFS_STATE_MODULE, uriVar, uuid, null);
	}

	public boolean itemExists (String uri, NodeId nodeId) throws FileSystemException
	{
		boolean result = getNodeState (uri, nodeId) != null;

		logger.log (logLevel, "(node): uri=" + fullPath (uri) + ", uuid=" + nodeId.getUUID().toString());

		return result;
	}

	public boolean itemExists (String uri, PropertyId propertyId) throws FileSystemException
	{
		boolean result = getPropertyState (uri, propertyId) != null;

		logger.log (logLevel, "(property): uri=" + fullPath (uri) + ", uuid=" + propertyId.getParentId().getUUID().toString());

		return result;
	}

	public boolean itemExists (String uri, NodeReferencesId referencesId) throws FileSystemException
	{
		boolean result = getReferencesState (uri, referencesId.getTargetId()) != null;

		logger.log (logLevel, "(references): uri=" + fullPath (uri) + ", uuid=" + referencesId.getTargetId().getUUID().toString());

		return result;
	}

	public InputStream nodeStateAsStream (String uri, NodeId nodeId) throws FileSystemException
	{
		SerializedItemState state = getNodeState (uri, nodeId);

		return (state == null) ? null : state.getInputStream();
	}

	public InputStream propertyStateAsStream (String uri, PropertyId propertyId) throws FileSystemException
	{
		SerializedItemState state = getPropertyState (uri, propertyId);

		return (state == null) ? null : state.getInputStream();
	}

	public InputStream referencesStateAsStream (String uri, NodeId nodeId) throws FileSystemException
	{
		SerializedItemState state = getReferencesState (uri, nodeId);

		return (state == null) ? null : state.getInputStream();
	}

	// ------------------------------------------------------------
	// Mark Logic extensions for updating workspace state

	private static final String UPDATE_STATE_MODULE = STATE + "update-state.xqy";

	public void applyStateUpdate (String workspaceDocUri, String changeListPath,
		String deltas, List contentList)
		throws FileSystemException
	{
		long startTime = System.currentTimeMillis();

		clearItemStateCache();

		String fullWsDocUri = fullPath (workspaceDocUri);

		if (logger.isLoggable (logLevel)) {
			logger.log (logLevel, "+Begin: workspaceDocUri=" + fullWsDocUri + ", contentsize=" + contentList.size());
		}

		ContentCreateOptions options = ContentCreateOptions.newBinaryInstance();
		Content [] blobs = new Content [contentList.size() + 1];
		String changeListUri = uriRoot + changeListPath;

		blobs [0] = ContentFactory.newContent (changeListUri, deltas, ContentCreateOptions.newXmlInstance());

		int i = 1;

		for (Iterator it = contentList.iterator(); it.hasNext();) {
			PropertyBlob blob = (PropertyBlob) it.next();
			BLOBFileValue blobVal = blob.getBlobFileValue();
			String uri = uriRoot + blob.getBlobId();

			try {
				blobs [i++] = new SemiBufferedContent (uri, options, blobVal.getStream(), 100 * 1024);
			} catch (Exception e) {
				throw new FileSystemException ("Preparing blob inserts: " + e, e);
			}
		}

		try {
			contentSource.newSession().insertContent (blobs);
		} catch (RequestException e) {
			throw new FileSystemException ("Inserting transaction data: " + e, e);
		}

		long prepareDone = System.currentTimeMillis();

		XdmVariable var1 = ValueFactory.newVariable (new XName ("state-doc-uri"), ValueFactory.newXSString (fullWsDocUri));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("workspace-root"), ValueFactory.newXSString (uriRoot));
		XdmVariable var3 = ValueFactory.newVariable (new XName ("deltas-uri"), ValueFactory.newXSString (changeListUri));

		ResultSequence rs = runModule (UPDATE_STATE_MODULE, var1, var2, var3);
		Map blobMap = new HashMap (rs.size());

		if (rs.size() != contentList.size()) {
			throw new FileSystemException ("Blob list size mismatch: content=" + contentList.size() + ", rs=" + rs.size());
		}

		long updateDone = System.currentTimeMillis();

		while (rs.hasNext()) {
			String blobPathItem = rs.next().asString();
			String [] strings = blobPathItem.split ("\\|");

			if (strings.length != 3) throw new FileSystemException ("Bad blob path item: " + blobPathItem);

			blobMap.put (strings [0] +"|" + strings [1], strings [2]);
		}

		for (Iterator it = contentList.iterator(); it.hasNext();) {
			PropertyBlob blob = (PropertyBlob) it.next();
			String hashKey = blob.getPropertyHashKey();
			String newBlobPath = (String) blobMap.get (hashKey);

			if (newBlobPath == null) throw new FileSystemException ("Missing blob path: " + blob.getPropertyHashKey());

			blobMap.remove (hashKey);

			FileSystemResource fsRes = new FileSystemResource (this, newBlobPath);

			try {
				blob.getPropertyState().getValues() [blob.getValueIndex()] = InternalValue.create (fsRes);
			} catch (IOException e) {
				throw new FileSystemException ("Updating PropertyState blob value: " + e, e);
			}

			blob.getBlobFileValue().discard();
		}

		if (blobMap.size () != 0) throw new FileSystemException ("Blob map inconsistency: size=" + blobMap.size());

		if (logger.isLoggable (logLevel)) {
			logger.log (logLevel, "-End:   workspaceDocUri=" + fullWsDocUri);

			logger.log (logLevel, "Insert: " + fmttime (prepareDone - startTime) +
				", New State: " + fmttime (updateDone - prepareDone) +
				", Total: " + fmttime (System.currentTimeMillis () - startTime) +
				", new blobs: " + rs.size() +
				", WS: " + fullWsDocUri);
		}
	}

	private String fmttime (long interval)
	{
		StringBuffer sb = new StringBuffer();

		sb.append (interval / 1000).append (".");

		long millis = interval % 1000;

		if (millis < 10) sb.append ("0");
		if (millis < 100) sb.append ("0");

		sb.append (millis);

		return sb.toString();
	}

	// ------------------------------------------------------------

	private static final String SANITY_MODULE = FS + "startup-check.xqy";

	private void sanityCheckConnection() throws FileSystemException
	{
		Session session = contentSource.newSession();
		Request request = session.newModuleInvoke (modulePath (SANITY_MODULE));

		try {
			session.submitRequest (request);
			// TODO: check version, etc
		} catch (RequestException e) {
			logger.log (Level.SEVERE, "Sanity check caught exception: " + e, e);
			throw new FileSystemException ("Cannot establish connection with " + contentSourceUrl + ", " + e);
		}
	}

	// ------------------------------------------------------------

	private String modulePath (String module)
	{
		return (MODULES_ROOT + module);
	}

	private String fullPath (String relPath)
	{
		String sep = (uriRoot.endsWith ("/") || relPath.startsWith ("/")) ? "" : "/";
		return (uriRoot + sep + relPath);
	}

	private String fullDirPath (String relPath)
	{
		if (relPath.endsWith ("/")) {
			return fullPath (relPath);
		}

		return uriRoot + relPath + "/";
	}

	// ------------------------------------------------------------

	private ContentCreateOptions optionsForUri (String uri)
	{
		if (isXmlType (uri)) {
			return ContentCreateOptions.newXmlInstance();
		} else if (isTextType (uri)) {
			return ContentCreateOptions.newTextInstance();
		} else {
			return ContentCreateOptions.newBinaryInstance();
		}
	}

	private boolean isXmlType (String uri)
	{
		if (uri.endsWith (".xml") ||
			uri.endsWith (".xsd") ||
			uri.endsWith (".svg"))
		{
			return true;
		}

		return false;
	}

	private boolean isTextType (String uri)
	{
		if (uri.endsWith (".txt") ||
			uri.endsWith (".css") ||
			uri.endsWith (".dtd") ||
			uri.endsWith (".properties"))
		{
			return true;
		}

		return false;
	}

	private ResultSequence runModule (String module, XdmVariable var1,
		XdmVariable var2, XdmVariable var3)
		throws FileSystemException
	{
		Session session = contentSource.newSession();
		Request request = session.newModuleInvoke (modulePath (module));

		if (var1 != null) request.setVariable (var1);
		if (var2 != null) request.setVariable (var2);
		if (var3 != null) request.setVariable (var3);

		try {
			return (session.submitRequest (request));
		} catch (RequestException e) {
			throw new FileSystemException ("cannot run Mark Logic request: " + e, e);
		}
	}

	private ResultSequence runModule (String module, XdmVariable var)
		throws FileSystemException
	{
		return runModule (module, var, null, null);
	}

	private boolean runBinaryModule (String module, XdmVariable var1,
		XdmVariable var2, XdmVariable var3)
		throws FileSystemException
	{
		ResultSequence rs = runModule (module, var1, var2, var3);
		ResultItem item = rs.next();
		XSBoolean bool = (XSBoolean) item.getItem();

		return bool.asPrimitiveBoolean();
	}

	private boolean runBinaryModule (String module, XdmVariable var)
		throws FileSystemException
	{
		return runBinaryModule (module, var, null, null);
	}

	// ------------------------------------------------------------

	public String [] runQuery (String docName, String variableName, String query) throws FileSystemException
	{
		String docUri = fullPath (docName);
		Session session = contentSource.newSession();
		Request request = session.newAdhocQuery (query.replaceAll (URI_PLACEHOLDER, docUri));

		request.setVariable (ValueFactory.newVariable (new XName (variableName), ValueFactory.newXSString (docUri)));

		try {
			ResultSequence rs = session.submitRequest (request);
			return rs.asStrings();
		} catch (RequestException e) {
			throw new FileSystemException ("cannot run Mark Logic query: " + e, e);
		}
	}
}
