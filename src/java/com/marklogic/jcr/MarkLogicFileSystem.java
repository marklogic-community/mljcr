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
import com.marklogic.xcc.types.XdmVariable;

import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.fs.FileSystemPathUtil;
import org.apache.jackrabbit.core.fs.RandomAccessOutputStream;
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
		this.uriRoot = uriRoot;
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

	private static final String GET_DOC_QUERY =
		"xquery version '1.0-ml';" +
		"declare variable $uri external;" +
		"fn:doc ($uri)";

	public InputStream getInputStream (String filePath) throws FileSystemException
	{
		String uri = fullPath (filePath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		log.info ("getInputStream: filePath=" + uri);

		ResultSequence rs = runRequest (GET_DOC_QUERY, var);

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

	public RandomAccessOutputStream getRandomAccessOutputStream (String filePath)
		throws FileSystemException, UnsupportedOperationException
	{
		log.info ("getRandomAccessOutputStream: filePath=" + filePath);

		throw new FileSystemException ("NOT IMPL");
	}

	private static final String CREATE_FOLDER_QUERY =
		"xquery version '1.0-ml';" +
		"declare variable $uri external;" +
		"xdmp:directory-create ($uri)";

	public void createFolder (String folderPath) throws FileSystemException
	{
		String uri = fullDirPath (folderPath);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		log.info ("createFolder: folderPath=" + uri);

		runRequest (CREATE_FOLDER_QUERY, var);
	}

	private static final String EXISTS_QUERY =
		"xquery version '1.0-ml';" +
		"declare variable $uri external;" +
		"declare variable $dir-uri := fn:concat ($uri, '/');" +
		"fn:exists (doc ($uri)) or fn:exists (xdmp:document-properties ($dir-uri)//prop:directory)";

	public boolean exists (String path) throws FileSystemException
	{
		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		boolean result = runBinaryrequest (EXISTS_QUERY, var);
		log.info ("exists: String path=" + uri + ", result=" + result);

		return result;
	}

	private static final String IS_FILE_QUERY =
		"xquery version '1.0-ml';" +
		"declare variable $uri external;" +
		"declare variable $dir-uri := fn:concat ($uri, '/');" +
		"fn:exists (doc ($uri))";

	public boolean isFile (String path) throws FileSystemException
	{
		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		boolean result = runBinaryrequest (IS_FILE_QUERY, var);
		log.info ("isFile: String path=" + uri + ", result=" + result);

		return result;
	}

	private static final String IS_FOLDER_QUERY =
		"xquery version '1.0-ml';" +
		"declare variable $uri external;" +
		"declare variable $dir-uri := fn:concat ($uri, '/');" +
		"fn:exists (xdmp:document-properties ($uri)//prop:directory) or " +
		"fn:exists (xdmp:document-properties ($dir-uri)//prop:directory)";

	public boolean isFolder (String path) throws FileSystemException
	{
		String uri = fullPath (path);
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));

		boolean result = runBinaryrequest (IS_FOLDER_QUERY, var);
		log.info ("isFolder: String path=" + uri + ", result=" + result);

		return result;
	}

	private static final String FETCH_DOC_QUERY =
		"xquery version '1.0-ml';" +
		"declare variable $uri external;" +
		"fn:doc ($uri)";

	public long length (String filePath) throws FileSystemException
	{
		String uri = fullPath (filePath);
		log.info ("length: filePath=" + uri);

		// FIXME: This needs to be done in XQuery
		return fetchFile (uri).length;
	}

	public byte [] fetchFile (String uri) throws FileSystemException
	{
		XdmVariable var = ValueFactory.newVariable (new XName ("uri"), ValueFactory.newXSString (uri));
		ResultSequence rs = runRequest (FETCH_DOC_QUERY, var);

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

	public boolean hasChildren (String path) throws FileSystemException
	{
		log.info ("hasChildren: folderPath=" + path);

		throw new FileSystemException ("NOT IMPL");
	}

	public String[] list (String folderPath) throws FileSystemException
	{
		log.info ("list: folderPath=" + folderPath);

		throw new FileSystemException ("NOT IMPL");
	}

	public String[] listFiles (String folderPath) throws FileSystemException
	{
		log.info ("listFiles: folderPath=" + folderPath);

		throw new FileSystemException ("NOT IMPL");
	}

	public String[] listFolders (String folderPath) throws FileSystemException
	{
		log.info ("listFolders: folderPath=" + folderPath);

		throw new FileSystemException ("NOT IMPL");
	}

	public void deleteFile (String filePath) throws FileSystemException
	{
		log.info ("deleteFile: folderPath=" + filePath);

		throw new FileSystemException ("NOT IMPL");
	}

	public void deleteFolder (String folderPath) throws FileSystemException
	{
		log.info ("deleteFolder: folderPath=" + folderPath);

		throw new FileSystemException ("NOT IMPL");
	}

	public void move (String srcPath, String destPath) throws FileSystemException
	{
		log.info ("move: srcPath=" + srcPath + ", destPath" + destPath);

		throw new FileSystemException ("NOT IMPL");
	}

	public void copy (String srcPath, String destPath) throws FileSystemException
	{
		log.info ("copy: srcPath=" + srcPath + ", destPath" + destPath);

		throw new FileSystemException ("NOT IMPL");
	}

	// ------------------------------------------------------------

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
			uri.endsWith (".properties"))
		{
			return true;
		}

		return false;
	}

	private ResultSequence runRequest (String query, XdmVariable var)
		throws FileSystemException
	{
		Session session = contentSource.newSession();
		Request request = session.newAdhocQuery (query);

		request.setVariable (var);

		try {
			return (session.submitRequest (request));
		} catch (RequestException e) {
			throw new FileSystemException ("cannot run Mark Logic request: " + e, e);
		}

	}

	private boolean runBinaryrequest (String query, XdmVariable var)
		throws FileSystemException
	{
		ResultSequence rs = runRequest (query, var);
		ResultItem item = rs.next();
		XSBoolean bool = (XSBoolean) item.getItem();

		return bool.asPrimitiveBoolean();
	}

	// ------------------------------------------------------------

}
