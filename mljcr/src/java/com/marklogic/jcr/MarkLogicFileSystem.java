/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr;

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
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.fs.FileSystemPathUtil;
import org.apache.jackrabbit.core.fs.RandomAccessOutputStream;
import org.apache.jackrabbit.core.state.NodeReferencesId;
import org.apache.jackrabbit.util.TransientFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 4, 2008
 * Time: 8:24:03 PM
 * @noinspection ClassWithTooManyMethods
 */
public class MarkLogicFileSystem implements FileSystem
{
	private static final Logger log = LoggerFactory.getLogger (MarkLogicFileSystem.class);

	private static final String MODULE_ROOT = "filesystem/";

	// bean properties
	private String contentSourceUrl = null;
	private String uriRoot = null;

	private ContentSource contentSource = null;

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

	public String getUriRoot ()
	{
		return uriRoot;
	}

	public void setUriRoot (String uriRoot)
	{
		if (uriRoot.startsWith ("/")) {
			this.uriRoot = uriRoot;
		} else {
			this.uriRoot = "/" + uriRoot;
		}
	}

	// ------------------------------------------------------------

	public void init() throws FileSystemException
	{
		log.info ("init: path=" + uriRoot + ", uri=" + contentSourceUrl);

		try {
			URI uri = new URI (contentSourceUrl);
			contentSource = ContentSourceFactory.newContentSource (uri);
		} catch (URISyntaxException e) {
			throw new FileSystemException ("Bad XCC connection URL: " + e, e);
		} catch (XccConfigException e) {
			throw new FileSystemException ("Cannot create ContentSource: " + e, e);
		}

		// TODO: fetch db metadata, check modules setup, store modules?
		// TODO: create/check repo dir?
	}

	public void close() throws FileSystemException
	{
		// TODO: anything to do here?
	}

	private static final String GET_DOC_MODULE = "get-doc.xqy";

	public InputStream getInputStream (String filePath) throws FileSystemException
	{
		String uri = fullPath (filePath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		log.info ("getInputStream: filePath=" + uri);

		ResultSequence rs = runModule (GET_DOC_MODULE, var);

		if ( ! rs.hasNext()) {
			throw new FileSystemException ("Cannot fetch document: " + filePath);
		}

		return rs.next().asInputStream();
	}

	public OutputStream getOutputStream (String filePath) throws FileSystemException
	{
		final String uri = fullPath (filePath);

		log.info ("getOutputStream: filePath=" + uri);

		FileSystemPathUtil.checkFormat (filePath);

		if (isFolder (uri)) {
		    throw new FileSystemException ("Path is a folder: " + filePath);
		}

		try {
			TransientFileFactory fileFactory = TransientFileFactory.getInstance();
			final File tmpFile = fileFactory.createTransientFile ("jcrInsert", ".tmp", null);

			//noinspection OverlyComplexAnonymousInnerClass
			return new FilterOutputStream (new FileOutputStream (tmpFile)) {
				public void close() throws IOException
				{
					super.close();

					ContentCreateOptions options = optionsForUri (uri);
					Content content = ContentFactory.newContent (uri, tmpFile, options);
					Session session = contentSource.newSession();

//		pukeFile (tmpFile);
					try {
						session.insertContent (content);
					} catch (RequestException e) {
						throw new IOException ("Inserting " + uri + ": " + e.getMessage());
					} finally {
					    // temp file can now safely be removed
					    tmpFile.delete();
					}
				}

//				private void pukeFile (File tmpFile) throws IOException
//				{
//					System.out.println ("File: " + uri);
//					if ( ! uri.endsWith (".xml")) return;
//
//					InputStream in = new FileInputStream (tmpFile);
//					byte [] buf = new byte [1024];
//					int rc;
//
//					System.out.println ("========");
//					while ((rc = in.read (buf)) != -1) {
//						System.out.write (buf, 0, rc);
//						System.out.flush();
//					}
//
//					System.out.println ("========");
//					in.close();
//				}
			};
		} catch (Exception e) {
		    String msg = "Failed to open output stream to file: " + filePath;
		    log.error (msg, e);
		    throw new FileSystemException (msg, e);
		}
	}

	// FIXME: Implement this
	public RandomAccessOutputStream getRandomAccessOutputStream (String filePath)
		throws FileSystemException, UnsupportedOperationException
	{
		log.info ("getRandomAccessOutputStream: filePath=" + filePath);

		throw new FileSystemException ("NOT IMPL");
	}

	private static final String CREATE_FOLDER_MODULE = "create-folder.xqy";

	public void createFolder (String folderPath) throws FileSystemException
	{
		String uri = fullDirPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		log.info ("createFolder: folderPath=" + uri);

		runModule (CREATE_FOLDER_MODULE, var);
	}

	private static final String EXISTS_MODULE = "exists.xqy";

	public boolean exists (String path) throws FileSystemException
	{
		if (path.endsWith (MarkLogicBlobStore.MAGIC_EMPTY_BLOB_ID)) {
			return true;
		}

		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		log.info ("exists: checking for uri=" + uri);
		boolean result = runBinaryModule (EXISTS_MODULE, var);
		log.info ("exists: String path=" + uri + ", result=" + result);

		return result;
	}

	private static final String IS_FILE_MODULE = "is-file.xqy";

	public boolean isFile (String path) throws FileSystemException
	{
		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		boolean result = runBinaryModule (IS_FILE_MODULE, var);
		log.info ("isFile: String path=" + uri + ", result=" + result);

		return result;
	}

	private static final String IS_FOLDER_MODULE = "is-folder.xqy";

	public boolean isFolder (String path) throws FileSystemException
	{
		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		boolean result = runBinaryModule (IS_FOLDER_MODULE, var);
		log.info ("isFolder: String path=" + uri + ", result=" + result);

		return result;
	}

	private static final String DOC_LENGTH_MODULE = "doc-length.xqy";

	public long length (String filePath) throws FileSystemException
	{
		if (filePath.endsWith (MarkLogicBlobStore.MAGIC_EMPTY_BLOB_ID)) {
			return 0;
		}

		String uri = fullPath (filePath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		int length = runIntModule (DOC_LENGTH_MODULE, var);

		log.info ("length: filePath=" + uri + ", length=" + length);

		return fetchFile (uri).length;
	}

	private byte [] fetchFile (String uri) throws FileSystemException
	{
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));
		ResultSequence rs = runModule (GET_DOC_MODULE, var);

		if ( ! rs.hasNext()) {
			throw new FileSystemException ("Document does not exist: " + uri);
		}

		InputStream in = rs.next().asInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte [] buffer = new byte [1024];
		int rc;

		try {
			while ((rc = in.read (buffer)) != -1) {
				out.write (buffer, 0, rc);
			}

			in.close();
			out.flush();
		} catch (IOException e) {
			throw new FileSystemException ("Cannot fetch file (" + uri + "): " + e, e);
		}

		return out.toByteArray();
	}

