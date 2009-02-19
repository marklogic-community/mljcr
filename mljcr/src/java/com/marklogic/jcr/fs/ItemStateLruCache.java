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
