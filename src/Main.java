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

        ThreadGroup mainGroup = Thread.currentThread().getThreadGroup();


        ThreadGroup G2 = new ThreadGroup("G2");
        ThreadGroup G3 = new ThreadGroup("G3");

        ThreadGroup G4 = new ThreadGroup(G2, "G4");

        ThreadGroup G1 = new ThreadGroup(G4, "G1");


        System.out.println("Создание потоков...");


        Thread Tha = new Thread(G1, dummyTask, "Tha"); Tha.setPriority(1); Tha.start();
        Thread Thb = new Thread(G1, dummyTask, "Thb"); Thb.setPriority(3); Thb.start();
        Thread Thc = new Thread(G1, dummyTask, "Thc"); Thc.setPriority(8); Thc.start();
        Thread Thd = new Thread(G1, dummyTask, "Thd"); Thd.setPriority(3); Thd.start();


        Thread ThA = new Thread(G4, dummyTask, "ThA"); ThA.setPriority(1); ThA.start();

        Thread Th1_G3 = new Thread(G3, dummyTask, "Th1_G3"); Th1_G3.setPriority(4); Th1_G3.start();
        Thread Th2_G3 = new Thread(G3, dummyTask, "Th2_G3"); Th2_G3.setPriority(3); Th2_G3.start();
        Thread Th3_G3 = new Thread(G3, dummyTask, "Th3_G3"); Th3_G3.setPriority(5); Th3_G3.start();

        Thread Th1_main = new Thread(dummyTask, "Th1_main"); Th1_main.setPriority(3); Th1_main.start();
        Thread Th2_main = new Thread(dummyTask, "Th2_main"); Th2_main.setPriority(6); Th2_main.start();


        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}


        System.out.println("\n=== ПЕРЕЧИСЛЕНИЕ ВСЕЙ СТРУКТУРЫ ПОТОКОВ ===");

        ThreadGroup rootGroup = mainGroup.getParent();
        if (rootGroup == null) rootGroup = mainGroup;

        listGroup(rootGroup, "");


        try {
            Thread[] allThreads = {Tha, Thb, Thc, Thd, ThA, Th1_G3, Th2_G3, Th3_G3, Th1_main, Th2_main};
            for (Thread t : allThreads) {
                t.join(10);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static void listGroup(ThreadGroup g, String indent) {

        System.out.println(indent + "GROUP: " + g.getName() +
                " [Max Pri: " + g.getMaxPriority() + "]");

        int numThreads = g.activeCount();
        Thread[] threads = new Thread[numThreads * 2];
        numThreads = g.enumerate(threads, false);

        int numGroups = g.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[numGroups * 2];
        numGroups = g.enumerate(groups, false);

        String threadIndent = indent + "  ";
        String groupIndent = indent + "  ";

        for (int i = 0; i < numThreads; i++) {
            Thread t = threads[i];

            if (t != null && !t.isDaemon()) {
                System.out.println(threadIndent +
                        "Th: " + t.getName() +
                        ", pri=" + t.getPriority() +
                        ", state=" + t.getState());
            }
        }

        // Рекурсивный вызов для дочерних групп
        for (int i = 0; i < numGroups; i++) {
            if (groups[i] != null) {
                listGroup(groups[i], groupIndent);
            }
        }
    }
}