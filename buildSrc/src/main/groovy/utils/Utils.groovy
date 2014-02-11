package utils

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class Utils {

	public static boolean isEmpty(String value) {
		if (value == null)
			return true;
		else if (value.equals(""))
			return true;
		else
			return false;
	}

	public static String buildVersionName(int major, int minor, boolean isSnapshot) {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String version = ""+major+"."+minor+"."+dateFormat.format(date);
		if (isSnapshot)
			version += "-SNAPSHOT";
		return version;
	}
}