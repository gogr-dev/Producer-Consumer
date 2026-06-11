package pc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Runs the bounded-buffer producer/consumer simulation.
 *
 * <p>Producers and consumers run on a thread pool for a fixed duration, each sleeping a
 * random interval between operations. When time is up the pool is shut down (which
 * interrupts any thread blocked in {@code put}/{@code take}) and a summary is printed,
 * including a check that {@code produced == consumed + items left in the buffer}.
 */
public final class ProducerConsumer {

    public static void main(String[] args) throws InterruptedException {
        Config cfg;
        try {
            cfg = Config.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("error: " + e.getMessage());
            System.err.println();
            Config.printUsage(System.err);
            System.exit(2);
            return;
        }
        if (cfg.help) {
            Config.printUsage(System.out);
            return;
        }

        Buffer buffer = Buffers.create(cfg.impl, cfg.capacity);
        Stats stats = new Stats();

        System.out.println("Producer-Consumer simulation");
        System.out.println("  implementation : " + buffer.name());
        System.out.println("  duration       : " + cfg.seconds + "s");
        System.out.println("  producers      : " + cfg.producers);
        System.out.println("  consumers      : " + cfg.consumers);
        System.out.println("  buffer capacity: " + cfg.capacity);
        System.out.println("  max sleep      : " + cfg.maxSleepMs + "ms");
        System.out.println();

        ExecutorService pool = Executors.newFixedThreadPool(cfg.producers + cfg.consumers);

        for (int i = 0; i < cfg.producers; i++) {
            pool.submit(producer("producer-" + (i + 1), buffer, stats, cfg.maxSleepMs, cfg.quiet));
        }
        for (int i = 0; i < cfg.consumers; i++) {
            pool.submit(consumer("consumer-" + (i + 1), buffer, stats, cfg.maxSleepMs, cfg.quiet));
        }

        long start = System.nanoTime();
        TimeUnit.SECONDS.sleep(cfg.seconds);

        // Stop: shutdownNow() interrupts threads blocked in put()/take() so they exit.
        pool.shutdownNow();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            System.err.println("warning: workers did not stop within 5s");
        }
        double elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0;

        printSummary(buffer, stats, elapsedSec);
    }

    private static Runnable producer(String name, Buffer buffer, Stats stats, int maxSleepMs, boolean quiet) {
        return () -> {
            Thread.currentThread().setName(name);
            String me = name;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    maybeSleep(maxSleepMs);
                    int item = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
                    buffer.put(item);
                    stats.recordProduced();
                    if (!quiet) {
                        System.out.println(me + " produced " + item);
                    }
                }
            } catch (InterruptedException e) {
                // Time is up — exit cleanly.
            }
        };
    }

    private static Runnable consumer(String name, Buffer buffer, Stats stats, int maxSleepMs, boolean quiet) {
        return () -> {
            Thread.currentThread().setName(name);
            String me = name;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    maybeSleep(maxSleepMs);
                    int item = buffer.take();
                    stats.recordConsumed();
                    if (!quiet) {
                        System.out.println("        " + me + " consumed " + item);
                    }
                }
            } catch (InterruptedException e) {
                // Time is up — exit cleanly.
            }
        };
    }

    private static void maybeSleep(int maxSleepMs) throws InterruptedException {
        if (maxSleepMs > 0) {
            Thread.sleep(ThreadLocalRandom.current().nextInt(maxSleepMs + 1));
        }
    }

    private static void printSummary(Buffer buffer, Stats stats, double elapsedSec) {
        long produced = stats.produced();
        long consumed = stats.consumed();
        int remaining = buffer.size();
        boolean balanced = produced == consumed + remaining;

        System.out.println();
        System.out.println("---- summary ----");
        System.out.printf("produced        : %d (%.0f/s)%n", produced, produced / elapsedSec);
        System.out.printf("consumed        : %d (%.0f/s)%n", consumed, consumed / elapsedSec);
        System.out.println("left in buffer  : " + remaining);
        System.out.println("conservation    : produced == consumed + buffered ? "
                + (balanced ? "OK" : "MISMATCH"));
    }
}
