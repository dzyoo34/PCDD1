public class Main {

    static int[] numbers = new int[50];


    static final Object lock1 = new Object();
    static boolean firstDone = false;


    static final Object lock2 = new Object();
    static boolean thirdDone = false;
    static boolean fourthDone = false;

    static final Object printLock = new Object();

    public static void main(String[] args) {


        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = (int) (Math.random() * 100);
        }

        System.out.println("Массив чисел:");
        for (int n : numbers) System.out.print(n + " ");
        System.out.println("\n");


        Thread t1 = new Thread(new SumEvenPositionsForward());
        Thread t2 = new Thread(new SumEvenPositionsBackward());
        Thread t3 = new Thread(new ForwardInterval());
        Thread t4 = new Thread(new ReverseInterval());

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }


    static class SumEvenPositionsForward implements Runnable {
        @Override
        public void run() {
            System.out.println("Поток 1: Сумма позиций чётных чисел по два");

            int i = 0;
            while (i < numbers.length - 1) {

                if (numbers[i] % 2 == 0) {
                    int pos1 = i;
                    int firstEven = numbers[i];
                    i++;


                    while (i < numbers.length && numbers[i] % 2 != 0) {
                        i++;
                    }

                    if (i < numbers.length) {
                        int pos2 = i;
                        int secondEven = numbers[i];
                        int sumPositions = pos1 + pos2;
                        System.out.println("Поток 1: числа[" + pos1 + "]=" + firstEven +
                                " + числа[" + pos2 + "]=" + secondEven +
                                " => позиции: " + pos1 + "+" + pos2 + "=" + sumPositions);
                    }
                }
                i++;
            }
            synchronized (lock1) {
                firstDone = true;
                lock1.notify();
            }

            printText("Фамилия: Stepanovici", 100);
        }
    }

    static class SumEvenPositionsBackward implements Runnable {
        @Override
        public void run() {
            synchronized (lock1) {
                while (!firstDone) {
                    try {
                        lock1.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("\nПоток 2: Сумма позиций чётных чисел по два с конца");

            int i = numbers.length - 1;
            while (i > 0) {
                if (numbers[i] % 2 == 0) {
                    int pos1 = i;
                    int firstEven = numbers[i];
                    i--;

                    while (i >= 0 && numbers[i] % 2 != 0) {
                        i--;
                    }

                    if (i >= 0) {
                        int pos2 = i;
                        int secondEven = numbers[i];
                        int sumPositions = pos1 + pos2;
                        System.out.println("Поток 2: числа[" + pos1 + "]=" + firstEven +
                                " + числа[" + pos2 + "]=" + secondEven +
                                " => позиции: " + pos1 + "+" + pos2 + "=" + sumPositions);
                    }
                }
                i--;
            }

            printText("\nИмя: Nichita", 100);
        }
    }

    static class ForwardInterval implements Runnable {
        @Override
        public void run() {
            System.out.println("\nПоток 3: Интервал [120, 690]");
            for (int i = 120; i <= 690; i += 10) {
                System.out.print(i + " ");
                if ((i - 120) % 100 == 90) System.out.println();
            }
            System.out.println();

            synchronized (lock2) {
                thirdDone = true;
                lock2.notifyAll();

                while (!fourthDone) {
                    try {
                        lock2.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            printText("\nДисциплина: Конкурентное программирование", 100);
        }
    }

    static class ReverseInterval implements Runnable {
        @Override
        public void run() {
            synchronized (lock2) {
                while (!thirdDone) {
                    try {
                        lock2.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("\nПоток 4: Интервал [1000, 1567] ");
            for (int i = 1567; i >= 1000; i -= 10) {
                System.out.print(i + " ");
                if ((1567 - i) % 100 == 90) System.out.println();
            }
            System.out.println();

            synchronized (lock2) {
                fourthDone = true;
                lock2.notifyAll();
            }

            printText("\nГруппа: Cr-233", 100);
        }
    }

    static void printText(String text, int delayMillis) {
        synchronized (printLock) {
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
}