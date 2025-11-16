import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

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
        if (totalProduced.get() >= TOTAL_OBJECTS) {
            break;
                    }

        if (sklad.remainingCapacity() == 0) {
        System.out.println("Производитель №" + id + " ждет. Склад полон!");
                    }
               sklad.put(chislo1);
                 int produced1 = totalProduced.incrementAndGet();

                  if (produced1 > TOTAL_OBJECTS) {
                        sklad.take();
                        totalProduced.decrementAndGet();
                        break;
                    }

              System.out.println("Производитель №" + id + " поместил на склад: " + chislo1);
             pokazatSklad();

            if (totalProduced.get() >= TOTAL_OBJECTS) {
             break;
                    }

             if (sklad.remainingCapacity() == 0) {
              System.out.println("Производитель №" + id + " ждет. Склад полон!");
                    }
                    sklad.put(chislo2);
                    int produced2 = totalProduced.incrementAndGet();

                if (produced2 > TOTAL_OBJECTS) {
              sklad.take();
          totalProduced.decrementAndGet();
              break;
                    }
            System.out.println("Производитель №" + id + " поместил на склад: " + chislo2);
           pokazatSklad();

              Thread.sleep(random.nextInt(100));

                if (produced2 >= TOTAL_OBJECTS) {
                        break;
                    }
                }
             } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
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
                    if (sklad.isEmpty()) {
                        System.out.println("Потребитель №" + id + " ждет. Склад пуст!");
                    }

                    int tovar = sklad.take();

                    consumerCounters.get(id).incrementAndGet();
                    totalConsumed.incrementAndGet();

                    System.out.println("Потребитель №" + id + " взял со склада: " + tovar);
                    pokazatSklad();

                    Thread.sleep(random.nextInt(100));
                }

                System.out.println("Потребитель №" + id + " взял " + CONSUMER_GOAL + " числа. Поток завершен");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static synchronized void pokazatSklad() {
        if (sklad.size() != 0) {
            System.out.print("На складе имеется " + sklad.size() + " единиц -> ");
            for (int tovar : sklad) {
                System.out.print(tovar + " ");
            }
            System.out.println();
        } else {
            System.out.println("Склад пуст");
        }
    }
}