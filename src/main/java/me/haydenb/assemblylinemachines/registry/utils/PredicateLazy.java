package me.haydenb.assemblylinemachines.registry.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Proxy object inspired by Forge's Lazy which allows selective caching; if supplied predicate is false, it will continue to dynamically
 * calculate return value until predicate is true, at which point the result will be cached permanently.
 * @author Hayden Belanger
 */
public class PredicateLazy<T> implements Supplier<T>{

	private Supplier<T> supplier;
	private Predicate<T> predicate;
	private T instance;

	public static <VT> PredicateLazy<VT> of(Supplier<VT> supplier, Predicate<VT> predicate){
		return new PredicateLazy<>(supplier, predicate);
	}

	public static <VT> PredicateLazy<VT> of(Supplier<VT> supplier){
		return new PredicateLazy<>(supplier, (o) -> true);
	}

	private PredicateLazy(Supplier<T> supplier, Predicate<T> predicate) {
		this.supplier = supplier;
		this.predicate = predicate;
	}

	@Override
	public T get() {
		if(supplier != null && predicate != null) {
			T result = supplier.get();
			if(!predicate.test(result)) return result;
			instance = result;
			supplier = null;
			predicate = null;
		}
		return instance;
	}
	
	public static class ClearablePredicateLazy<T> extends PredicateLazy<T>{
		
		private ClearablePredicateLazy(Supplier<T> supplier, Predicate<T> predicate) {
			super(supplier, predicate);
		}
		
		public static <VT> ClearablePredicateLazy<VT> of(Supplier<VT> supplier, Predicate<VT> predicate){
			return new ClearablePredicateLazy<>(supplier, predicate);
		}

		public static <VT> ClearablePredicateLazy<VT> of(Supplier<VT> supplier){
			return new ClearablePredicateLazy<>(supplier, (o) -> true);
		}
		
		public void clear() {
			super.instance = null;
		}
	}
}
