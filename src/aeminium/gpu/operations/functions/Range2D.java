package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;

public class Range2D<R, R2> {
	public PList<R> starts;
	public PList<R> ends;
	public PList<R2> tops;
	public PList<R2> bottoms;
	
	
	public Range2D() {
		tops = null;
		bottoms = null;
	}
	
	@SuppressWarnings("unchecked")
	public Range2D(String r, String r2) {
		starts = (PList<R>) CollectionFactory.listFromType(r);
		ends = (PList<R>) CollectionFactory.listFromType(r);
		tops = (PList<R2>) CollectionFactory.listFromType(r2);
		bottoms = (PList<R2>) CollectionFactory.listFromType(r2);
	}
	
	public void add(R s, R e, R2 t, R2 b) {
		starts.add(s);
		ends.add(e);
		if (tops != null) tops.add(t);
		if (bottoms != null) bottoms.add(b);
	}
	
	public int size() {
		return starts.size();
	}

}
