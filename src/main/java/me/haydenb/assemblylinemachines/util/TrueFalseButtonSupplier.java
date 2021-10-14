package me.haydenb.assemblylinemachines.util;

import java.util.function.Supplier;

public class TrueFalseButtonSupplier {
	private final String trueText;
	private final String falseText;
	
	public final Supplier<Boolean> supplier;

	public TrueFalseButtonSupplier(String trueText, String falseText, Supplier<Boolean> supplier) {
		this.trueText = trueText;
		this.falseText = falseText;
		this.supplier = supplier;

	}
	
	public TrueFalseButtonSupplier(String text, Supplier<Boolean> supplier) {
		this.trueText = text;
		this.falseText = text;
		this.supplier = supplier;

	}
	
	public boolean get() {
		return supplier.get();
	}
	
	public String getTrueText() {
		return trueText;
	}
	
	public String getFalseText() {
		return falseText;
	}
	
	@Deprecated
	public String getTextFromSupplier() {
		if(get()) {
			return trueText;
		}else {
			return falseText;
		}
	}
	
	
}
