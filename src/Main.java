import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
            for (int i = 0; i < goal; i++) {        // Цикл: goal раз забрать по одному элементу
                int item = store.take(id);          // Берём элемент со склада (может блокироваться, если склад пуст)
                totalConsumed.incrementAndGet();    // Увеличиваем глобальный счётчик всех потреблённых объектов
                Thread.sleep(300);
            }
            System.out.println("Потребитель " + id + " получил свои " + goal + " объектов");

        } catch (InterruptedException e) {
        }
    }
}

class Producer implements Runnable {
    private final int id;
    private final Store store;
    private final AtomicInteger totalProduced;
    private final int totalNeeded;
    private static final int F = 2;
    private final Random random = new Random();

    public Producer(int id, Store store, AtomicInteger totalProduced, int totalNeeded) {
        this.id = id;
        this.store = store;
        this.totalProduced = totalProduced;
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
            while (totalProduced.get() < totalNeeded) {
                for (int i = 0; i < F; i++) {             //  произвести до F элементов за одну "партию"
                    if (totalProduced.get() >= totalNeeded) // вдруг пока крутились, уже набрали нужное количество
                        break;
                    int item = generateOdd();
                    store.put(item, id);                   // кладём элемент на склад
                    totalProduced.incrementAndGet();       // атомарно увеличиваем глобальный счётчик
                }
            }
            System.out.println("Производитель " + id + " закончил работу");
        } catch (InterruptedException e) {
        }
    }
}

class Store {
    private final BlockingQueue<Integer> buffer;
    public Store(int capacity) {
        this.buffer = new ArrayBlockingQueue<>(capacity);
    }

    public void put(int item, int producerId) throws InterruptedException {
        if (buffer.remainingCapacity() == 0) {
            System.out.println("Склад заполнен, производитель " + producerId + " ждёт");
        }
        buffer.put(item);                            // ставим элемент в очеред, эсли нет места — поток блокируется..
        System.out.println("Производитель " + producerId + " произвел " + item +
                " склад: " + buffer.size());
    }

    public int take(int consumerId) throws InterruptedException {
        if (buffer.isEmpty()) {
            System.out.println("Склад пуст, потребитель " + consumerId + " ждёт");
        }
        int item = buffer.take();                     // кушаем элемент, эсли пусто — поток блокируется, пока не появится что-то
        System.out.println("Потребитель " + consumerId + " скушал " + item +
                " склад: " + buffer.size());
        return item;
    }
}

public class Main {
    public static void main(String[] args) {

        int X = 3; // производители                   // Количество производителей
        int Y = 4; // потребители                     // Количество потребителей
        int Z = 2; // каждому нужно                   // Сколько объектов нужно каждому потребителю
        int D = 5; // склад                           // Вместимость склада (размер буфера)

        int TOTAL_OBJECTS = Y * Z;                   // общее количество объектов, которое нужно произвести

        Store store = new Store(D);

        AtomicInteger totalProduced = new AtomicInteger(0); // Глобальный атомарный счётчик произведённых объектов
        AtomicInteger totalConsumed = new AtomicInteger(0); // Глобальный атомарный счётчик потреблённых объектов

        ExecutorService executor = Executors.newFixedThreadPool(X + Y);

        for (int i = 1; i <= X; i++) {
            executor.execute(new Producer(i, store, totalProduced, TOTAL_OBJECTS));
        }

        for (int i = 1; i <= Y; i++) {
            executor.execute(new Consumer(i, store, Z, totalConsumed));
        }
        executor.shutdown();

        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }

        System.out.println("Всего произведено: " + totalProduced.get());
        System.out.println("Всего скушано: " + totalConsumed.get());
    }
}
