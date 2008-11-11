/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 10, 2008
 * Time: 2:10:16 PM
 */
public class SerializedItemState
{
	private final String hashKey;
	private byte [] content;

	public SerializedItemState (String hashKey, InputStream in) throws IOException
	{
		this.hashKey = hashKey;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte [] buffer = new byte[2048];
		int rc;

		while ((rc = in.read (buffer)) > 0) {
			baos.write (buffer, 0, rc);
		}

		in.close();

		content = baos.toByteArray();
	}

	public String getHashKey()
	{
		return hashKey;
	}

	public InputStream getInputStream()
	{
		return new ByteArrayInputStream (content);
	}
}
