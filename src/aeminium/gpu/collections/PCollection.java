package aeminium.gpu.collections;


public interface PCollection<T>  {

	public int size();
	
	public T get(int index);
	
	/* Type Methods */
	public Class<?> getType();
	
}
