/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr;

import com.marklogic.jcr.mock.MockRepository;
import com.marklogic.jcr.impl.MLRepository;

import javax.jcr.Repository;

import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 2, 2008
 * Time: 5:21:37 PM
 */
public class RepositoryFactory
{
	private RepositoryFactory ()
	{
		// prevent instantiation
	}

	public static Repository openRepository (URI uri)
	{
		String scheme = uri.getScheme();
		String host = uri.getHost();
		int port = uri.getPort();
		String userInfo = uri.getUserInfo();
		String [] userFields = (userInfo == null) ? new String [0] : uri.getUserInfo().split (":");
		String user = (userFields.length > 0) ? userFields [0] : null;
		String passwd = (userFields.length > 1) ? userFields [1] : null;
		String workspaceName = uri.getPath();

		if (workspaceName.startsWith ("/")) {
			workspaceName = workspaceName.substring (1);
		}

		if (scheme.equals ("mljcr")) {
			return new MLRepository (scheme, host, port, user, passwd, workspaceName);
		}

		if (scheme.equals ("mockjcr")) {
			return new MockRepository (scheme, host, port, user, passwd, workspaceName);
		}

		throw new IllegalArgumentException ("Unrecognized scheme: " + scheme);
	}
}
