public class Main {

    static int[] numbers = new int[50];

    // Синхронизация для потоков 1-2
    static final Object lock1 = new Object();
    static boolean firstDone = false;
    static boolean secondDone = false;

    // Синхронизация для потоков 3-4
    static final Object lock2 = new Object();
    static boolean thirdDone = false;
    static boolean fourthDone = false;

    // Синхронизация для вывода текста
    static final Object printLock = new Object();

    // Счётчик завершённых задач
    static int tasksCompleted = 0;

    public static void main(String[] args) {

        // 1. Заполняем массив
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = (int) (Math.random() * 100);
        }

        System.out.println("Массив чисел:");
        for (int n : numbers) System.out.print(n + " ");
        System.out.println("\n");

        // 2. Создаём и ЗАПУСКАЕМ потоки
        Thread t1 = new Thread(new SumEvenPositionsForward());
        Thread t2 = new Thread(new SumEvenPositionsBackward());
        Thread t3 = new Thread(new ForwardInterval());
        Thread t4 = new Thread(new ReverseInterval());

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }

    // Поток 1: сумма позиций чётных чисел ПО ДВА с начала
    static class SumEvenPositionsForward implements Runnable {
        @Override
        public void run() {
            synchronized (printLock) {
                System.out.println("Поток 1: Сумма позиций чётных чисел ПО ДВА (с начала)");

                int i = 0;
                while (i < numbers.length - 1) {
                    // Ищем первое чётное число
                    if (numbers[i] % 2 == 0) {
                        int pos1 = i;
                        int firstEven = numbers[i];
                        i++;

                        // Ищем второе чётное число
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
                System.out.println();
            }

            // Метод 1: synchronized + notify
            synchronized (lock1) {
                firstDone = true;
                lock1.notify();
            }

            // Ждём завершения всех задач
            synchronized (printLock) {
                tasksCompleted++;
                while (tasksCompleted < 4) {
                    try {
                        printLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printLock.notifyAll();
            }

            printText("Фамилия: Stepanovici", 100);
        }
    }

    // Поток 2: сумма позиций чётных чисел ПО ДВА с конца
    static class SumEvenPositionsBackward implements Runnable {
        @Override
        public void run() {
            // Метод 1: synchronized + wait
            synchronized (lock1) {
                while (!firstDone) {
                    try {
                        lock1.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (printLock) {
                System.out.println("Поток 2: Сумма позиций чётных чисел ПО ДВА (с конца)");

                int i = numbers.length - 1;
                while (i > 0) {
                    // Ищем первое чётное число с конца
                    if (numbers[i] % 2 == 0) {
                        int pos1 = i;
                        int firstEven = numbers[i];
                        i--;

                        // Ищем второе чётное число
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
                System.out.println();
            }

            // Сигнализируем, что поток 2 закончил
            synchronized (lock1) {
                secondDone = true;
                lock1.notifyAll();
            }

            // Ждём завершения всех задач
            synchronized (printLock) {
                tasksCompleted++;
                while (tasksCompleted < 4) {
                    try {
                        printLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printLock.notifyAll();
            }

            printText("Имя: Nichita", 100);
        }
    }

    // Поток 3: интервал [120, 690]
    static class ForwardInterval implements Runnable {
        @Override
        public void run() {
            // Ждём завершения потока 2
            synchronized (lock1) {
                while (!secondDone) {
                    try {
                        lock1.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (printLock) {
                System.out.println("Поток 3: Интервал [120, 690]");
                for (int i = 120; i <= 690; i += 10) {
                    System.out.print(i + " ");
                    if ((i - 120) % 100 == 90) System.out.println();
                }
                System.out.println();
            }

            // Метод 2: synchronized + notifyAll + двусторонняя синхронизация
            synchronized (lock2) {
                thirdDone = true;
                lock2.notifyAll();

                // Ждём пока поток 4 закончит свою работу
                while (!fourthDone) {
                    try {
                        lock2.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Ждём завершения всех задач
            synchronized (printLock) {
                tasksCompleted++;
                while (tasksCompleted < 4) {
                    try {
                        printLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printLock.notifyAll();
            }

            printText("Дисциплина: Конкурентное программирование", 100);
        }
    }

    // Поток 4: интервал [1000, 1567] с конца
    static class ReverseInterval implements Runnable {
        @Override
        public void run() {
            // Метод 2: synchronized + wait
            synchronized (lock2) {
                while (!thirdDone) {
                    try {
                        lock2.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (printLock) {
                System.out.println("Поток 4: Интервал [1000, 1567] (обратно)");
                for (int i = 1567; i >= 1000; i -= 10) {
                    System.out.print(i + " ");
                    if ((1567 - i) % 100 == 90) System.out.println();
                }
                System.out.println();
            }

            // Метод 2: synchronized + notifyAll (сигнализируем потоку 3)
            synchronized (lock2) {
                fourthDone = true;
                lock2.notifyAll();
            }

            // Ждём завершения всех задач
            synchronized (printLock) {
                tasksCompleted++;
                while (tasksCompleted < 4) {
                    try {
                        printLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printLock.notifyAll();
            }

            printText("Группа: Cr-233", 100);
        }
    }

    // Плавный вывод текста (синхронизированный)
    static void printText(String text, int delayMillis) {
        synchronized (printLock) {
            long delay = delayMillis * 1_000_000L;
            for (char c : text.toCharArray()) {
                System.out.print(c);
                long start = System.nanoTime();
                while (System.nanoTime() - start < delay) {
                    // активное ожидание
                }
            }
            System.out.println();
        }
    }
}