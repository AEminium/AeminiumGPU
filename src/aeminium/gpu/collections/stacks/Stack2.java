package aeminium.gpu.collections.stacks;

import aeminium.gpu.collections.AbstractPObject;
import aeminium.gpu.collections.PNativeWrapper;
import aeminium.gpu.collections.PObject;

public class Stack2<A, B> extends AbstractPObject implements PObject {

	A a;
	B b;
	
	public Stack2(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public String getCLType() {
		return convert(a).getCLType() + "," + convert(b).getCLType();
	}
	
	
	public PObject convert(Object o) {
		if (o instanceof PObject) {
			return (PObject) o;
		}
		if (o instanceof Number) {
			return new PNativeWrapper<Number>((Number) o);
		}
		return null;
	}
	
	
	public A get1() {
		return a;
	}
	
	public void set1(A n) {
		a = n;
	}
	
	public B get2() {
		return b;
	}
	
	public void set3(B n) {
		b = n;
	}
	
	@SuppressWarnings("unchecked")
	public Stack2<A, B> copy() {
		A na = null;
		B nb = null;
		if (a instanceof Number) na = a;
		else if (a instanceof PObject) na = (A) ((PObject) a).copy();
		if (b instanceof Number) nb = b;
		else if (b instanceof PObject) nb = (B) ((PObject) b).copy();
		return new Stack2<A, B>(na, nb);
	}

}
