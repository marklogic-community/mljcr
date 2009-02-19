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

/**
 * File metadata, cachable, to avoid trips to the repository.
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
