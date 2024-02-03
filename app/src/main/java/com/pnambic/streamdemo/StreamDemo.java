package com.pnambic.streamdemo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StreamDemo {

    private static String WORD_CHARS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"
        + "abcdefghijklmnopqrstuvxyz"; 

    private final StopWatch collectorStopwatch = new StopWatch();

    private final StopWatch forEachStopwatch = new StopWatch();

    public int nonLocal;

    private <T> void secret(Collection<T> source) {
        nonLocal += source.size();
    }

    private Test<String> buildTest(String kind, int reps) {
        switch (kind) {
        case "testAB":
            return new TestAB<>(System.out, reps);
        case "testBA":
            return new TestBA<>(System.out, reps);
        case "testA":
            return new TestA<>(System.out, reps);
        case "testB":
            return new TestB<>(System.out, reps);
        }
        return new TestAB<>(System.out, reps);
        }

    private static List<String> buildSource(int sourceSize, int wordMin, int wordMax) {
        int wordRange = wordMax - wordMin;
        List<String> result = new ArrayList<>(sourceSize);
        for(; sourceSize > 0; --sourceSize) {
            int wordSize = wordMin + (int)(wordRange * Math.random());
            result.add(randomWord(wordSize));
        }
        return result;
    }

    private static String randomWord(int size)  {
        StringBuffer result = new StringBuffer(size);
 
        for (; size > 0; --size) {
            int index = (int)(WORD_CHARS.length() * Math.random()); 
            result.append(WORD_CHARS.charAt(index)); 
        }
        return  result.toString(); 
    }

    public static void main(String[] args) {
        Cli cli = new Cli(args);
        String kind = cli.nextText("testAB");
        int reps = cli.nextInt(1000);
        int sourceSize = cli.nextInt(100);
        int trial = cli.nextInt(10);
        int wordMin = cli.nextInt(8);
        int wordMax = cli.nextInt(15);

        String description = "Running test " + kind + " on " + trial + " trials each with "
            + reps + " reps using a source array of " + sourceSize + " elements.";
        System.out.println(description);

        StreamDemo runner = new StreamDemo();
        Test<String> test = runner.buildTest(kind, reps);
        List<String> source = buildSource(sourceSize, wordMin, wordMax);
        for(; trial > 0; --trial) {
            test.test(source);
        }
    }

    private static class Cli {

        private final String[] args;

        private int index;

        public Cli(String[] args) {
            this.args = args;
            this.index = 0;
        }

        public String nextText(String orValue) {
            if (args.length > index) {
                return args[index++];
            }
            return orValue;
        }

        public int nextInt(int orValue) {
            if (args.length > index) {
                return Integer.parseInt(args[index++]);
            }
            return orValue;
        }
    }

    private static interface Test<T> {
        void test(Collection<T> source);
    }

    private abstract class AbstractTest<T> implements Test<T> {

        protected final PrintStream out;

        protected final int reps;

        private final boolean forCsv = true;

        public AbstractTest(PrintStream out, int reps) {
            this.out = out;
            this.reps = reps;
        }

        protected void asCollector(Collection<T> source, int reps) {
            for(; reps > 0; --reps) {
                List<T> result = source.stream()
                    .collect(Collectors.toList());
                secret(result);
            }
        }

        protected void asForEach(Collection<T> source, int reps) {
            for(; reps > 0; --reps) {
                List<T> result = new ArrayList<>(source.size());
                source.stream()
                    .forEach(result::add);
                secret(result);
            }
        }

        protected void reportTest() {
            if (!forCsv) {
                out.println("Test completed " + reps + " reps, secret " + nonLocal + ".");
            }
        }

        protected void reportStats(String label, StopWatch timer) {
            if (forCsv) {
                out.println(label + ", " + timer.reportCsv());
                return;
            }
            out.println(label + ", " + timer.reportStats());
        }
    }

    private class TestAB<T> extends AbstractTest<T> {

        public TestAB(PrintStream out, int reps) {
            super(out, reps);
        }

        @Override
        public void test(Collection<T> source) {
            collectorStopwatch.time(() -> asCollector(source, reps));
            forEachStopwatch.time(() -> asForEach(source, reps));

            reportTest();
            reportStats("asCollector", collectorStopwatch);
            reportStats("asForEach", forEachStopwatch);
        }
    }

    private class TestBA<T> extends AbstractTest<T> {

        public TestBA(PrintStream out, int reps) {
            super(out, reps);
        }

        @Override
        public void test(Collection<T> source) {
            forEachStopwatch.time(() -> asForEach(source, reps));
            collectorStopwatch.time(() -> asCollector(source, reps));

            reportTest();
            reportStats("asForEach", forEachStopwatch);
            reportStats("asCollector", collectorStopwatch);
        }
    }

    private class TestA<T> extends AbstractTest<T> {

        public TestA(PrintStream out, int reps) {
            super(out, reps);
        }

        @Override
        public void test(Collection<T> source) {
            collectorStopwatch.time(() -> asCollector(source, reps));

            reportTest();
            reportStats("asCollector", collectorStopwatch);
        }
    }

    private class TestB<T> extends AbstractTest<T> {

        public TestB(PrintStream out, int reps) {
            super(out, reps);
        }

        @Override
        public void test(Collection<T> source) {
            forEachStopwatch.time(() -> asForEach(source, reps));

            reportTest();
            reportStats("asForEach", forEachStopwatch);
        }
    }

    @FunctionalInterface
    private static interface StreamTest {
        void runTest();
    }

    private static class StopWatch {

        private long startNano;

        private long startTotalMem;

        private long startFreeMem;

        private long stopNano;

        private long stopTotalMem;

        private long stopFreeMem;

        private final Runtime runtime = Runtime.getRuntime();

        public void time(StreamTest test) {
            startTotalMem = runtime.totalMemory();
            startFreeMem = runtime.freeMemory();
            startNano = System.nanoTime();
            test.runTest();
            stopNano = System.nanoTime();
            stopTotalMem = runtime.totalMemory();
            stopFreeMem = runtime.freeMemory();
        }

        public long getTestTime() {
            return stopNano - startNano;
        }

        public String reportCsv() {
            StringBuffer result = new StringBuffer();
            result.append(getTestTime());
            result.append(", ");
            result.append(startTotalMem);
            result.append(", ");
            result.append(startFreeMem);
            result.append(", ");
            result.append(stopTotalMem);
            result.append(", ");
            result.append(stopFreeMem);
            return result.toString();
        }

        public String reportStats() {
            StringBuffer result = new StringBuffer();
            result.append("nano-duration ");
            result.append(getTestTime());
            result.append(" start-mem ");
            result.append(startTotalMem);
            result.append(" start-free ");
            result.append(startFreeMem);
            result.append(" stop-mem ");
            result.append(stopTotalMem);
            result.append(" stop-free ");
            result.append(stopFreeMem);
            return result.toString();
        }
    }
}
