package me.haydenb.assemblylinemachines.registry.utils;

import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class FormattingHelper {

	private static final NavigableMap<Long, String> SUFFIX = new TreeMap<>();

	public static final DecimalFormat GENERAL_FORMAT = new DecimalFormat("###,###,###,###,###,###,###");
	public static final DecimalFormat FEPT_FORMAT = new DecimalFormat("###,##0.#");


	static {
		SUFFIX.put(1_000L, "K");
		SUFFIX.put(1_000_000L, "M");
		SUFFIX.put(1_000_000_000L, "G");
		SUFFIX.put(1_000_000_000_000L, "T");
		SUFFIX.put(1_000_000_000_000_000L, "P");
		SUFFIX.put(1_000_000_000_000_000_000L, "E");
	}

	public static String formatToSuffix(long value) {
		// Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
		if (value == Long.MIN_VALUE)
			return formatToSuffix(Long.MIN_VALUE + 1);
		if (value < 0)
			return "-" + formatToSuffix(-value);
		if (value < 1000)
			return Long.toString(value); // deal with easy case

		Entry<Long, String> e = SUFFIX.floorEntry(value);
		Long divideBy = e.getKey();
		String suffix = e.getValue();

		long truncated = value / (divideBy / 10); // the number part of the output times 10
		boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
		return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
	}

	public static Character getCharForNumber(int i) {
	    return i > 0 && i < 27 ? (char)(i + 'A' - 1) : null;
	}
}