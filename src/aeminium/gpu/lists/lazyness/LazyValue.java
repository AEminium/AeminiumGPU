package aeminium.gpu.lists.lazyness;

public class LazyValue<T> {
	private boolean evaluated = false;
	private T actual;
	private LazyEvaluator<T> evaluator;
	
	public LazyValue(LazyEvaluator<T> eval) {
		evaluator = eval;
	}
	
	@SuppressWarnings("unchecked")
	public T evaluate() {
		if (!evaluated) {
			actual = (T) evaluator.evaluate();
			evaluated = true;
		}
		return actual;
	}
}
