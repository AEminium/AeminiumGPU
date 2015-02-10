package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.lists.CharList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

public class CharMatrix extends AbstractMatrix<Character> implements
		ConcreteCollection {

	protected char[] box;

	public CharMatrix(int m, int n) {
		this(new char[m * n], m, n);
	}

	public CharMatrix(char[] box, int m, int n) {
		super(m, n);
		this.box = box;
	}

	@Override
	public Character get(int i, int j) {
		return box[i * cols + j];
	}

	@Override
	public void set(int i, int j, Character e) {
		box[i * cols + j] = e;
	}

	@Override
	public Class<?> getContainingType() {
		return Character.class;
	}

	@Override
	public PList<Character> elements() {
		return new CharList(box, size);
	}
	
	public void replaceBy(PMatrix<?> newMatrix) {
		CharMatrix nb = (CharMatrix) newMatrix;
		box = nb.box;
	}

}
