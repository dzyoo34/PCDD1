import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Store{
    ArrayList<Integer> stockList = new ArrayList<>();

    ReentrantLock lock = new ReentrantLock();
    Condition notFull = lock.newCondition();
    Condition notEmpty = lock.newCondition();

    private volatile int consumeralive = 0;

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
    public void put(String str, int... values) {
        lock.lock();
        try {
            for (int v : values) {

                while (stockList.size() == 5) {
                    notFull.await();
                }

                if (consumeralive == 0) return;

                stockList.add(v);
                System.out.println(str + " поместил: " + v + " -> " + stockList);

                if (stockList.size() == 5) {
                    System.out.println(">>> Склад заполнен!");
                    notEmpty.signalAll();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


    public void get(String str) {
        lock.lock();
        try {
            while (stockList.size() < 5 && consumeralive > 0) {
                notEmpty.await();
            }

            if (consumeralive == 0 && stockList.isEmpty()) return;

            while (!stockList.isEmpty()) {
                int val = stockList.remove(stockList.size() - 1);
                System.out.println(str + " взял число " + val);
            }

            System.out.println("<<< Склад пуст!");
            notFull.signalAll();

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

        while (store.getConsumeralive() > 0) {
            int iterations = 1+(int)(Math.random() * 5);
            for (int i = 0; i < iterations; i++) {
                int count = 1 + (int)(Math.random() * 5); // 1..5 чисел
                int[] arr = new int[count];

                for (int j = 0; j < count; j++) {
                    arr[j] = odd[(int) (Math.random() * odd.length)];
                }

                store.put(Thread.currentThread().getName(), arr);
            }
        }
    }
}
class Consumer implements Runnable{
    Store store;
    public Consumer(Store store){
        this.store = store;
    }
    public void run(){
        store.consumer_started();
        for (int i = 0; i < 45; i++) {
            store.get(Thread.currentThread().getName());
        }
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!"+Thread.currentThread().getName() + " Наелся и умер");
        store.consumer_finished();
    }
}

public class Main {
    public static void main(String[] args) {
        Store store = new Store();
        ExecutorService producers = Executors.newFixedThreadPool(3);
        ExecutorService consumers = Executors.newFixedThreadPool(4);

        consumers.execute(new Consumer(store));
        consumers.execute(new Consumer(store));
        consumers.execute(new Consumer(store));
        consumers.execute(new Consumer(store));

        producers.execute(new Producer(store));
        producers.execute(new Producer(store));
        producers.execute(new Producer(store));

        producers.shutdown();
        consumers.shutdown();
    }
}