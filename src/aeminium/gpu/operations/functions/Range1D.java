package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;

public class Range1D<R> extends Range2D<R, Void>{
	
	@SuppressWarnings("unchecked")
	public Range1D(String r) {
		starts = (PList<R>) CollectionFactory.listFromType(r);
		ends = (PList<R>) CollectionFactory.listFromType(r);
	}
	
	public void add(R s, R e) {
		starts.add(s);
		ends.add(e);
	}
}
