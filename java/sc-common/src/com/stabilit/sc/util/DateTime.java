package com.stabilit.sc.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTime {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");

	public static String getCurrentTimeZoneMillis() {
		long timeInMillis = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeInMillis);
		java.util.Date date = cal.getTime();

		synchronized (sdf) {
			return sdf.format(date);
		}
	}
}
