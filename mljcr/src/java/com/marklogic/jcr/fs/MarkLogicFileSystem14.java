/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

import com.marklogic.jcr.persistence.PMAdapter14;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 18, 2008
 * Time: 8:24:28 PM
 */
public class MarkLogicFileSystem14 extends MarkLogicFileSystem
{
	public MarkLogicFileSystem14()
	{
		super (new PMAdapter14());
	}
}
