package pc;

import java.util.List;

/** Factory that maps an implementation name to a {@link Buffer}. */
public final class Buffers {

    /** All implementation keys, in the order they should be listed/tested. */
    public static final List<String> IMPLS = List.of("semaphore", "monitor", "lock", "queue");

    private Buffers() {
    }

    public static Buffer create(String impl, int capacity) {
        return switch (impl) {
            case "semaphore" -> new SemaphoreBuffer(capacity);
            case "monitor" -> new MonitorBuffer(capacity);
            case "lock" -> new LockBuffer(capacity);
            case "queue" -> new BlockingQueueBuffer(capacity);
            default -> throw new IllegalArgumentException(
                    "unknown impl '" + impl + "' (expected one of " + IMPLS + ")");
        };
    }
}
