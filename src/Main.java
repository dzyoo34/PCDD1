import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.concurrent.*;


class Producer implements Runnable {

    private final String name;
    private final BlockingQueue<Integer> queue;
    private final int batchSize;
    private final AtomicInteger activeConsumers;
    private final AtomicInteger consumedTotal;
    private final int totalNeeded;
    private final Random random = new Random();

    public Producer(String name, BlockingQueue<Integer> queue,
                    int batchSize, AtomicInteger activeConsumers,
                    AtomicInteger consumedTotal, int totalNeeded) {

        this.name = name;
        this.queue = queue;
        this.batchSize = batchSize;
        this.activeConsumers = activeConsumers;
        this.consumedTotal = consumedTotal;
        this.totalNeeded = totalNeeded;
    }

    private int generateOdd() {
        int n = random.nextInt(100);
        return (n % 2 == 0) ? n + 1 : n;
    }

    public void run() {
        try {
            while (activeConsumers.get() > 0 && consumedTotal.get() < totalNeeded) {

                for (int i = 0; i < batchSize; i++) {

                    if (activeConsumers.get() == 0 ||
                            consumedTotal.get() >= totalNeeded) {
                        System.out.println(name + " stopping — no more needed.");
                        return;
                    }

                    int item = generateOdd();

                    if (queue.remainingCapacity() == 0) {
                        System.out.println(name + ": Depot FULL! Waiting...");
                    }

                    queue.put(item);
                    System.out.println(name + " produced: " + item +
                            " | stock: " + queue.size());
                }
            }

            System.out.println(name + " FINISHED.");

        } catch (InterruptedException ignored) {
        }
    }
}

class Consumer implements Runnable {

    private final String name;
    private final BlockingQueue<Integer> queue;
    private final int goal;
    private final AtomicInteger activeConsumers;
    private final Object monitor;
    private final AtomicInteger consumedTotal;

    public Consumer(String name, BlockingQueue<Integer> queue, int goal,
                    AtomicInteger activeConsumers, Object monitor,
                    AtomicInteger consumedTotal) {

        this.name = name;
        this.queue = queue;
        this.goal = goal;
        this.activeConsumers = activeConsumers;
        this.monitor = monitor;
        this.consumedTotal = consumedTotal;
    }

    public void run() {
        int taken = 0;

        try {
            while (taken < goal) {

                if (queue.isEmpty()) {
                    System.out.println(name + ": Depot is EMPTY! Waiting...");
                }

                Integer item = queue.take();
                taken++;
                consumedTotal.incrementAndGet();

                System.out.println(name + " consumed: " + item +
                        " (" + taken + "/" + goal + ")");
            }

            System.out.println(name + " satisfied and leaving.");

        } catch (InterruptedException ignored) {

        } finally {
            int left = activeConsumers.decrementAndGet();
            System.out.println(name + " left. Remaining consumers: " + left);

            if (left == 0) {
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        }
    }
}


public class Main {
    public static void main(String[] args) {

        int X = 3;   // Producers
        int Y = 4;   // Consumers
        int Z = 2;   // Each consumer needs
        int D = 5;   // Depot capacity
        int F = 2;   // Items per production batch

        BlockingQueue<Integer> depot = new ArrayBlockingQueue<>(D);

        ExecutorService executor = Executors.newFixedThreadPool(X + Y);

        AtomicInteger activeConsumers = new AtomicInteger(Y);
        Object monitor = new Object();


        int totalNeeded = Y * Z;
        AtomicInteger consumedTotal = new AtomicInteger(0);

        // RUN PRODUCERS
        for (int i = 0; i < X; i++) {
            executor.submit(new Producer("Producer-" + (i + 1), depot, F,
                    activeConsumers, consumedTotal, totalNeeded));
        }

        // RUN CONSUMERS
        for (int i = 0; i < Y; i++) {
            executor.submit(new Consumer("Consumer-" + (i + 1), depot, Z,
                    activeConsumers, monitor, consumedTotal));
        }

        // MAIN WAITS UNTIL ALL CONSUMERS FINISH
        synchronized (monitor) {
            while (activeConsumers.get() > 0) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        System.out.println("\n--- All consumers finished. Stopping producers... ---");
        executor.shutdownNow();
    }
}
