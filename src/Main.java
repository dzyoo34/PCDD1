import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// ---------------- CONSUMER ----------------
class Consumer implements Runnable {
    private final int id;
    private final Store store;
    private final int goal;
    private final AtomicInteger totalConsumed;

    public Consumer(int id, Store store, int goal, AtomicInteger totalConsumed) {
        this.id = id;
        this.store = store;
        this.goal = goal;
        this.totalConsumed = totalConsumed;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < goal; i++) {
                int item = store.take(id);
                totalConsumed.incrementAndGet();
                Thread.sleep(300);
            }
            System.out.println("Потребитель " + id + " получил свои " + goal + " объектов");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// ---------------- PRODUCER ----------------
class Producer implements Runnable {
    private final int id;
    private final Store store;
    private final AtomicInteger totalConsumed;
    private final int totalNeeded;    // Y * Z
    private static final int F = 2;
    private final Random random = new Random();

    public Producer(int id, Store store, AtomicInteger totalConsumed, int totalNeeded) {
        this.id = id;
        this.store = store;
        this.totalConsumed = totalConsumed;
        this.totalNeeded = totalNeeded;
    }

    private int generateOdd() {
        int n = random.nextInt(100);
        if (n % 2 == 0) n++;
        return n;
    }

    @Override
    public void run() {
        try {
            while (true) {

                // Если все потребители уже наелись — завершаем производителя
                if (totalConsumed.get() >= totalNeeded) {
                    System.out.println("Производитель " + id + " закончил работу");
                    break;
                }

                for (int i = 0; i < F; i++) {

                    // Проверяем снова перед каждой единицей продукции
                    if (totalConsumed.get() >= totalNeeded) {
                        System.out.println("Производитель " + id + " остановился (больше не нужно)");
                        return;
                    }

                    int item = generateOdd();
                    store.put(item, id);
                }

                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// ---------------- STORE ----------------
class Store {
    private final BlockingQueue<Integer> buffer;

    public Store(int capacity) {
        this.buffer = new ArrayBlockingQueue<>(capacity);
    }

    public void put(int item, int producerId) throws InterruptedException {
        if (buffer.remainingCapacity() == 0) {
            System.out.println("Склад заполнен, производитель " + producerId + " ждёт");
        }
        buffer.put(item);
        System.out.println("Производитель " + producerId + " произвел " + item +
                " | склад: " + buffer.size());
    }

    public int take(int consumerId) throws InterruptedException {
        if (buffer.isEmpty()) {
            System.out.println("Склад пуст, потребитель " + consumerId + " ждёт");
        }
        int item = buffer.take();
        System.out.println("Потребитель " + consumerId + " скушал " + item +
                " | склад: " + buffer.size());
        return item;
    }
}

// ---------------- MAIN ----------------
public class Main {
    public static void main(String[] args) {

        int X = 3; // производители
        int Y = 4; // потребители
        int Z = 2; // каждому нужно
        int D = 5; // размер склада

        int totalNeeded = Y * Z; // сколько нужно всего потребителям

        Store store = new Store(D);

        AtomicInteger totalConsumed = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(X + Y);

        // Производители
        for (int i = 1; i <= X; i++) {
            executor.execute(new Producer(i, store, totalConsumed, totalNeeded));
        }

        // Потребители
        for (int i = 1; i <= Y; i++) {
            executor.execute(new Consumer(i, store, Z, totalConsumed));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("Всего скушано: " + totalConsumed.get());
    }
}
