package me.haydenb.assemblylinemachines.util;

import java.util.function.Supplier;

public class SupplierWrapper {
	private final String trueText;
	private final String falseText;
	public final Supplier<Boolean> supplier;

	public SupplierWrapper(String trueText, String falseText, Supplier<Boolean> supplier) {
		this.trueText = trueText;
		this.falseText = falseText;
		this.supplier = supplier;

	}

	public String getTextFromSupplier() {
		if (supplier.get()) {
			return trueText;
		} else {
			return falseText;
		}
	}
}
