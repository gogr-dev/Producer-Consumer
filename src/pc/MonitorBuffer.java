package pc;

/**
 * A monitor-based solution using Java's intrinsic locks: {@code synchronized} methods
 * plus {@code wait()} / {@code notifyAll()}.
 *
 * <p>A thread that cannot proceed releases the lock and waits; whenever the buffer
 * changes, every waiter is woken to re-check its condition in a {@code while} loop
 * (guarding against spurious wakeups). This is the same idea as the semaphore version,
 * expressed with the language's built-in monitor.
 */
public final class MonitorBuffer implements Buffer {

    private final int[] slots;
    private int in = 0;
    private int out = 0;
    private int count = 0;

    public MonitorBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1, got " + capacity);
        }
        slots = new int[capacity];
    }

    @Override
    public synchronized void put(int item) throws InterruptedException {
        while (count == slots.length) {
            wait();
        }
        slots[in] = item;
        in = (in + 1) % slots.length;
        count++;
        notifyAll();
    }

    @Override
    public synchronized int take() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        int item = slots[out];
        out = (out + 1) % slots.length;
        count--;
        notifyAll();
        return item;
    }

    @Override
    public synchronized int size() {
        return count;
    }

    @Override
    public int capacity() {
        return slots.length;
    }

    @Override
    public String name() {
        return "monitor (synchronized / wait / notifyAll)";
    }
}
