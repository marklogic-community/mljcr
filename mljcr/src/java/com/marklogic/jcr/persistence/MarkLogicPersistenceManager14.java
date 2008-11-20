/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.persistence;

import com.marklogic.jcr.compat.PMAdapter14;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Sep 4, 2008
 * Time: 4:40:12 PM
 */
public class MarkLogicPersistenceManager14 extends MarkLogicPersistenceManager
{
	public MarkLogicPersistenceManager14()
	{
		super (new PMAdapter14 ());
	}
}
