package pc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The "you'd actually use this in production" version: delegate the whole problem to
 * {@link ArrayBlockingQueue}, which already implements a bounded, blocking, thread-safe
 * buffer. Included as a baseline to compare the hand-rolled implementations against.
 */
public final class BlockingQueueBuffer implements Buffer {

    private final BlockingQueue<Integer> queue;
    private final int capacity;

    public BlockingQueueBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1, got " + capacity);
        }
        this.capacity = capacity;
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void put(int item) throws InterruptedException {
        queue.put(item);
    }

    @Override
    public int take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public String name() {
        return "blocking-queue (java.util.concurrent.ArrayBlockingQueue)";
    }
}
