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

package com.marklogic.jcr.query;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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

	private final String functionName;
	private final List positiveTerms = new ArrayList(10);
	private final List negativeTerms = new ArrayList(10);

	public TextQueryParser (String rawQuery, String functionName)
	{
		this.functionName = functionName;

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

	public String getFunctionName()
	{
		return functionName;
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


	// This is a simplistic implementation that does not
	// properly handle precendence of "and" vs "or" nor
	// grouping of terms (parens are not part of the the
	// BNF in Spec 6.6.5.2).
	// Nor is composability with negation tests handled
	// properly because this is hard to do cts:contains.
	// The positive and negative tests are gathered separately
	// and put out as two different predicates.
	private String aggregateQuery (List terms)
	{
		if (terms.size() == 1) {
			return "'" + terms.get (0) + "'";
		}

		LinkedList stack = new LinkedList();

		for (Iterator it = terms.iterator(); it.hasNext();) {
			String term = (String) it.next();

			if (term.equalsIgnoreCase ("or")) {
				if ((stack.size() == 0) || ( ! it.hasNext())) {
					throw new IllegalStateException ("Malformed full text expression");
				}

				String left = (String) stack.removeLast();
				String right = (String) it.next();

				stack.add ("cts:or-query ((" + left + ", '" + right + "'))");
			} else {
				stack.add ("'" + term + "'");
			}
		}

		if (stack.size() == 1) {
			return (String) stack.get (0);
		}

		StringBuffer sb = new StringBuffer();
		boolean notFirst = false;

		sb.append ("cts:and-query ((");

		for (Iterator it = stack.iterator(); it.hasNext();) {
			if (notFirst) {
				sb.append (", ");
			} else {
				notFirst = true;
			}

			sb.append (it.next());
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
}
