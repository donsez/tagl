/*
 * cron4j - A pure Java cron-like scheduler
 * 
 * Copyright (C) 2008-2009 Carlo Pelliccia (www.sauronsoftware.it)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package it.sauronsoftware.cron4j;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * <p>
 * A UNIX crontab-like pattern is a string splitted in five space separated
 * parts. Each part is intented as:
 * </p>
 * <ol>
 * <li><strong>Minutes sub-pattern</strong>. During which minutes of the hour
 * should the task been launched? The values range is from 0 to 59.</li>
 * <li><strong>Hours sub-pattern</strong>. During which hours of the day should
 * the task been launched? The values range is from 0 to 23.</li>
 * <li><strong>Days of month sub-pattern</strong>. During which days of the
 * month should the task been launched? The values range is from 0 to 31.</li>
 * <li><strong>Months sub-pattern</strong>. During which months of the year
 * should the task been launched? The values range is from 1 (January) to 12
 * (December), otherwise this sub-pattern allows the aliases &quot;jan&quot;,
 * &quot;feb&quot;, &quot;mar&quot;, &quot;apr&quot;, &quot;may&quot;,
 * &quot;jun&quot;, &quot;jul&quot;, &quot;aug&quot;, &quot;sep&quot;,
 * &quot;oct&quot;, &quot;nov&quot; and &quot;dec&quot;.</li>
 * <li><strong>Days of week sub-pattern</strong>. During which days of the week
 * should the task been launched? The values range is from 0 (Sunday) to 6
 * (Saturday), otherwise this sub-pattern allows the aliases &quot;sun&quot;,
 * &quot;mon&quot;, &quot;tue&quot;, &quot;wed&quot;, &quot;thu&quot;,
 * &quot;fri&quot; and &quot;sat&quot;.</li>
 * </ol>
 * <p>
 * The star wildcard character is also admitted, indicating &quot;every minute
 * of the hour&quot;, &quot;every hour of the day&quot;, &quot;every day of the
 * month&quot;, &quot;every month of the year&quot; and &quot;every day of the
 * week&quot;, according to the sub-pattern in which it is used.
 * </p>
 * <p>
 * Once the scheduler is started, a task will be launched when the five parts in
 * its scheduling pattern will be true at the same time.
 * </p>
 * <p>
 * Some examples:
 * </p>
 * <p>
 * <strong>5 * * * *</strong><br>
 * This pattern causes a task to be launched once every hour, at the begin of
 * the fifth minute (00:05, 01:05, 02:05 etc.).
 * </p>
 * <p>
 * <strong>* * * * *</strong><br>
 * This pattern causes a task to be launched every minute.
 * </p>
 * <p>
 * <strong>* 12 * * Mon</strong><br>
 * This pattern causes a task to be launched every minute during the 12th hour
 * of Monday.
 * </p>
 * <p>
 * <strong>* 12 16 * Mon</strong><br>
 * This pattern causes a task to be launched every minute during the 12th hour
 * of Monday, 16th, but only if the day is the 16th of the month.
 * </p>
 * <p>
 * Every sub-pattern can contain two or more comma separated values.
 * </p>
 * <p>
 * <strong>59 11 * * 1,2,3,4,5</strong><br>
 * This pattern causes a task to be launched at 11:59AM on Monday, Tuesday,
 * Wednesday, Thursday and Friday.
 * </p>
 * <p>
 * Values intervals are admitted and defined using the minus character.
 * </p>
 * <p>
 * <strong>59 11 * * 1-5</strong><br>
 * This pattern is equivalent to the previous one.
 * </p>
 * <p>
 * The slash character can be used to identify periodic values, in the form a/b.
 * A sub-pattern with the slash character is satisfied when the value on the
 * left divided by the one on the right gives an integer result (a % b == 0).
 * </p>
 * <p>
 * <strong>*&#47;15 9-17 * * *</strong><br>
 * This pattern causes a task to be launched every 15 minutes between the 9th
 * and 17th hour of the day (9:00, 9:15, 9:30, 9:45 and so on... note that the
 * last execution will be at 17:45).
 * </p>
 * <p>
 * All the fresh described syntax rules can be used together.
 * </p>
 * <p>
 * <strong>* 12 10-16/2 * *</strong><br>
 * This pattern causes a task to be launched every minute during the 12th hour
 * of the day, but only if the day is the 10th, the 12th, the 14th or the16th of
 * the month.
 * </p>
 * <p>
 * <strong>* 12 1-15,17,20-25 * *</strong><br>
 * This pattern causes a task to be launched every minute during the 12th hour
 * of the day, but the day of the month must be between the 1st and the 15th,
 * the 20th and the 25, or at least it must be the 17th.
 * </p>
 * <p>
 * Finally cron4j lets you combine more scheduling patterns into one, with the
 * pipe character:
 * </p>
 * <p>
 * <strong>0 5 * * *|8 10 * * *|22 17 * * *</strong><br>
 * This pattern causes a task to be launched every day at 05:00, 10:08 and
 * 17:22.
 * </p>
 * 
 * @author Carlo Pelliccia
 * @since 2.0
 */
