/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 3:59:13 PM
 */
@SuppressWarnings({"ClassWithTooManyMethods"})
class SessionImpl implements javax.jcr.Session
{
	private final Repository repository;
	private final SimpleCredentials credentials;
	private final Workspace workspace;
	private final NamespaceRegistry registry;
	private final Credentials blindCreds;
	private boolean live = false;

	// package-local constructor for unit testing
	SessionImpl (Repository repository, Credentials credentials, Workspace workspace)
		throws RepositoryException
	{
		live = true;
		this.repository = repository;
		this.workspace = workspace;
		this.registry = workspace.getNamespaceRegistry();

		if (credentials instanceof SimpleCredentials) {
			this.credentials = (SimpleCredentials) credentials;
			blindCreds = null;
		} else {
			this.credentials = new SimpleCredentials (null, new char [0]);
			blindCreds = credentials;
		}
	}

	// -------------------------------------------------------------

	public javax.jcr.Repository getRepository()
	{
		return repository;
	}

	public String getUserID()
	{
		return credentials.getUserID();
	}

	public Object getAttribute (String name)
	{
		return credentials.getAttribute (name);
	}

	public String[] getAttributeNames()
	{
		return credentials.getAttributeNames();
	}

	public Workspace getWorkspace()
	{
		return workspace;
	}

	public Session impersonate (Credentials credentials) throws RepositoryException
	{
		return getRepository().login (credentials);
	}

	public Node getRootNode() throws RepositoryException
	{
		return new NodeImpl (this, null, "", "", 0);		// FIXME
	}

	public Node getNodeByUUID (String uuid) throws ItemNotFoundException
	{
		throw new ItemNotFoundException ("Node not found by UUID: " + uuid);
	}

	public Item getItem (String absPath) throws PathNotFoundException
	{
		throw new PathNotFoundException ("Node not found by path: " + absPath);
	}

	public boolean itemExists (String absPath)
	{
		return false;  // FIXME: auto-generated
	}

	public void move (String srcAbsPath, String destAbsPath)
	{
		// FIXME: auto-generated
	}

	public void save()
	{
		// FIXME: auto-generated
	}

	public void refresh (boolean keepChanges) throws RepositoryException
	{
		// FIXME: auto-generated
	}

	public boolean hasPendingChanges() throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public ValueFactory getValueFactory()
	{
		return new com.marklogic.jcr.values.ValueFactory();
	}

	public void checkPermission (String absPath, String actions) throws AccessControlException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public ContentHandler getImportContentHandler (String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public void importXML (String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void exportSystemView (String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void exportSystemView (String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void exportDocumentView (String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void exportDocumentView (String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void setNamespacePrefix (String prefix, String uri) throws RepositoryException
	{
		registry.registerNamespace (prefix, uri);
	}

	public String[] getNamespacePrefixes() throws RepositoryException
	{
		return registry.getPrefixes();
	}

	public String getNamespaceURI (String prefix) throws RepositoryException
	{
		return registry.getURI (prefix);
	}

	public String getNamespacePrefix (String uri) throws RepositoryException
	{
		return registry.getPrefix (uri);
	}

	public void logout()
	{
		live = false;
	}

	public boolean isLive()
	{
		return live;
	}

	public void addLockToken (String lt)
	{
		throwUnsupportedRuntime ("Locking is not supported in this version");
	}

	public String[] getLockTokens()
	{
		throwUnsupportedRuntime ("Locking is not supported in this version");

		return null;	// never reached
	}

	public void removeLockToken (String lt)
	{
		throwUnsupportedRuntime ("Locking is not supported in this version");
	}

	// -----------------------------------------------------------

	private void throwUnsupportedRuntime (String msg)
	{
		throw new RuntimeException (msg, new UnsupportedRepositoryOperationException (msg));
	}
}
