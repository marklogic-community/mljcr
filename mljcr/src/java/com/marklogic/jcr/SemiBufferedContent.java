/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * <p>
 * This is a specialized {@link com.marklogic.xcc.Content} implementation
 * that takes an {@link java.io.InputStream} and will either buffer the
 * content of the stream if it's small enough, or copy the content to
 * a temporary disk file otherwise.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/SemiBufferedContent.java.txt">
 * source code for this class</a>
 * </p>
 * <p>
 * Use this class rather than
 * {@link ContentFactory#newContent(String, java.io.InputStream, com.marklogic.xcc.ContentCreateOptions)}
 * when the size of the input stream may be too large to buffer in memory
 * and you need to assure that retries are supported when calling
 * {@link com.marklogic.xcc.Session#insertContent(com.marklogic.xcc.Content)}.
 * <p>
 * </p>
 * A buffer will be allocated of the size you specify.  The
 * {@link java.io.InputStream} will be read into that buffer until either the
 * stream is fully read or the buffer fills up.  If all the content fits into
 * the buffer, then this class will be equivalent to
 * {@link com.marklogic.xcc.ContentFactory#newContent(String, byte[], com.marklogic.xcc.ContentCreateOptions)}.
 * If the internal buffer fills up, then a temp file is created and all the
 * content is written to it.  This class will then be equivalent to
 * {@link com.marklogic.xcc.ContentFactory#newContent(String, java.io.File, com.marklogic.xcc.ContentCreateOptions)}.
 * </p>
 * <p>
 * If you provide a {@link java.io.File} object as the fifth argument to the
 * constructor, then it is passed to
 * {@link File#createTempFile(String, String, java.io.File)} to specify the
 * directory in which to create the temporary file.  If null, then the
 * system-default temp directory is used.
 * </p>
 */
public class SemiBufferedContent implements Content
{
	private final Content delegate;
	private final File tempFile;

	/**
	 * Instantiate a {@link com.marklogic.xcc.Content} object that
	 * will either buffer the {@link java.io.InputStream} or copy
	 * it to disk, depending on whether the stream size exceeds the provided
	 * buffer size.
	 * @param uri The URI to use when the content is inserted.
	 * @param options The {@link com.marklogic.xcc.ContentCreateOptions}
	 *  object to use when the content is inserted.
	 * @param input An {@link java.io.InputStream} object, which will
	 *  be consumed when this object is created.
	 * @param bufferSize The maximum number of bytes to buffer in memory
	 *  before spilling the content to a temporary disk file.
	 * @param directory A {@link java.io.File} object that specifies a
	 *  directory where the temporary file, if needed, will be created.
	 *  This parameter may be null.
	 * @throws IOException If there is a problem reading the
	 *  {@link java.io.InputStream} or creating the temporary file.
	 */
	public SemiBufferedContent (String uri, ContentCreateOptions options,
		InputStream input, int bufferSize, File directory)
		throws IOException
	{
		byte [] buffer = new byte [bufferSize];
		int bytesRead = loadBuffer (input, buffer);

		if (bytesRead == buffer.length) {
			tempFile = spillContentToFile (input, buffer, directory);
			delegate = ContentFactory.newContent (uri, tempFile, options);
		} else {
			tempFile = null;
			delegate = ContentFactory.newContent (uri, buffer, 0, bytesRead, options);
		}
	}


	/**
	 * Instantiate a {@link com.marklogic.xcc.Content} object that
	 * will either buffer the {@link java.io.InputStream} or copy
	 * it to disk, depending on whether the size exceeds the provided
	 * buffer size.  This constructor defaults to using the system-specific
	 * temporary file directory.
	 * @param uri The URI to use when the content is inserted.
	 * @param options The {@link com.marklogic.xcc.ContentCreateOptions}
	 *  object to use when the content is inserted.
	 * @param input An {@link java.io.InputStream} object, which will
	 *  be consumed when this object is created.
	 * @param bufferSize The maximum number of bytes to buffer in memory
	 *  before spilling the content to a temporary disk file.
	 * @throws IOException If there is a problem reading the
	 *  {@link java.io.InputStream} or creating the temporary file.
	 */
	public SemiBufferedContent (String uri, ContentCreateOptions options,
		InputStream input, int bufferSize)
		throws IOException
	{
		this (uri, options, input, bufferSize, null);
	}

	// ----------------------------------------------------------------

	private File spillContentToFile (InputStream input, byte[] buffer, File directory)
		throws IOException
	{
		File file = File.createTempFile ("XccContent", null, directory);
		OutputStream output = new FileOutputStream (file);

		output.write (buffer, 0, buffer.length);

		int rc;

		while ((rc = input.read (buffer)) > 0) {
			output.write (buffer, 0, rc);
		}

		input.close();
		output.flush();
		output.close();

		return file;
	}

	private int loadBuffer (InputStream input, byte[] buffer) throws IOException
	{
		int bytesRead = 0;
		int available = buffer.length;
		int rc;

		while ((rc = input.read (buffer, bytesRead, available)) > 0) {
			bytesRead += rc;
			available -= rc;
		}

		return bytesRead;
	}

	// ----------------------------------------------------------------
	// Delegate to the contained Content Instance, and delete temp file

	public String getUri()
	{
		return delegate.getUri();
	}

	public InputStream openDataStream()
		throws IOException
	{
		return delegate.openDataStream();
	}

	public ContentCreateOptions getCreateOptions()
	{
		return delegate.getCreateOptions();
	}

	public boolean isRewindable()
	{
		return delegate.isRewindable();
	}

	public void rewind()
		throws IOException
	{
		delegate.rewind();
	}

	public long size()
	{
		return delegate.size();
	}

	public void close()
	{
		delegate.close();

		if (tempFile != null) {
			tempFile.delete();
		}
	}
}
