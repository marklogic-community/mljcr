/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 6, 2008
 * Time: 4:57:49 PM
 */
public class FileMetaData
{
	private final long size;
	private final long lastModified;
	private final boolean directory;

	public FileMetaData (long size, long lastModified, boolean directory)
	{
		this.size = size;
		this.lastModified = lastModified;
		this.directory = directory;
	}

	public long getSize()
	{
		return size;
	}

	public long getLastModified()
	{
		return lastModified;
	}

	public boolean isDirectory()
	{
		return directory;
	}
}
