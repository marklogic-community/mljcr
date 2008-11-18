/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr;

import com.marklogic.jcr.persistence.PMAdapter13;
import com.marklogic.jcr.persistence.MarkLogicPersistenceManager;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Sep 4, 2008
 * Time: 5:09:45 PM
 */
public class MarkLogicPersistenceManager13 extends MarkLogicPersistenceManager
{
	public MarkLogicPersistenceManager13()
	{
		super (new PMAdapter13());
	}
}
