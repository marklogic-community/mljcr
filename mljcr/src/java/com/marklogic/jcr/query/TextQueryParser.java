/*
 * Copyright (c) 2009,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.query;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Jan 21, 2009
 * Time: 7:06:31 PM
 */
public class TextQueryParser
{
	private static final String QUOTSINGLE = "(-?'[^']+')";
	private static final String QUOTDOUBLE = "(-?\"[^\"]+\")";
	private static final String SIMPLEWORD = "(-?\\S+)";
	private static final String TERM_REGEX =
		"(" + QUOTSINGLE + "|" + QUOTDOUBLE + "|" + SIMPLEWORD + ")";
	private static final Pattern termPattern = Pattern.compile (TERM_REGEX);

	private final List positiveTerms = new ArrayList(10);
	private final List negativeTerms = new ArrayList(10);

	public TextQueryParser (String rawQuery)
	{
		Matcher matcher = termPattern.matcher (rawQuery);

		while (matcher.find()) {
			String term = matcher.group();
			boolean notQuery = term.charAt (0) == '-';

			term = scrubTerm (term);

			if (notQuery) {
				negativeTerms.add (term);
			} else {
				positiveTerms.add (term);
			}
		}
	}

	public String getPositiveTest()
	{
		if (positiveTerms.size() == 0) {
			return null;
		}

		return aggregateQuery (positiveTerms);
	}

	public String getNegativeTest()
	{
		if (negativeTerms.size() == 0) {
			return null;
		}

		return aggregateQuery (negativeTerms);
	}


	// TODO: Need to handle "or" queries
	// TODO: Need to handle precedence rules (parens?)
	private String aggregateQuery (List queries)
	{
		boolean notFirst = false;
		StringBuffer sb = new StringBuffer();

		sb.append ("cts:and-query ((");

		for (Iterator it = queries.iterator(); it.hasNext();) {
			if (notFirst) {
				sb.append (", ");
			} else {
				notFirst = true;
			}

			sb.append ("'");
			sb.append (it.next());
			sb.append ("'");
		}

		sb.append ("))");

		return sb.toString();
	}

	private String scrubTerm (String rawTerm)
	{
		String temp = (rawTerm.charAt (0) == '-') ? rawTerm.substring (1) : rawTerm;

		temp = temp.replaceAll ("\\\\'", "''");
		temp = temp.replaceAll ("\\\\\"", "\"\"");

		if ((temp.charAt (0) == '\'') && (temp.charAt (temp.length() - 1) == '\'')) {
			temp = temp.substring (1, temp.length () - 1);
		}

		if ((temp.charAt (0) == '"') && (temp.charAt (temp.length() - 1) == '"')) {
			temp = temp.substring (1, temp.length () - 1);
		}

		return temp;
	}

	// -------------------------------------------------------

//	public static String parseContainsQuery (String rawQuery)
//	{
////		String [] terms = rawQuery.split (TERM_REGEX);
//		List terms = findTerms (rawQuery);
//System.out.println ("parseContainsQuery: rawQuery=" + rawQuery + ", terms=" + terms.size() + ", regex=" + TERM_REGEX);
//		List queries = new ArrayList (10);
//
//		for (Iterator it = terms.iterator(); it.hasNext();) {
//			String term = (String) it.next ();
//
////			if (term.equalsIgnoreCase ("or")) {
////				// FIXME: what to do here?
////			}
//
//			String ctsQuery = queryFor (term);
//System.out.println ("  term=" + term + ", query=" + ctsQuery);
//
//			queries.add (ctsQuery);
//		}
//
//		if (queries.size () == 1) {
//			return (String) queries.get (0);
//		}
//
//		return aggregateQuery (queries, sb);
//	}

//	private List findTerms (String rawQuery)
//	{
//		List terms = new ArrayList (10);
//		Matcher matcher = termPattern.matcher (rawQuery);
//
//		while (matcher.find()) {
//			terms.add (matcher.group());
//		}
//
//		return terms;
//	}

//	private static String queryFor (String rawTerm)
//	{
//		boolean notQuery = rawTerm.charAt (0) == '-';
//		String term = scrubTerm (rawTerm);
//		String query = ("cts:word-query('" + term + "')");
//
//		if (notQuery) {
//			query = "cts:not-query(" + query + ")";
//		}
//
//		return query;
//	}
}
