/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Mar 24, 2008
 * Time: 1:42:54 PM
 */
public class MLRepository extends AbstractRepository
{
	private static final Map<String,String> descriptors;

	static {
		Map<String,String> map = new HashMap<String, String>();
		map.put (REP_VERSION_DESC, "4.0-1");	// FIXME: Set this from generated VERSION
		map.put (SPEC_VERSION_DESC, "1.0");
		map.put (SPEC_NAME_DESC, "Content Repository for Java Technology API");
		map.put (REP_VENDOR_DESC, "Mark Logic Corporation");
		map.put (REP_VENDOR_URL_DESC, "http://marklogic.com/");
		map.put (REP_NAME_DESC, "Mark Logic Java Content MLRepository");
		map.put (LEVEL_1_SUPPORTED, "true");
		map.put (LEVEL_2_SUPPORTED, "true");
		map.put (OPTION_TRANSACTIONS_SUPPORTED, "false");
		map.put (OPTION_VERSIONING_SUPPORTED, "false");
		map.put (OPTION_OBSERVATION_SUPPORTED, "false");
		map.put (OPTION_LOCKING_SUPPORTED, "false");
		map.put (OPTION_QUERY_SQL_SUPPORTED, "false");
		map.put (QUERY_XPATH_POS_INDEX, "true");
		map.put (QUERY_XPATH_DOC_ORDER, "true");

		descriptors = Collections.unmodifiableMap (map);
	}

	// --------------------------------------------------------------

	public MLRepository (String scheme, String host, int port, String user,
		String passwd, String workspaceName)
	{
		super (user, port, scheme, passwd, workspaceName, host, descriptors);
	}

	MLRepository()
	{
		this (null, null, -1, null, null, null);
	}

	// --------------------------------------------------------------
}
