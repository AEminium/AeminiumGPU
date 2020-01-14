package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.properties.operations.Filter;
import aeminium.gpu.utils.UniqCounter;

public abstract class LambdaFilter<I> implements Filter<I>, GPUFunction {
    private String id = null;

    /* This method should be overridden by the Aeminium GPU Compiler */
    public String getSource() {
        return null;
    }

    /* This method should be overridden by the Aeminium GPU Compiler */
    public String getSourceComplexity() {
        return null;
    }

    /* This method should be overridden by the Aeminium GPU Compiler */
    public String[] getParameters() {
        return new String[]{"input"};
    }

    /* This method should be overridden by the Aeminium GPU Compiler */
    public String getFeatures() {
        return null;
    }

    /* This method should be overridden by the Aeminium GPU Compiler */
    public String getId() {
        if (id == null)
            id = UniqCounter.getNewId();
        return id;
    }
}
