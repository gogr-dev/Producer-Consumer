package pc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * A dependency-free correctness stress test for every {@link Buffer} implementation.
 *
 * <p>Producers emit the distinct integers {@code 0..total-1} (each exactly once, claimed
 * via a shared counter); consumers claim exactly {@code total} takes the same way and
 * tally each value they see. A correct buffer neither loses, duplicates, nor corrupts
 * items, so afterwards every value must have been seen exactly once. A watchdog fails the
 * run if the threads deadlock.
 */
public final class BufferTest {

    private static final int CAPACITY = 8;
    private static final int PRODUCERS = 4;
    private static final int CONSUMERS = 4;
    private static final int TOTAL = 200_000;
    private static final int TIMEOUT_SECONDS = 30;

    public static void main(String[] args) throws InterruptedException {
        boolean allPassed = true;
        for (String impl : Buffers.IMPLS) {
            allPassed &= runOne(impl);
        }
        System.out.println();
        System.out.println(allPassed ? "ALL TESTS PASSED" : "TESTS FAILED");
        if (!allPassed) {
            System.exit(1);
        }
    }

    private static boolean runOne(String impl) throws InterruptedException {
        Buffer buffer = Buffers.create(impl, CAPACITY);
        AtomicIntegerArray seen = new AtomicIntegerArray(TOTAL);

        // Shared claim counters guarantee each integer is produced once and exactly
        // TOTAL takes happen (so no consumer blocks forever waiting for a stray item).
        java.util.concurrent.atomic.AtomicInteger toProduce = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger toConsume = new java.util.concurrent.atomic.AtomicInteger(TOTAL);

        ExecutorService pool = Executors.newFixedThreadPool(PRODUCERS + CONSUMERS);
        for (int i = 0; i < PRODUCERS; i++) {
            pool.submit(() -> {
                try {
                    int item;
                    while ((item = toProduce.getAndIncrement()) < TOTAL) {
                        buffer.put(item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        for (int i = 0; i < CONSUMERS; i++) {
            pool.submit(() -> {
                try {
                    while (toConsume.getAndDecrement() > 0) {
                        int item = buffer.take();
                        seen.incrementAndGet(item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        pool.shutdown();
        long startNs = System.nanoTime();
        boolean finished = pool.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        double ms = (System.nanoTime() - startNs) / 1_000_000.0;

        if (!finished) {
            pool.shutdownNow();
            System.out.printf("FAIL  %-10s deadlock/timeout after %ds%n", impl, TIMEOUT_SECONDS);
            return false;
        }

        int missing = 0;
        int duplicated = 0;
        int leftover = buffer.size();
        for (int v = 0; v < TOTAL; v++) {
            int c = seen.get(v);
            if (c == 0) {
                missing++;
            } else if (c > 1) {
                duplicated++;
            }
        }

        boolean ok = missing == 0 && duplicated == 0 && leftover == 0;
        System.out.printf("%-5s %-10s %,d items in %6.0f ms  (missing=%d, duplicated=%d, leftover=%d)%n",
                ok ? "PASS" : "FAIL", impl, TOTAL, ms, missing, duplicated, leftover);
        return ok;
    }
}
