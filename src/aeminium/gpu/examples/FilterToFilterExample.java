package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaFilter;

public class FilterToFilterExample {
    public static void main(String[] args) {
        int N = 40;

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
                return "return input % 2 != 0;";
            }
        });

        input = input.filter(new LambdaFilter<Integer>() {
            @Override
            public boolean filter(Integer input) {
                return input % 5 == 0;
            }

            @Override
            public String getSource() {
                return "return input % 5 != 0;";
            }
        });

        input = input.filter(new LambdaFilter<Integer>() {
            @Override
            public boolean filter(Integer input) {
                return input % 3 == 0;
            }

            @Override
            public String getSource() {
                return "return input % 3 != 0;";
            }
        });

        System.out.println("Values: ");
        for (int i = 0; i < input.size(); i++) {
            System.out.println(input.get(i));
        }
        System.out.println("Length: " + input.size());
    }
}

