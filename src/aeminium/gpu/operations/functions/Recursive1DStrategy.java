package aeminium.gpu.operations.functions;


public abstract class Recursive1DStrategy<R extends Number,T> extends Recursive2DStrategy<R, Void, T> {
	
	public abstract T iterative(R r, R l, RecursiveCallback result);
	public abstract Range2D<R, Void> split(R s, R e, int n);
	
	public T iterative(R r, R l, Void t, Void b, RecursiveCallback result) {
		return iterative(r, l, result);
	}
	
	public Range2D<R, Void> split(R s, R e, Void t, Void b,int n) {
		return split(s, e, n);
	}

	public Void getTop() { return null; }
	public Void getBottom() { return null; }
}
