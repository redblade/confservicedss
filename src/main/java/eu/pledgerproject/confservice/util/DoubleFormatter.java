package eu.pledgerproject.confservice.util;

import java.text.DecimalFormat;

public class DoubleFormatter {
	
	public static Double format(Double number) {
		DecimalFormat decimalFormat = new DecimalFormat("#");
		return Double.parseDouble(decimalFormat.format(number));
	}
	public static String formatAsString(Double number) {
		DecimalFormat decimalFormat = new DecimalFormat("#");
		return decimalFormat.format(number);
	}
}
