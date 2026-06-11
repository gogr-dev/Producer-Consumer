package pc;

/**
 * A fixed-capacity buffer shared between producer and consumer threads.
 *
 * <p>{@link #put} blocks while the buffer is full; {@link #take} blocks while it is
 * empty. The four implementations in this package solve that same coordination problem
 * with different synchronization primitives, so they are interchangeable behind this
 * interface.
 */
public interface Buffer {

    /** Insert an item, blocking until a slot is free. */
    void put(int item) throws InterruptedException;

    /** Remove and return an item, blocking until one is available. */
    int take() throws InterruptedException;

    /** Number of items currently buffered (best-effort snapshot, for reporting). */
    int size();

    /** Maximum number of items the buffer can hold. */
    int capacity();

    /** Human-readable name of the synchronization strategy, for logs. */
    String name();
}
