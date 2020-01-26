package aeminium.gpu.recorder;

import aeminium.gpu.collections.lists.FloatList;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.Filter;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.utils.TimeoutController;

import java.util.Random;

public class FilterRecorder {

    int times = 10;
    public int[] sizes = new int[]{10, 100, 1000, 10000, 100000, 1000000, 10000000};

    GPUDevice dev = new DefaultDeviceFactory().getDevice();
    RecordTracker tracker = new RecordTracker();

    public static void main(String[] args) {
        FilterRecorder b = new FilterRecorder();
        b.run();
    }

    public void run() {
        executeExprMultipleSizes("mod2", new LambdaFilter<Integer>() {
            @Override
            public boolean filter(Integer input) {
                return input % 2 == 0;
            }

            @Override
            public String getSource() {
                return "return input % 2 == 0;";
            }
        });
//        executeExprMultipleSizes("prime", new LambdaFilter<Integer>() {
//            @Override
//            public boolean filter(Integer input) {
//                if(input <= 3) return input > 1;
//                if(input % 2 == 0 || input % 3 == 0) return false;
//                for(int x = 5 ; x * x < input ; x += 6) if(input % x == 0 || input % (x+2) == 0) return false;
//                return true;
//            }
//
//            @Override
//            public String getSource() {
//                return "if(input <= 3) return input > 1;\n" +
//                        "if(input % 2 == 0 || input % 3 == 0) return false;\n" +
//                        "for(int x = 5 ; x * x < input ; x += 6) if(input % x == 0 || input % (x+2) == 0) return false;\n" +
//                        "return true;";
//            }
//        });
        finish();
    }

    private void finish() {
        tracker.makeAverages();
    }

    public void executeExprMultipleSizes(String name,
                                         LambdaFilter<Integer> expr) {
        PList<Integer> input;
        for (int size : sizes) {
            input = generateRandomIntegerList(size);
            executeExprMultipleTimes(name, expr, input);
        }
    }

    public void executeExprMultipleTimes(String name,
                                         LambdaFilter<Integer> expr, PList<Integer> input) {
        System.out.println("Op:" + name + ", Size:" + input.size());
        LoggerTimer loggerGPU = new LoggerTimer("gpu", times, input.size(),
                name, tracker);
        LoggerTimer loggerCPU = new LoggerTimer("cpu", times, input.size(),
                name, tracker);
        for (int i = 0; i < times; i++) {
            executeExpr(name, expr, input, loggerGPU, loggerCPU);
            System.gc();
        }
    }

    public void executeExpr(String name, final LambdaFilter<Integer> expr,
                            final PList<Integer> input, LoggerTimer loggerGPU,
                            final LoggerTimer loggerCPU) {
        // Record GPU Times
        long t = System.nanoTime();
        Filter<Integer> op = new Filter<>(expr, input, dev);
        dev.setLogger(loggerGPU);
        op.gpuExecution(0, input.size());
        final long gpuTime = System.nanoTime() - t;
        System.out.println("GPU," + input.size() + "," + gpuTime);

        // Record CPU Times
        Runnable cpu = () -> {
            Filter<Integer> op1 = new Filter<>(expr, input, dev);
            long t1 = System.nanoTime();
            op1.cpuExecution(0, input.size());
            final long cpuTime = System.nanoTime() - t1;
            loggerCPU.saveTime("execution", cpuTime);
            System.out.println("CPU," + input.size() + "," + cpuTime);
        };
        try {
            TimeoutController.execute(cpu, 2 * gpuTime);
        } catch (TimeoutController.TimeoutException e) {
            loggerCPU.saveTime("execution", 2 * gpuTime);
        }
    }

    private static PList<Float> generateRandomFloatList(int size) {
        Random r = new Random(123412341234L);
        PList<Float> t = new FloatList();
        for (int i = 0; i < size; i++) {
            t.add((float) r.nextFloat());
        }
        return t;
    }

    private static PList<Integer> generateRandomIntegerList(int size) {
        Random r = new Random(123412341234L);
        PList<Integer> t = new IntList();
        for (int i = 0; i < size; i++) {
            t.add((int) r.nextInt());
        }
        return t;
    }
}
