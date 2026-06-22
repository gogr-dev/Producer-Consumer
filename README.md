# Producer–Consumer

A Java implementation of the classic **bounded-buffer producer–consumer** synchronization
problem — and, because the same problem has several canonical solutions, **four
interchangeable implementations** of the shared buffer that you can swap and compare:

| `--impl`    | Strategy                                                | Primitive |
| ----------- | ------------------------------------------------------- | --------- |
| `semaphore` | `mutex` + `empty`/`full` counting semaphores            | `java.util.concurrent.Semaphore` |
| `monitor`   | `synchronized` methods with `wait()` / `notifyAll()`    | intrinsic monitor |
| `lock`      | `ReentrantLock` with `notFull` / `notEmpty` conditions  | explicit lock + conditions |
| `queue`     | delegate to a ready-made bounded blocking queue         | `ArrayBlockingQueue` |

Producers generate random integers and `put` them into a fixed-capacity buffer;
consumers `take` them out. When the buffer is full, producers block; when it is empty,
consumers block — no busy-waiting, no lost or duplicated items. It started as a one-day
class exercise (the original `semaphore` version); this is the fuller version of the same
idea.

## How the buffer protects itself

All four implementations enforce the same two invariants — the buffer never overflows and
never underflows — and serialize access to the shared indices. The original semaphore
version makes the mechanics explicit:

- **`mutex`** (binary semaphore) gives a thread exclusive access while it touches the
  slots and `in`/`out` indices.
- **`empty`** counts free slots and starts at `capacity`; a producer must acquire a permit
  before inserting, so it blocks when the buffer is full.
- **`full`** counts filled slots and starts at `0`; a consumer must acquire a permit
  before removing, so it blocks when the buffer is empty.

The other three reach the same guarantees with a monitor, an explicit lock with condition
variables, or a library `BlockingQueue` — a side-by-side tour of how Java expresses
thread coordination.

## Build & run

Requires a JDK (17+; developed on JDK 25). No external dependencies.

```bash
make            # compile to out/
make run        # run the simulation with defaults
make test       # run the correctness stress test across all four implementations
make clean
```

Pass arguments through `ARGS`:

```bash
make run ARGS="--seconds 5 --producers 4 --consumers 2 --impl lock"
make run ARGS="--seconds 10 --quiet"
```

Or invoke directly after `make`:

```bash
java -cp out pc.ProducerConsumer --help
```

## Options

```
--seconds N     how long to run the simulation   (default 20)
--producers N   number of producer threads       (default 2)
--consumers N   number of consumer threads        (default 2)
--capacity N    bounded-buffer capacity           (default 5)
--max-sleep MS  max random sleep per operation    (default 500)
--impl NAME     semaphore | monitor | lock | queue (default semaphore)
--quiet         print only the final summary
-h, --help      show this help
```

The original positional form still works: `java pc.ProducerConsumer 20 10 5`
(`seconds producers consumers`).

## Example

```
$ make run ARGS="--seconds 3 --producers 4 --consumers 2 --impl lock --quiet"
Producer-Consumer simulation
  implementation : lock (ReentrantLock / Condition)
  duration       : 3s
  producers      : 4
  consumers      : 2
  buffer capacity: 5
  max sleep      : 500ms

---- summary ----
produced        : 28 (9/s)
consumed        : 23 (8/s)
left in buffer  : 5
conservation    : produced == consumed + buffered ? OK
```

```
$ make test
PASS  semaphore  200,000 items in    244 ms  (missing=0, duplicated=0, leftover=0)
PASS  monitor    200,000 items in    319 ms  (missing=0, duplicated=0, leftover=0)
PASS  lock       200,000 items in    507 ms  (missing=0, duplicated=0, leftover=0)
PASS  queue      200,000 items in    322 ms  (missing=0, duplicated=0, leftover=0)

ALL TESTS PASSED
```

## Project structure

```
src/pc/
  Buffer.java               # the shared interface
  SemaphoreBuffer.java      # mutex + empty/full semaphores (the original, cleaned up)
  MonitorBuffer.java        # synchronized + wait/notifyAll
  LockBuffer.java           # ReentrantLock + Conditions
  BlockingQueueBuffer.java  # ArrayBlockingQueue baseline
  Buffers.java              # factory: name -> implementation
  Stats.java                # thread-safe produced/consumed tallies
  Config.java               # command-line parsing + usage
  ProducerConsumer.java     # the simulation (entry point)
  BufferTest.java           # correctness stress test (entry point)
Makefile
```

## License

MIT.
