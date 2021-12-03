package eu.pledgerproject.confservice.util;

import java.text.DecimalFormat;

public class DoubleFormatter {
	public static Double format(Double number) {
		DecimalFormat decimalFormat = new DecimalFormat("#");
		decimalFormat.setMaximumFractionDigits(2);
		return Double.parseDouble(decimalFormat.format(number));
	}
	
	public static void main(String[] args) {
		Double d1 = 123.1213123;
		Double d2 = 223123.1213123;
		Double d3 = 312123.1;
		System.out.println(DoubleFormatter.format(d1));
		System.out.println(DoubleFormatter.format(d2));
		System.out.println(DoubleFormatter.format(d3));
	}
}
