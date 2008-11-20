/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.fs;

import com.marklogic.jcr.compat.PMAdapter13;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Nov 18, 2008
 * Time: 8:33:12 PM
 */
public class MarkLogicFileSystem13 extends MarkLogicFileSystem
{
	public MarkLogicFileSystem13()
	{
		super (new PMAdapter13());
	}
}
