package pc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link ReentrantLock} solution with two {@link Condition} queues — the modern
 * {@code java.util.concurrent} equivalent of the monitor version.
 *
 * <p>Unlike intrinsic locks, distinct conditions let us wake only the threads that can
 * actually make progress: a {@code put} signals {@code notEmpty} (a waiting consumer),
 * and a {@code take} signals {@code notFull} (a waiting producer), avoiding the
 * wake-everyone overhead of {@code notifyAll()}.
 */
public final class LockBuffer implements Buffer {

    private final int[] slots;
    private int in = 0;
    private int out = 0;
    private int count = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public LockBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1, got " + capacity);
        }
        slots = new int[capacity];
    }

    @Override
    public void put(int item) throws InterruptedException {
        lock.lock();
        try {
            while (count == slots.length) {
                notFull.await();
            }
            slots[in] = item;
            in = (in + 1) % slots.length;
            count++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            int item = slots[out];
            out = (out + 1) % slots.length;
            count--;
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int capacity() {
        return slots.length;
    }

    @Override
    public String name() {
        return "lock (ReentrantLock / Condition)";
    }
}
