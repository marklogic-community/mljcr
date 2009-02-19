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

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Holds a serialized representation of a node or property
 * for caching.  This is used to avoid round-trips to the MarkLogic.
 */
public class SerializedItemState
{
	private final String hashKey;
	private final byte [] content;

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
