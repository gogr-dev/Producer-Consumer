package pc;

import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe tallies of how many items were produced and consumed during a run. */
public final class Stats {

    private final AtomicLong produced = new AtomicLong();
    private final AtomicLong consumed = new AtomicLong();

    public void recordProduced() {
        produced.incrementAndGet();
    }

    public void recordConsumed() {
        consumed.incrementAndGet();
    }

    public long produced() {
        return produced.get();
    }

    public long consumed() {
        return consumed.get();
    }
}
