/*
 *  Copyright (c) 2009,  Mark Logic Corporation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  The use of the Apache License does not indicate that this project is
 *  affiliated with the Apache Software Foundation.
 */

package com.marklogic.jcr.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 6, 2008
 * Time: 7:34:32 PM
 */
public class ObjectLruCache
{
	private static final float hashTableLoadFactor = 0.75f;

	private final LinkedHashMap map;
	private final int cacheSize;

	/**
	 * Creates a new LRU cache.
	 *
	 * @param cacheSize the maximum number of entries that will be kept in this cache.
	 */
	public ObjectLruCache (int cacheSize)
	{
		int hashTableCapacity = (int) Math.ceil (cacheSize / hashTableLoadFactor) + 1;
		this.cacheSize = cacheSize;
		this.map = new LinkedHashMap (hashTableCapacity, hashTableLoadFactor, true)
		{
			// (an anonymous inner class)
			private static final long serialVersionUID = 1;

			protected boolean removeEldestEntry (Map.Entry eldest)
			{
				return size() > ObjectLruCache.this.cacheSize;
			}
		};
	}

	/**
	 * Retrieves an entry from the cache.<br>
	 * The retrieved entry becomes the MRU (most recently used) entry.
	 *
	 * @param key the key whose associated value is to be returned.
	 * @return the value associated to this key, or null if no value with this key exists in the cache.
	 */
	public synchronized Object get (Object key)
	{
		return map.get (key);
	}

	/**
	 * Adds an entry to this cache.
	 * If the cache is full, the LRU (least recently used) entry is dropped.
	 *
	 * @param key   the key with which the specified value is to be associated.
	 * @param value a value to be associated with the specified key.
	 */
	public synchronized void put (Object key, Object value)
	{
		map.put (key, value);
	}

	public synchronized void remove (Object key)
	{
		map.remove (key);
	}

	/**
	 * Clears the cache.
	 */
	public synchronized void clear()
	{
		map.clear();
	}

	/**
	 * Returns the number of used entries in the cache.
	 *
	 * @return the number of entries currently in the cache.
	 */
	public synchronized int usedEntries()
	{
		return map.size();
	}

	/**
	 * Returns a <code>Collection</code> that contains a copy of all cache entries.
	 *
	 * @return a <code>Collection</code> with a copy of the cache content.
	 */
	public synchronized Collection getAll()
	{
		return new ArrayList (map.entrySet());
	}
}
