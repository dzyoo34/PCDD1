import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class Store{
    ArrayList<Integer> stockList = new ArrayList<>();

    ReentrantLock lock = new ReentrantLock();
    Condition notFull = lock.newCondition();
    AtomicInteger[] eaten = new AtomicInteger[] {
            new AtomicInteger(0),
            new AtomicInteger(0),
            new AtomicInteger(0),
            new AtomicInteger(0)
    };
    Condition notEmpty = lock.newCondition();

    private volatile int consumeralive = 0;
    volatile int totalProduced = 0;
    private final int TARGET = 45;

    public void consumer_started(){
        lock.lock();
        try {
            consumeralive++;
        } finally { lock.unlock(); }
    }

    public void consumer_finished(){
        lock.lock();
        try {
            consumeralive--;
            if (consumeralive == 0) {
                notEmpty.signalAll();
                notFull.signalAll();
            }
        } finally { lock.unlock(); }
    }

    public int getConsumeralive(){
        return consumeralive;
    }

    public boolean isTargetReached(){
        return totalProduced >= TARGET;
    }

    public void put(String str, int... values) {
        lock.lock();
        try {
            for (int v : values) {
                if (totalProduced >= TARGET) {
                    notEmpty.signalAll();
                    return;
                }

                while (stockList.size() == 5) {
                    notFull.await();
                }

                if (consumeralive == 0) return;

                stockList.add(v);
                totalProduced++;
                System.out.println(str + " поместил: " + v + " -> " + stockList + " (всего: " + totalProduced + ")");

                if (stockList.size() == 5) {
                    System.out.println(">>> Склад заполнен!");
                }

                notEmpty.signalAll();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void get(String str, int consumerIndex) {
        lock.lock();
        try {
            while (stockList.isEmpty() && consumeralive > 0 && totalProduced < TARGET) {
                notEmpty.await();
            }

            if (consumeralive == 0 && stockList.isEmpty()) return;
            if (stockList.isEmpty() && totalProduced >= TARGET) return;

            // Берём случайное количество элементов (от 1 до всех доступных)
            int available = stockList.size();
            int toTake = 1 + (int)(Math.random() * available);

            int consumed = 0;
            for (int i = 0; i < toTake && !stockList.isEmpty(); i++) {
                int val = stockList.remove(stockList.size() - 1);
                consumed++;
                eaten[consumerIndex].incrementAndGet();
                System.out.println(str + " взял число " + val);
            }

            System.out.println("--- " + str + " взял " + consumed + " элементов (осталось на складе: " + stockList.size() + ")");

            if (stockList.isEmpty()) {
                System.out.println("<<< Склад пуст!");
            }

            notFull.signalAll();
            notEmpty.signalAll();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

class Producer implements Runnable{
    Store store;
    public Producer(Store store){
        this.store = store;
    }

    public void run(){
        int[] odd = new int[]{1,3,5,7,9,11,13,15,17,19};

        while (store.getConsumeralive() > 0 && !store.isTargetReached()) {
            int count = 1 + (int)(Math.random() * 5);
            int[] arr = new int[count];

            for (int j = 0; j < count; j++) {
                arr[j] = odd[(int) (Math.random() * odd.length)];
            }

            store.put(Thread.currentThread().getName(), arr);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + " завершил работу");
    }
}

class Consumer implements Runnable{
    Store store;
    int consumerIndex;

    public Consumer(Store store, int consumerIndex){
        this.store = store;
        this.consumerIndex = consumerIndex;
    }

    public void run(){
        store.consumer_started();

        while (store.getConsumeralive() > 0) {
            store.get(Thread.currentThread().getName(), consumerIndex);

            if (store.isTargetReached() && store.stockList.isEmpty()) {
                break;
            }

            try {
                Thread.sleep(100); // Небольшая задержка
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(">>> " + Thread.currentThread().getName() + " завершил работу");
        store.consumer_finished();
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Store store = new Store();
        ExecutorService producers = Executors.newFixedThreadPool(3);
        ExecutorService consumers = Executors.newFixedThreadPool(4);

        consumers.execute(new Consumer(store, 0));
        consumers.execute(new Consumer(store, 1));
        consumers.execute(new Consumer(store, 2));
        consumers.execute(new Consumer(store, 3));

        producers.execute(new Producer(store));
        producers.execute(new Producer(store));
        producers.execute(new Producer(store));

        consumers.shutdown();
        producers.shutdown();

        while (!consumers.isTerminated() || !producers.isTerminated()) {
            Thread.sleep(100);
        }

        System.out.println("Произведено элементов: " + store.totalProduced);
        System.out.println();
        int total = 0;
        for (int i = 0; i < store.eaten.length; i++) {
            int count = store.eaten[i].get();
            System.out.println("Consumer " + (i+1) + " съел: " + count);
            total += count;
        }
        System.out.println();
        System.out.println("Всего съедено: " + total + " ");
    }
}