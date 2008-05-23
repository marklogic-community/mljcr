/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr;

import org.apache.jackrabbit.core.persistence.util.FileSystemBLOBStore;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemPathUtil;
import org.apache.jackrabbit.core.PropertyId;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 9, 2008
 * Time: 5:31:14 PM
 */
public class MarkLogicBlobStore extends FileSystemBLOBStore
{
	public static final String MAGIC_EMPTY_BLOB_ID = "@=-empty-=@";

	/**
	 * Creates a new <code>FileSystemBLOBStore</code> instance.
	 *
	 * @param fs file system for storing the BLOB data
	 */
	public MarkLogicBlobStore (FileSystem fs)
	{
		super (fs);
	}

	// TODO: override put() method (need to enforce MarkLogicFileSystem)

	//------------------------------------------------------------
	// Override createId in FileSystemBLOBStore

	public String createId (PropertyId id, int index)
	{
		// the blobId is an absolute file system path
		StringBuffer sb = new StringBuffer();
		sb.append (FileSystem.SEPARATOR_CHAR);
		sb.append (id.getParentId().getUUID().toString()).append (FileSystem.SEPARATOR);
		sb.append (FileSystemPathUtil.escapeName(id.getName().toString())).append (".blob");

		return sb.toString();
	}

	//------------------------------------------------------------


	public InputStream get (String blobId) throws Exception
	{
		if ( ! MAGIC_EMPTY_BLOB_ID.equals (blobId)) {
			return super.get (blobId);
		}

		return new ByteArrayInputStream (new byte [0]);
	}
}