	public long lastModified (String path) throws FileSystemException
	{
		log.info ("lastModified: folderPath=" + path);

		throw new FileSystemException ("NOT IMPL");
	}

	public void touch (String filePath) throws FileSystemException
	{
		log.info ("touch: folderPath=" + filePath);

		throw new FileSystemException ("NOT IMPL");
	}

	private static final String HAS_CHILDREN_MODULE = "has-children.xqy";

	public boolean hasChildren (String path) throws FileSystemException
	{
		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		boolean result = runBinaryModule (HAS_CHILDREN_MODULE, var);

		log.info ("hasChildren: folderPath=" + path + ", result=" + result);

		return result;
	}

	private static final String LIST_CHILDREN_MODULE = "list-children.xqy";

	public String[] list (String folderPath) throws FileSystemException
	{
		log.info ("list: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		ResultSequence rs = runModule (LIST_CHILDREN_MODULE, var);

		return rs.asStrings();
	}

	private static final String LIST_FILES_MODULE = "list-files.xqy";

	public String[] listFiles (String folderPath) throws FileSystemException
	{
		log.info ("listFiles: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		ResultSequence rs = runModule (LIST_FILES_MODULE, var);

		return rs.asStrings();
	}

	private static final String LIST_FOLDERS_MODULE = "list-folders.xqy";

	public String[] listFolders (String folderPath) throws FileSystemException
	{
		log.info ("listFolders: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		ResultSequence rs = runModule (LIST_FOLDERS_MODULE, var);

		return rs.asStrings();
	}

	private static final String DELETE_FILE_MODULE = "delete-file.xqy";

	public void deleteFile (String filePath) throws FileSystemException
	{
		log.info ("deleteFile: folderPath=" + filePath);

		String uri = fullPath (filePath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		runModule (DELETE_FILE_MODULE, var);
	}

	private static final String DELETE_FOLDER_MODULE = "delete-folder.xqy";

	public void deleteFolder (String folderPath) throws FileSystemException
	{
		log.info ("deleteFolder: folderPath=" + folderPath);

		String uri = fullPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		runModule (DELETE_FOLDER_MODULE, var);
	}

	// FIXME: Implement this
	public void move (String srcPath, String destPath) throws FileSystemException
	{
		log.info ("move: srcPath=" + srcPath + ", destPath" + destPath);

		throw new FileSystemException ("NOT IMPL");
	}

	// FIXME: Implement this
	public void copy (String srcPath, String destPath) throws FileSystemException
	{
		log.info ("copy: srcPath=" + srcPath + ", destPath" + destPath);

		throw new FileSystemException ("NOT IMPL");
	}

	// ------------------------------------------------------------

	private static final String UPDATE_STATE_MODULE = "update-state.xqy";

	public void updateState (String uri, String deltas) throws FileSystemException
	{
		XdmVariable var1 = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPath (uri)));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("deltas-str"), ValueFactory.newXSString (deltas));

		runModule (UPDATE_STATE_MODULE, var1, var2);
	}

	private static final String CHECK_NODE_EXISTS_MODULE = "check-node-exists.xqy";

	public boolean itemExists (String uri, NodeId nodeId) throws FileSystemException
	{
		XdmVariable var1 = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPath (uri)));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (nodeId.getUUID().toString()));

		return runBinaryModule (CHECK_NODE_EXISTS_MODULE, var1, var2);
	}

	private static final String CHECK_PROP_EXISTS_MODULE = "check-property-exists.xqy";

	public boolean itemExists (String uri, PropertyId propertyId) throws FileSystemException
	{
		XdmVariable var1 = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPath (uri)));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (propertyId.getParentId().getUUID().toString()));
		XdmVariable var3 = ValueFactory.newVariable (new XName ("name"), ValueFactory.newXSString (FileSystemPathUtil.escapeName (propertyId.getName().toString())));

		return runBinaryModule (CHECK_PROP_EXISTS_MODULE, var1, var2, var3);
	}

	private static final String CHECK_REF_EXISTS_MODULE = "check-reference-exists.xqy";

	public boolean itemExists (String uri, NodeReferencesId referencesId) throws FileSystemException
	{
		XdmVariable var1 = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPath (uri)));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (referencesId.getTargetId().getUUID().toString()));

		return runBinaryModule (CHECK_REF_EXISTS_MODULE, var1, var2);
	}

	private static final String QUERY_NODE_STATE_MODULE = "query-node-state.xqy";

	public InputStream nodeStateAsStream (String uri, NodeId nodeId) throws FileSystemException
	{
		XdmVariable var1 = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPath (uri)));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (nodeId.getUUID().toString()));

		ResultSequence rs = runModule (QUERY_NODE_STATE_MODULE, var1, var2);

		if (rs.size() == 0) {
			return null;
		}

