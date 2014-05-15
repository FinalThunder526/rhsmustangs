/**
 * ScheduleData.java
 * May 14, 2014
 * Sarang Joshi
 */

package com.sarangjoshi.rhsmustangs.schedule;

import java.util.Calendar;

public class ScheduleData {
	public static String a = "P1 07 30 08 24\nP2 08 30 09 24\nP3 09 30 10 24\nLA 10 30 11 00\nP4 11 06 12 00\nP5 12 06 01 00\nP6 01 06 02 00\n";
	public static String b = "P1 07 30 08 24\nP2 08 30 09 24\nP3 09 30 10 24\n4A 10 30 11 00\nLB 11 00 11 30\n4B 11 36 12 00\nP5 12 06 01 00\nP6 01 06 02 00\n";
	public static String c = "P1 07 30 08 24\nP2 08 30 09 24\nP3 09 30 10 24\nP4 10 30 11 24\nLC 11 30 12 00\nP5 12 06 01 00\nP6 01 06 02 00\n";

	public static String wed = "P1 07 30 08 10\nP2 08 16 09 56\nP3 09 02 09 42\nP4 09 48 10 28\nP5 10 34 11 14\nP6 11 20 12 00\nLN 12 00 12 30\n";

	public static String ta = "P1 07 30 08 20\nP2 08 26 09 16\nHR 09 22 09 46\nP3 09 46 10 36\nLA 10 42 11 12\nP4 11 18 12 08\nP5 12 14 01 04\nP6 01 10 02 00\n";
	public static String tb = "P1 07 30 08 20\nP2 08 26 09 16\nHR 09 22 09 46\nP3 09 46 10 36\n4A 10 42 11 07\nLB 11 07 11 37\n4B 11 43 12 08\nP5 12 14 01 04\nP6 01 10 02 00\n";
	public static String tc = "P1 07 30 08 20\n2 08 26 09 16\nHR 09 22 09 46\nP3 09 46 10 36\nP4 10 42 11 32\nLC 11 38 12 08\nP5 12 14 01 04\nP6 01 10 02 00\n";

	/**
	 * Given the current day and lunch type, returns the appropriate schedule string.
	 * 
	 * @param day the current weekday
	 * @param lType the lunch type
	 * @return the schedule string
	 */
	public static String getScheduleByDay(int day, char lType) {
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			day = Calendar.MONDAY;

		if (day == Calendar.WEDNESDAY)
			return wed;
		else if (day == Calendar.THURSDAY)
			switch (lType) {
			case ('b'):
				return tb;
			case ('c'):
				return tc;
			default:
				return ta;
			}
		else
			switch (lType) {
			case ('b'):
				return b;
			case ('c'):
				return c;
			default:
				return a;
			}
	}
}
