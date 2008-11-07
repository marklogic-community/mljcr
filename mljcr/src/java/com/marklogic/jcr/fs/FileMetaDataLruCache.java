/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

import com.marklogic.jcr.cache.ObjectLruCache;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 6, 2008
 * Time: 4:52:40 PM
 */
public class FileMetaDataLruCache
{
	private final ObjectLruCache cache;

	public FileMetaDataLruCache (int cacheSize)
	{
		cache = new ObjectLruCache (cacheSize);
	}

	public synchronized FileMetaData get (String key)
	{
		return (FileMetaData) cache.get (key);
	}

	public synchronized void put (String key, FileMetaData value)
	{
		cache.put (key, value);
	}

	public synchronized void remove (String key)
	{
		cache.remove (key);
	}

	public synchronized void clear()
	{
		cache.clear();
	}
}
