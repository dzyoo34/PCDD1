

public class Main {

    static int[] numbers = new int[50];
    static final Object lock1 = new Object();
    static final Object lock2 = new Object();
    static boolean firstDone = false;
    static boolean thirdDone = false;

    public static void main(String[] args) {

        // 1. Заполняем массив
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = (int) (Math.random() * 100);
        }

        System.out.println("Массив чисел:");
        for (int n : numbers) System.out.print(n + " ");
        System.out.println("\n");

        // 2. Создаём потоки
        SumEvenPositionsForward t1 = new SumEvenPositionsForward();
        SumEvenPositionsBackward t2 = new SumEvenPositionsBackward();
        ForwardInterval t3 = new ForwardInterval();
        ReverseInterval t4 = new ReverseInterval();

        // 3. Запускаем их как обычные методы
        t1.run();
        t2.run();
        t3.run();
        t4.run();
    }

    // Поток 1: сумма позиций чётных чисел с начала
    static class SumEvenPositionsForward implements Runnable {
        @Override
        public void run() {
            int sum = 0;
            System.out.println("Поток 1: Сумма позиций чётных чисел (вперёд)");
            for (int i = 0; i < numbers.length; i++) {
                if (numbers[i] % 2 == 0) {
                    sum += i;
                }
            }
            System.out.println("Результат потока 1: " + sum);

            synchronized (lock1) {
                firstDone = true;
                lock1.notify(); // начало 2 потока
            }

            printText("Фамилия: Stepanovici", 100);
        }
    }

    // Поток 2: сумма позиций чётных чисел с конца
    static class SumEvenPositionsBackward implements Runnable {
        @Override
        public void run() {
            synchronized (lock1) {
                while (!firstDone) {
                    try {
                        lock1.wait();
                    } catch (InterruptedException ignored) {}
                }
            }

            int sum = 0;
            System.out.println("Поток 2: Сумма позиций чётных чисел (назад)");
            for (int i = numbers.length - 1; i >= 0; i--) {
                if (numbers[i] % 2 == 0) {
                    sum += i;
                }
            }
            System.out.println("Результат потока 2: " + sum);

            printText("Имя: Nichita", 100);
        }
    }

    // Поток 3 интервал [120, 690]
    static class ForwardInterval implements Runnable {
        @Override
        public void run() {
            System.out.println("Поток 3: Интервал [120, 690]");
            for (int i = 120; i <= 690; i += 100) {
                System.out.print(i + " ");
            }
            System.out.println();

            synchronized (lock2) {
                thirdDone = true;
                lock2.notify(); // разрешаем потоку 4
            }

            printText("Дисциплина: Конкурентное и распределённое программирование", 100);
        }
    }

    // Поток 4 интервал [1000, 1567] с конца
    static class ReverseInterval implements Runnable {
        @Override
        public void run() {
            synchronized (lock2) {
                while (!thirdDone) { // ждём поток 3
                    try {
                        lock2.wait();
                    } catch (InterruptedException ignored) {}
                }
            }

            System.out.println("Поток 4: Интервал [1000, 1567] (обратно)");
            for (int i = 1567; i >= 1000; i -= 100) {
                System.out.print(i + " ");
            }
            System.out.println();

            printText("Группа: Cr-233", 100);
        }
    }

    // Плавный вывод текста
    static void printText(String text, int delayMillis) {
        long delay = delayMillis * 1_000_000L;
        for (char c : text.toCharArray()) {
            System.out.print(c);
            long start = System.nanoTime();
            while (System.nanoTime() - start < delay) {

            }
        }
        System.out.println();
    }
}