public class SchedulingPattern {

	/**
	 * Months aliases.
	 */
	private static String[] MONTHS = { "", "jan", "feb", "mar", "apr", "may",
			"jun", "jul", "aug", "sep", "oct", "nov", "dec" };

	/**
	 * Days of week aliases.
	 */
	private static String[] DAYS_OF_WEEK = { "sun", "mon", "tue", "wed", "thu",
			"fri", "sat" };

	/**
	 * Validates a string as a scheduling pattern.
	 * 
	 * @param schedulingPattern
	 *            The pattern to validate.
	 * @return true if the given string represents a valid scheduling pattern;
	 *         false otherwise.
	 */
	public static boolean validate(String schedulingPattern) {
		try {
			new SchedulingPattern(schedulingPattern);
		} catch (InvalidPatternException e) {
			return false;
		}
		return true;
	}

	/**
	 * The pattern as a string.
	 */
	private String asString;

	/**
	 * The ValueMatcher list for the "minute" field.
	 */
	protected ArrayList minuteMatchers = new ArrayList();

	/**
	 * The ValueMatcher list for the "hour" field.
	 */
	protected ArrayList hourMatchers = new ArrayList();

	/**
	 * The ValueMatcher list for the "day of month" field.
	 */
	protected ArrayList dayOfMonthMatchers = new ArrayList();

	/**
	 * The ValueMatcher list for the "month" field.
	 */
	protected ArrayList monthMatchers = new ArrayList();

	/**
	 * The ValueMatcher list for the "day of week" field.
	 */
	protected ArrayList dayOfWeekMatchers = new ArrayList();

	/**
	 * How many matcher groups in this pattern?
	 */
	protected int matcherSize = 0;

	/**
	 * Builds a SchedulingPattern parsing it from a string.
	 * 
	 * @param pattern
	 *            The pattern as a crontab-like string.
	 * @throws InvalidPatternException
	 *             If the supplied string is not a valid pattern.
	 */
	public SchedulingPattern(String pattern) throws InvalidPatternException {
		this.asString = pattern;
		StringTokenizer st1 = new StringTokenizer(pattern, "|");
		if (st1.countTokens() < 1) {
			throw new InvalidPatternException();
		}
		while (st1.hasMoreTokens()) {
			String localPattern = st1.nextToken();
			StringTokenizer st2 = new StringTokenizer(localPattern, " \t");
			if (st2.countTokens() != 5) {
				throw new InvalidPatternException();
			}
			minuteMatchers.add(buildValueMatcher(st2.nextToken(), 0, 59, null));
			hourMatchers.add(buildValueMatcher(st2.nextToken(), 0, 23, null));
			dayOfMonthMatchers.add(buildValueMatcher(st2.nextToken(), 1, 31, null));
			monthMatchers.add(buildValueMatcher(st2.nextToken(), 1, 12, MONTHS));
			dayOfWeekMatchers.add(buildValueMatcher(st2.nextToken(), 0, 6, DAYS_OF_WEEK));
			matcherSize++;
		}
	}

	/**
	 * A ValueMatcher utility builder.
	 * 
	 * @param str
	 *            The pattern part for the ValueMatcher creation.
	 * @param minValue
	 *            The minimum value for the field represented by the
	 *            ValueMatcher.
	 * @param maxValue
	 *            The maximum value for the field represented by the
	 *            ValueMatcher.
	 * @param stringEquivalents
	 *            An array of aliases for the numeric values. It can be null if
	 *            no alias is provided.
	 * @return The requested ValueMatcher.
	 * @throws InvalidPatternException
	 *             If the pattern part supplied is not valid.
	 */
	private ValueMatcher buildValueMatcher(String str, int minValue,
			int maxValue, String[] stringEquivalents)
			throws InvalidPatternException {
		if (str.length() == 1 && str.equals("*")) {
			return new AlwaysTrueValueMatcher();
		}
		StringTokenizer st = new StringTokenizer(str, "/");
		int tokens = st.countTokens();
		if (tokens < 1 || tokens > 2) {
			throw new InvalidPatternException();
		}
		ArrayList list = buildPart1(st.nextToken(), minValue, maxValue,
				stringEquivalents);
		if (tokens == 2) {
			list = buildPart2(list, st.nextToken(), minValue, maxValue);
		}
		return new IntArrayValueMatcher(list);
	}

