class Custom_thread extends Thread {
    Custom_thread(ThreadGroup group, String name, int priority) {
        super(group, name);
        setPriority(priority);
        setDaemon(true);
    }

    public void run() {
        System.out.println(
                "Thread '" + getName() +
                        "' from Group '" + getThreadGroup().getName() +
                        "' started with priority " + getPriority()
        );
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ThreadGroup main = new ThreadGroup("Main");
        ThreadGroup g2 = new ThreadGroup(main, "G2");
        ThreadGroup g4 = new ThreadGroup(g2, "G4");
        ThreadGroup g1 = new ThreadGroup(g4, "G1");
        ThreadGroup g3 = new ThreadGroup(main, "G3");

        /* G1 group */
        Thread Tha = new Custom_thread(g1, "Tha", 1);
        Thread Thb = new Custom_thread(g1, "Thb", 3);
        Thread Thc = new Custom_thread(g1, "Thc", 8);
        Thread Thd = new Custom_thread(g1, "Thd", 3);

        /* G4 group */
        Thread ThA = new Custom_thread(g4, "ThA", 1);

        /* G3 group */
        Thread Th1_g3 = new Custom_thread(g3, "Th1", 4);
        Thread Th2_g3 = new Custom_thread(g3, "Th2", 3);
        Thread Th3_g3 = new Custom_thread(g3, "Th3", 5);

        /* Main group */
        Thread Th1 = new Custom_thread(main, "Th1", 3);
        Thread Th2 = new Custom_thread(main, "Th2", 6);

        Tha.start();
        Thb.start();
        Thc.start();
        Thd.start();
        ThA.start();
        Th1_g3.start();
        Th2_g3.start();
        Th3_g3.start();
        Th1.start();
        Th2.start();


        main.list();
        System.out.println("Total active threads is " + main.activeCount());
    }
}