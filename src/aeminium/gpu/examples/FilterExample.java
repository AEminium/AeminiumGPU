package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaFilter;

public class FilterExample {
    public static void main(String[] args) {
        int N = 1034;

        PList<Integer> input = new IntList();
        for (int i = 0; i < N; i++)
            input.add(i);
        input = input.filter(new LambdaFilter<Integer>() {
            @Override
            public boolean filter(Integer input) {
                return input % 2 == 0;
            }

            @Override
            public String getSource() {
                return "return input % 2 == 0;";
            }
        });

        input.evaluate();
        System.out.println("The first value is " + input.get(0));
        System.out.println("Length: " + input.size());
    }
}