	private ArrayList buildPart1(String str, int minValue, int maxValue,
			String[] stringEquivalents) throws InvalidPatternException {
		if (str.length() == 1 && str.equals("*")) {
			ArrayList ret = new ArrayList();
			for (int i = minValue; i <= maxValue; i++) {
				ret.add(new Integer(i));
			}
			return ret;
		}
		StringTokenizer st = new StringTokenizer(str, ",");
		if (st.countTokens() < 1) {
			throw new InvalidPatternException();
		}
		ArrayList list = new ArrayList();
		while (st.hasMoreTokens()) {
			ArrayList list2 = buildPart1_1(st.nextToken(), minValue, maxValue,
					stringEquivalents);
			int size = list2.size();
			for (int i = 0; i < size; i++) {
				list.add(list2.get(i));
			}
		}
		return list;
	}

	private ArrayList buildPart1_1(String str, int minValue, int maxValue,
			String[] stringEquivalents) throws InvalidPatternException {
		StringTokenizer st = new StringTokenizer(str, "-");
		int tokens = st.countTokens();
		if (tokens < 1 || tokens > 2) {
			throw new InvalidPatternException();
		}
		String str1 = st.nextToken();
		int value1;
		try {
			value1 = Integer.parseInt(str1);
		} catch (NumberFormatException e) {
			if (stringEquivalents != null) {
				try {
					value1 = getIntValue(str1, stringEquivalents);
				} catch (Exception e1) {
					throw new InvalidPatternException();
				}
			} else {
				throw new InvalidPatternException();
			}
		}
		if (value1 < minValue || value1 > maxValue) {
			throw new InvalidPatternException();
		}
		int value2 = value1;
		if (tokens == 2) {
			String str2 = st.nextToken();
			try {
				value2 = Integer.parseInt(str2);
			} catch (NumberFormatException e) {
				if (stringEquivalents != null) {
					try {
						value2 = getIntValue(str2, stringEquivalents);
					} catch (Exception e1) {
						throw new InvalidPatternException();
					}
				} else {
					throw new InvalidPatternException();
				}
			}
			if (value2 < minValue || value2 > maxValue || value2 <= value1) {
				throw new InvalidPatternException();
			}
		}
		ArrayList list = new ArrayList();
		for (int i = value1; i <= value2; i++) {
			Integer aux = new Integer(i);
			if (!list.contains(aux)) {
				list.add(aux);
			}
		}
		return list;
	}

	private ArrayList buildPart2(ArrayList list, String p2, int minValue,
			int maxValue) throws InvalidPatternException {
		int div = 0;
		try {
			div = Integer.parseInt(p2);
		} catch (NumberFormatException e) {
			;
		}
		if (div <= minValue || div >= maxValue) {
			throw new InvalidPatternException();
		}
		int size = list.size();
		ArrayList list2 = new ArrayList();
		for (int i = 0; i < size; i++) {
			Integer aux = (Integer) list.get(i);
			if (aux.intValue() % div == 0) {
				list2.add(aux);
			}
		}
		return list2;
	}

	/**
	 * This utility method changes an alias to an int value.
	 */
	private int getIntValue(String value, String[] values) throws Exception {
		for (int i = 0; i < values.length; i++) {
			if (values[i].equalsIgnoreCase(value)) {
				return i;
			}
		}
		throw new Exception();
	}

	/**
	 * This methods returns true if the given timestamp (expressed as a UNIX-era
	 * millis value) matches the pattern, according to the given time zone.
	 * 
	 * @param timezone
	 *            A time zone.
	 * @param millis
	 *            The timestamp, as a UNIX-era millis value.
	 * @return true if the given timestamp matches the pattern.
	 */
	public boolean match(TimeZone timezone, long millis) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(millis));
		int minute = gc.get(Calendar.MINUTE);
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int dayOfMonth = gc.get(Calendar.DAY_OF_MONTH);
		int month = gc.get(Calendar.MONTH) + 1;
		int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK) - 1;
		for (int i = 0; i < matcherSize; i++) {
			ValueMatcher minuteMatcher = (ValueMatcher) minuteMatchers.get(i);
			ValueMatcher hourMatcher = (ValueMatcher) hourMatchers.get(i);
			ValueMatcher dayOfMonthMatcher = (ValueMatcher) dayOfMonthMatchers.get(i);
			ValueMatcher monthMatcher = (ValueMatcher) monthMatchers.get(i);
			ValueMatcher dayOfWeekMatcher = (ValueMatcher) dayOfWeekMatchers.get(i);
			boolean eval = minuteMatcher.match(minute)
					&& hourMatcher.match(hour)
					&& dayOfMonthMatcher.match(dayOfMonth)
					&& monthMatcher.match(month)
					&& dayOfWeekMatcher.match(dayOfWeek);
			if (eval) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This methods returns true if the given timestamp (expressed as a UNIX-era
	 * millis value) matches the pattern, according to the system default time
	 * zone.
	 * 
	 * @param millis
	 *            The timestamp, as a UNIX-era millis value.
	 * @return true if the given timestamp matches the pattern.
	 */
	public boolean match(long millis) {
		return match(TimeZone.getDefault(), millis);
	}
	
	/**
	 * Returns the pattern as a string.
	 * 
	 * @return The pattern as a string.
	 */
	public String toString() {
		return asString;
	}

}
