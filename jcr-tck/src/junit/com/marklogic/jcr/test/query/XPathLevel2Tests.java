/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.test.query;

import org.apache.jackrabbit.test.api.query.AbstractQueryLevel2Test;

import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.jcr.query.Row;
import javax.jcr.query.Query;
import javax.jcr.Value;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Feb 11, 2009
 * Time: 4:37:41 PM
 */
public class XPathLevel2Tests extends AbstractQueryLevel2Test
{
	// test a full text "or" expression
	public void testFullTextOrSearch() throws Exception
	{
		setUpFullTextTest();

		String query = "/" + jcrRoot + testRoot + "/*[" + jcrContains +
			"(., \"'quick brown' or cat\")]/@" + propertyName1;

		QueryResult result = execute (query, Query.XPATH);

		checkResult (result, 2);

		RowIterator itr = result.getRows ();
		while (itr.hasNext ()) {
			Row row = itr.nextRow ();
			Value value = row.getValue (propertyName1);
			if (value != null) {
				String fullText = value.getString ();
				if ((fullText.indexOf ("quick brown") == 0) && (fullText.indexOf ("cat") == 0)) {
					fail ("Search Text: full text search not correct, returned prohibited text");
				}
			}
		}
	}
}
