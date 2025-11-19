import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int PRODUCER_COUNT = 3;
    private static final int CONSUMER_COUNT = 4;
    private static final int CONSUMER_GOAL = 2;
    private static final int BUFFER_CAPACITY = 5;
    private static final int TOTAL_OBJECTS = CONSUMER_GOAL * CONSUMER_COUNT;

    private static final BlockingQueue<Integer> sklad = new ArrayBlockingQueue<>(BUFFER_CAPACITY);

    private static final AtomicInteger totalProduced = new AtomicInteger(0);
    private static final AtomicInteger totalConsumed = new AtomicInteger(0);
    private static final ConcurrentHashMap<Integer, AtomicInteger> consumerCounters = new ConcurrentHashMap<>();

    private static final Object LOCK = new Object();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(PRODUCER_COUNT + CONSUMER_COUNT);

        for (int i = 1; i <= CONSUMER_COUNT; i++) {
            consumerCounters.put(i, new AtomicInteger(0));
        }

        for (int i = 1; i <= PRODUCER_COUNT; i++) {
            executor.execute(new Proizvoditel(i));
        }

        for (int i = 1; i <= CONSUMER_COUNT; i++) {
            executor.execute(new Potrebitel(i));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("\n=== Все потоки завершены ===");
    }

    static class Proizvoditel implements Runnable {
        private final int id;
        private final Random random = new Random();
        private final int[] nechetnyeChisla = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29};

        public Proizvoditel(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (totalProduced.get() < TOTAL_OBJECTS) {
                    int chislo1 = nechetnyeChisla[random.nextInt(nechetnyeChisla.length)];
                    int chislo2 = nechetnyeChisla[random.nextInt(nechetnyeChisla.length)];

                    if (!produceItem(chislo1)) {
                        break;
                    }

                    if (!produceItem(chislo2)) {
                        break;
                    }

                    Thread.sleep(random.nextInt(100));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private boolean produceItem(int chislo) throws InterruptedException {
            synchronized (LOCK) {
                if (totalProduced.get() >= TOTAL_OBJECTS) {
                    return false;
                }

                while (sklad.size() >= BUFFER_CAPACITY) {
                    System.out.println("Производитель №" + id + " ждет. Склад полон!");
                    LOCK.wait();
                }

                sklad.offer(chislo);
                int produced = totalProduced.incrementAndGet();

                // Проверяем, не превысили ли лимит
                if (produced > TOTAL_OBJECTS) {
                    sklad.poll();
                    totalProduced.decrementAndGet();
                    return false;
                }

                System.out.println("Производитель №" + id + " поместил на склад: " + chislo);
                pokazatSklad();

                LOCK.notifyAll();

                return true;
            }
        }
    }

    static class Potrebitel implements Runnable {
        private final int id;
        private final Random random = new Random();

        public Potrebitel(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (consumerCounters.get(id).get() < CONSUMER_GOAL) {
                    synchronized (LOCK) {
                        // Проверяем, есть ли товары
                        while (sklad.isEmpty()) {
                            System.out.println("Потребитель №" + id + " ждет. Склад пуст!");
                            LOCK.wait();
                        }

                        // Берем товар
                        Integer tovar = sklad.poll();
                        if (tovar != null) {
                            consumerCounters.get(id).incrementAndGet();
                            totalConsumed.incrementAndGet();

                            System.out.println("Потребитель №" + id + " взял со склада: " + tovar);
                            pokazatSklad();

                            LOCK.notifyAll();
                        }
                    }

                    Thread.sleep(random.nextInt(100));
                }

                synchronized (LOCK) {
                    System.out.println(" Потребитель №" + id + " завершил работу (взял " + CONSUMER_GOAL + " товара)");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Вызывается только внутри synchronized(LOCK)
    private static void pokazatSklad() {
        if (sklad.size() != 0) {
            System.out.print("На складе имеется " + sklad.size() + " единиц -> ");
            List<Integer> skladCopy = new ArrayList<>(sklad);
            for (int tovar : skladCopy) {
                System.out.print(tovar + " ");
            }
            System.out.println();
        } else {
            System.out.println("Склад пуст");
        }
    }
}