
import java.util.Random;
import java.util.concurrent.Semaphore;

public class ProducerConsumer {

    // Main loop
    public static void main(String[] args) throws InterruptedException {

        // Get arguments from command line
        int sleepTime = Integer.parseInt(args[0]);
        int Producers = Integer.parseInt(args[1]);
        int Consumers = Integer.parseInt(args[2]);
        int Size = Producers + Consumers;

        System.out.println("Using arguments from command line");
        System.out.println("Sleep time = " + sleepTime);
        System.out.println("Producer threads = " + Producers);
        System.out.println("Consumer threads = " + Consumers);
        System.out.println(" ");

        // Initialize buffer, empty, and full
        final ProgramCounter programCounter = new ProgramCounter();
        programCounter.boundedBuffer(5);
        programCounter.empty = new Semaphore(5); // empty = 5
        programCounter.full = new Semaphore(0); // full = 0
        programCounter.out = 0;
        programCounter.in = 0;
        programCounter.num = 0;

        Thread[] arr = new Thread[Size];
        for (int i = 0; i < Size; i++) {
            if (i < Producers) {
                arr[i] = new Thread(() -> {
                    try {
                        programCounter.producer();
                    } catch (InterruptedException ignored) {
                    }
                });
            } else {
                arr[i] = new Thread(() -> {
                    try {
                        programCounter.consumer();
                    } catch (InterruptedException ignored) {
                    }
                });
            }
            arr[i].start();
        }

        Thread.sleep(sleepTime * 1000); // Multiplies milliseconds by 1000 to get sleep time in seconds
        System.exit(0);
    }

    // Program Counter
    public static class ProgramCounter {
        private final Semaphore mutex = new Semaphore(1);
        private int[] buffer;
        private int bufferSize;
        private int out;
        private int in;
        private int num;
        private Semaphore empty, full;
        Random rand = new Random();

        // Producer
        public void producer() throws InterruptedException {
            int item;

            for (int i = 0; i < 100; i++) {
                Thread.sleep(rand.nextInt(501)); // Sleep a random amount between 0s and o.5s
                item = rand.nextInt(Integer.MAX_VALUE); // RNG

                if (insertItem(item)) {
                    System.out.println("ERROR- check items");
                } else {
                    System.out.println("Producer produced " + item);
                }
            }
        }

        // Consumer
        public void consumer() throws InterruptedException {
            int Consumed;

            for (int i = 0; i < 100; i++) {
                Thread.sleep(rand.nextInt(500)); // Sleep for random time between 0 and 0.5 seconds
                full.acquire();
                mutex.acquire();

                if (num != 0) {
                    Consumed = buffer[out];
                    out = (out + 1) % bufferSize;
                    num--;
                    System.out.println("        Consumer consumed " + Consumed);
                } else {
                    System.out.println("ERROR- check items");
                }
                mutex.release();
                empty.release();
            }
        }

        // Bounded Buffer
        public void boundedBuffer(int bufferSize) {
            this.bufferSize = bufferSize;
            buffer = new int[bufferSize];
        }

        // Check if program executed successfully and insert
        public boolean insertItem(int Produced) throws InterruptedException {
            empty.acquire();
            mutex.acquire();
            boolean run;

            if (num != bufferSize) {
                buffer[in] = Produced;
                in = (in + 1) % bufferSize;
                num++;
                run = false;
            } else {
                run = true;
            }

            mutex.release();
            full.release();
            return run;
        }
    }
}