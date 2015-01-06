package aeminium.gpu.collections;

public interface PCollection<T> extends PObject {

	public int size();

	/* Type Methods */
	public Class<?> getType();

}
