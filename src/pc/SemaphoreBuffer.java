package pc;

import java.util.concurrent.Semaphore;

/**
 * The classic textbook solution, using three semaphores:
 *
 * <ul>
 *   <li>{@code mutex} - binary semaphore granting exclusive access to the slots/indices.</li>
 *   <li>{@code empty} - counts free slots; a producer acquires one before inserting.</li>
 *   <li>{@code full}  - counts filled slots; a consumer acquires one before removing.</li>
 * </ul>
 *
 * Because {@code empty} and {@code full} block until a permit is available, there is no
 * need to re-check "is there room?" / "is there an item?" after acquiring one — the
 * permit <em>is</em> the guarantee. This is the original implementation, cleaned up.
 */
public final class SemaphoreBuffer implements Buffer {

    private final int[] slots;
    private int in = 0;   // next index to write
    private int out = 0;  // next index to read

    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore empty; // free slots, starts at capacity
    private final Semaphore full;  // filled slots, starts at 0

    public SemaphoreBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1, got " + capacity);
        }
        slots = new int[capacity];
        empty = new Semaphore(capacity);
        full = new Semaphore(0);
    }

    @Override
    public void put(int item) throws InterruptedException {
        empty.acquire();
        mutex.acquire();
        try {
            slots[in] = item;
            in = (in + 1) % slots.length;
        } finally {
            mutex.release();
        }
        full.release();
    }

    @Override
    public int take() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        try {
            int item = slots[out];
            out = (out + 1) % slots.length;
            return item;
        } finally {
            mutex.release();
            empty.release();
        }
    }

    @Override
    public int size() {
        return full.availablePermits();
    }

    @Override
    public int capacity() {
        return slots.length;
    }

    @Override
    public String name() {
        return "semaphore (mutex / empty / full)";
    }
}
