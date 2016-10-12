package aeminium.gpu.collections.lazyness;

import java.util.Random;

import aeminium.gpu.collections.lists.FloatList;

public class RandomList extends FloatList {

	protected int max;
	protected int seed;
	protected float[] arr;

	public RandomList(int max) {
		this(max, new Random().nextInt());
	}

	public RandomList(int max, int seed) {
		this(new float[max], max, seed);
	}
	
	protected RandomList(float[] arr, int max, int seed) {
		super(arr, max);
		this.max = max;
		this.seed = seed;
		Random r = new Random(seed);
		for (int i=0; i < max; i++) {
			arr[i] = r.nextFloat();
		}
	}
}
