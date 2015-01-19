package aeminium.gpu.utils;

public class Quad<R, R2> {
	
	public R s;
	public R e;
	public R2 t;
	public R2 b;
	
	public Quad(R st, R end, R2 top, R2 bot) {
		s = st;
		e = end;
		t = top;
		b = bot;
	}

}
