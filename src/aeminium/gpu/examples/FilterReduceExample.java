package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

@SuppressWarnings("DuplicatedCode")
public class FilterReduceExample {
    public static void main(String[] args) {
        int N = 10;

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

        int sum = input.reduce(new LambdaReducerWithSeed<Integer>() {

            @Override
            public Integer combine(Integer input, Integer other) {
                return input + other;
            }

            @Override
            public String getSource() {
                return "return reduce_input_first + reduce_input_second;";
            }

            @Override
            public Integer getSeed() {
                return 0;
            }

        });
        System.out.println("The sum of the first " + N + " even numbers is " + sum);
    }
}
