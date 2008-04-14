/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 2, 2008
 * Time: 8:01:11 PM
 */
public class NodeImpl extends ItemImpl implements Node
{
	public NodeImpl (Session session, Node parent, String name, String path, int depth)
	{
		super (session, parent, name, path, depth);
	}

	// ------------------------------------------------------------
	// Implementation of Node interface


	public boolean isNode()
	{
		return true;
	}

	public Node addNode (String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException
	{
		return new NodeImpl (getSession(), this, relPath, getPath() + "/" + relPath, getDepth() + 1);
	}

	public Node addNode (String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public void orderBefore (String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public Property setProperty (String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property setProperty (String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Node getNode (String relPath) throws PathNotFoundException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public NodeIterator getNodes () throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public NodeIterator getNodes (String namePattern) throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Property getProperty (String relPath) throws PathNotFoundException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public PropertyIterator getProperties () throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public PropertyIterator getProperties (String namePattern) throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Item getPrimaryItem () throws ItemNotFoundException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public String getUUID () throws UnsupportedRepositoryOperationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public int getIndex () throws RepositoryException
	{
		return 0;  // FIXME: auto-generated
	}

	public PropertyIterator getReferences () throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public boolean hasNode (String relPath) throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public boolean hasProperty (String relPath) throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public boolean hasNodes () throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public boolean hasProperties () throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public NodeType getPrimaryNodeType () throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public NodeType[] getMixinNodeTypes () throws RepositoryException
	{
		return new NodeType[0];  // FIXME: auto-generated
	}

	public boolean isNodeType (String nodeTypeName) throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public void addMixin (String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void removeMixin (String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public boolean canAddMixin (String mixinName) throws NoSuchNodeTypeException, RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public NodeDefinition getDefinition () throws RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Version checkin () throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public void checkout () throws UnsupportedRepositoryOperationException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void doneMerge (Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void cancelMerge (Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void update (String srcWorkspaceName) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public NodeIterator merge (String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public String getCorrespondingNodePath (String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public boolean isCheckedOut () throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public void restore (String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void restore (Version version, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void restore (Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public void restoreByLabel (String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public VersionHistory getVersionHistory () throws UnsupportedRepositoryOperationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Version getBaseVersion () throws UnsupportedRepositoryOperationException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Lock lock (boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public Lock getLock () throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException
	{
		return null;  // FIXME: auto-generated
	}

	public void unlock () throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException
	{
		// FIXME: auto-generated
	}

	public boolean holdsLock () throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}

	public boolean isLocked () throws RepositoryException
	{
		return false;  // FIXME: auto-generated
	}
}
