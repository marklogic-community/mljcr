/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import org.xml.sax.ContentHandler;

import javax.jcr.Workspace;
import javax.jcr.Session;
import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;

import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 1, 2008
 * Time: 6:34:16 PM
 */
public class WorkspaceImpl implements Workspace
{
	private final String workspaceName;
	private final NamespaceRegistry registry;

	private Session session;

	public WorkspaceImpl (String workspaceName)
	{
		this.workspaceName = workspaceName;
		this.registry = new NamespaceRegistryImpl();
	}

	// -------------------------------------------------------
	// Implementation of Workspace interface

	public Session getSession()
	{
		return session;
	}

	public String getName()
	{
		return workspaceName;
	}

	public void copy (String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void copy (String srcWorkspace, String srcAbsPath, String destAbsPath) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void clone (String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void move (String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void restore (Version[] versions, boolean removeExisting) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public QueryManager getQueryManager() throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public NamespaceRegistry getNamespaceRegistry() throws RepositoryException
	{
		return registry;
	}

	public NodeTypeManager getNodeTypeManager() throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public String[] getAccessibleWorkspaceNames() throws RepositoryException
	{
		return new String[] { workspaceName };
	}

	public ContentHandler getImportContentHandler (String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public void importXML (String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException
	{
		// FIXME: auto-generated
	}

	// -----------------------------------------------------

	// FIXME
	void setSession (Session session)
	{
		this.session = session;
	}
}
