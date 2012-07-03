package aeminium.gpu.collections.properties.evaluation;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;

public interface LazyGPUHelper {
	public CLBuffer<?> getInputBuffer(CLContext ctx);
}
