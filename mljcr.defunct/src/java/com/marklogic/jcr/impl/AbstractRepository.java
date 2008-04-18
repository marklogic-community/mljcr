/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.RepositoryException;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 2, 2008
 * Time: 5:45:30 PM
 */
public class AbstractRepository implements javax.jcr.Repository
{
	protected final String scheme;
	protected final String host;
	protected final int port;
	protected final String user;
	protected final String passwd;
	protected final String workspaceName;

	private final Map<String,String> descriptors;

	public AbstractRepository (String user, int port, String scheme,
		String passwd, String workspaceName, String host,
		Map<String,String> descriptors)
	{
		this.user = user;
		this.port = port;
		this.scheme = scheme;
		this.passwd = (passwd == null) ? "" : passwd;
		this.workspaceName = workspaceName;
		this.host = host;
		this.descriptors = descriptors;
	}

	public String[] getDescriptorKeys()
	{
		String [] array = new String [descriptors.size()];

		return descriptors.keySet().toArray (array);
	}

	public String getDescriptor (String key)
	{
		return descriptors.get (key);
	}

	public Session login (Credentials creds, String ws) throws RepositoryException
	{
		String workspaceName = (ws == null) ? this.workspaceName : ws;
		Credentials credentials = (creds == null)
			? new SimpleCredentials (user, passwd.toCharArray()) : creds;
		WorkspaceImpl workspace = new WorkspaceImpl (workspaceName);
		Session session = new SessionImpl (this, credentials, workspace);

		workspace.setSession (session);

		return session;
	}

	public Session login (Credentials credentials) throws RepositoryException
	{
		return login (credentials, null);
	}

	public Session login (String workspaceName) throws RepositoryException
	{
		return login (null, workspaceName);
	}

	public Session login() throws RepositoryException
	{
		return login (null, null);
	}
}
