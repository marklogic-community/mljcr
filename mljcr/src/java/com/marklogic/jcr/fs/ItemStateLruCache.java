/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

import com.marklogic.jcr.cache.ObjectLruCache;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 10, 2008
 * Time: 2:24:38 PM
 */
public class ItemStateLruCache
{
	private final ObjectLruCache cache;

	public ItemStateLruCache (int cacheSize)
	{
		cache = new ObjectLruCache (cacheSize);
	}

	public synchronized SerializedItemState get (String key)
	{
		return (SerializedItemState) cache.get (key);
	}

	public synchronized void put (String key, SerializedItemState value)
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
