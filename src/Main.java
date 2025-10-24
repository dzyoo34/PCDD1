import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        runProgram();
    }

    private static void runProgram() {
        Runnable dummyTask = () -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        };

        // 2. Создание иерархии групп потоков
        ThreadGroup mainGroup = Thread.currentThread().getThreadGroup();

        // Создаем новые группы. Если родитель не указан, родитель — группа текущего потока (mainGroup)
        ThreadGroup G2 = new ThreadGroup("G2");
        ThreadGroup G4 = new ThreadGroup(G2, "G4"); // G4 в G2
        ThreadGroup G1 = new ThreadGroup(G4, "G1"); // G1 в G4
        ThreadGroup G3 = new ThreadGroup("G3");

        System.out.println("Создание потоков...");

        // 3. Создание и запуск потоков (потоки автоматически добавляются в группу)
        Thread Tha = new Thread(G1, dummyTask, "Tha"); Tha.setPriority(1); Tha.start();
        Thread Thb = new Thread(G1, dummyTask, "Thb"); Thb.setPriority(3); Thb.start();
        Thread Thc = new Thread(G1, dummyTask, "Thc"); Thc.setPriority(8); Thc.start();
        Thread Thd = new Thread(G1, dummyTask, "Thd"); Thd.setPriority(3); Thd.start();

        Thread ThA = new Thread(G4, dummyTask, "ThA"); ThA.setPriority(1); ThA.start();

        // Потоки в основной группе (mainGroup)
        Thread Th1_main = new Thread(dummyTask, "Th1_main"); Th1_main.setPriority(3); Th1_main.start();
        Thread Th2_main = new Thread(dummyTask, "Th2_main"); Th2_main.setPriority(6); Th2_main.start();

        // Потоки в G3
        Thread Th1_G3 = new Thread(G3, dummyTask, "Th1_G3"); Th1_G3.setPriority(4); Th1_G3.start();
        Thread Th2_G3 = new Thread(G3, dummyTask, "Th2_G3"); Th2_G3.setPriority(3); Th2_G3.start();
        Thread Th3_G3 = new Thread(G3, dummyTask, "Th3_G3"); Th3_G3.setPriority(5); Th3_G3.start();

        // Дадим потокам немного времени на запуск
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}


        System.out.println("Группы и приоритеты ");

        // Выводим структуру, начиная с родительской группы (родитель mainGroup — это root-группа)
        ThreadGroup rootGroup = mainGroup.getParent();
        if (rootGroup == null) rootGroup = mainGroup; // Если root-группы нет (редко), начинаем с mainGroup

        listGroup(rootGroup, "");

        // 4. Демонстрация ограничения приоритета
        G1.setMaxPriority(5);
        System.out.println("Изменение максимального приоритета:");
        System.out.println("G1.setMaxPriority(5). Thc (pri 8) -> effective pri " + Thc.getPriority() + ".");

        // 5. Ожидание завершения потоков
        try {
            // Собираем все потоки для удобного join
            Thread[] allThreads = {Tha, Thb, Thc, Thd, ThA, Th1_main, Th2_main, Th1_G3, Th2_G3, Th3_G3};
            for (Thread t : allThreads) {
                t.join(10);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static void listGroup(ThreadGroup g, String indent) {

        // 1. Выводим информацию о текущей группе
        System.out.println(indent + "GROUP: " + g.getName() +
                " [Max Pri: " + g.getMaxPriority() + "]");

        // 2. Получаем список подгрупп и потоков
        int numThreads = g.activeCount();
        Thread[] threads = new Thread[numThreads * 2]; // Запас
        numThreads = g.enumerate(threads, false); // Получаем только потоки в этой группе

        int numGroups = g.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[numGroups * 2]; // Запас
        numGroups = g.enumerate(groups, false); // Получаем только подгруппы в этой группе

        String threadIndent = indent + "  ";
        String groupIndent = indent + "  ";

        // 3. Выводим потоки этой группы
        for (int i = 0; i < numThreads; i++) {
            Thread t = threads[i];
            // Исключаем системные потоки, если их не создавали явно
            if (t != null && !t.isDaemon()) {
                System.out.println(threadIndent +
                        "Th: " + t.getName() +
                        ", pri=" + t.getPriority() +
                        ", state=" + t.getState());
            }
        }

        // 4. Рекурсивно вызываем метод для подгрупп
        for (int i = 0; i < numGroups; i++) {
            if (groups[i] != null) {
                listGroup(groups[i], groupIndent);
            }
        }
    }
}