System.out.println ("Node load: " + rs.itemAt (0).asString());

		return rs.next().asInputStream();
	}

	private static final String QUERY_PROP_STATE_MODULE = "query-property-state.xqy";

	public InputStream propertyStateAsStream (String uri, PropertyId propertyId) throws FileSystemException
	{
		XdmVariable var1 = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPath (uri)));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (propertyId.getParentId().getUUID().toString()));
		XdmVariable var3 = ValueFactory.newVariable (new XName ("name"), ValueFactory.newXSString (FileSystemPathUtil.escapeName (propertyId.getName().toString())));

		ResultSequence rs = runModule (QUERY_PROP_STATE_MODULE, var1, var2, var3);

		if (rs.size() == 0) {
			return null;
		}

System.out.println ("Property load: " + rs.itemAt (0).asString());

		return rs.next().asInputStream();
	}

	private static final String QUERY_REFS_STATE_MODULE = "query-references-state.xqy";

	public InputStream referencesStateAsStream (String uri, NodeId nodeId) throws FileSystemException
	{
		XdmVariable var1 = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (fullPath (uri)));
		XdmVariable var2 = ValueFactory.newVariable (new XName ("uuid"), ValueFactory.newXSString (nodeId.getUUID().toString()));

		ResultSequence rs = runModule (QUERY_REFS_STATE_MODULE, var1, var2);

		if (rs.size() == 0) {
			return null;
		}

System.out.println ("Refs load: " + rs.itemAt (0).asString());

		return rs.next().asInputStream();
	}

	// ------------------------------------------------------------

	private String modulePath (String module)
	{
		return (MODULE_ROOT + module);
	}

	private String fullPath (String relPath)
	{
		return (uriRoot + relPath);
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

	// need to flesh this out
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

	// need to flesh this out
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

//	private ResultSequence runRequest (String query, XdmVariable var)
//		throws FileSystemException
//	{
//		Session session = contentSource.newSession();
//		Request request = session.newAdhocQuery (query);
//
//		request.setVariable (var);
//
//		try {
//			return (session.submitRequest (request));
//		} catch (RequestException e) {
//			throw new FileSystemException ("cannot run Mark Logic request: " + e, e);
//		}
//
//	}

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

	private ResultSequence runModule (String module, XdmVariable var1,
		XdmVariable var2)
		throws FileSystemException
	{
		return runModule (module, var1, var2, null);
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

	private boolean runBinaryModule (String module, XdmVariable var1, XdmVariable var2)
		throws FileSystemException
	{
		return runBinaryModule (module, var1, var2, null);
	}

	private boolean runBinaryModule (String module, XdmVariable var)
		throws FileSystemException
	{
		return runBinaryModule (module, var, null, null);
	}

	private int runIntModule (String module, XdmVariable var)
		throws FileSystemException
	{
		ResultSequence rs = runModule (module, var);
		ResultItem item = rs.next();
		XSInteger intVal = (XSInteger) item.getItem();

		return intVal.asPrimitiveInt();
	}

	// ------------------------------------------------------------

}
