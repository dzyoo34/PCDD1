public class Main {

    public static void main(String[] args) {
        runProgram();
    }

    private static void runProgram() {
        Runnable dummyTask = () -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        };
        ThreadGroup mainGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup G2 = new ThreadGroup("G2");
        ThreadGroup G4 = new ThreadGroup(G2, "G4");
        ThreadGroup G1 = new ThreadGroup(G4, "G1");
        ThreadGroup G3 = new ThreadGroup("G3");

        Thread Tha = new Thread(G1, dummyTask, "Tha"); Tha.setPriority(1); Tha.start();
        Thread Thb = new Thread(G1, dummyTask, "Thb"); Thb.setPriority(3); Thb.start();
        Thread Thc = new Thread(G1, dummyTask, "Thc"); Thc.setPriority(8); Thc.start();
        Thread Thd = new Thread(G1, dummyTask, "Thd"); Thd.setPriority(3); Thd.start();


        Thread ThA = new Thread(G4, dummyTask, "ThA"); ThA.setPriority(1); ThA.start();


        Thread Th1_main = new Thread(dummyTask, "Th1_main"); Th1_main.setPriority(3); Th1_main.start();
        Thread Th2_main = new Thread(dummyTask, "Th2_main"); Th2_main.setPriority(6); Th2_main.start();

        Thread Th1_G3 = new Thread(G3, dummyTask, "Th1_G3"); Th1_G3.setPriority(4); Th1_G3.start();
        Thread Th2_G3 = new Thread(G3, dummyTask, "Th2_G3"); Th2_G3.setPriority(3); Th2_G3.start();
        Thread Th3_G3 = new Thread(G3, dummyTask, "Th3_G3"); Th3_G3.setPriority(5); Th3_G3.start();

        System.out.println("Группы и приоритеты");

        String structure =
                "main:\n" +
                        "  Th: Th1_main, pri=3\n" +
                        "  Th: Th2_main, pri=6\n" +
                        "  G2:\n" +
                        "    G4:\n" +
                        "      G1:\n" +
                        "        Th: Tha, pri=1\n" +
                        "        Th: Thc, pri=8\n" +
                        "      Th: ThA, pri=1\n" +
                        "  G3:\n" +
                        "    Th: Th1_G3, pri=4\n" +
                        "    Th: Th3_G3, pri=5\n";

        System.out.println(structure);

        try {
            Tha.join(10); Thb.join(10); Thc.join(10); Thd.join(10); ThA.join(10);
            Th1_G3.join(10); Th2_G3.join(10); Th3_G3.join(10);
            Th1_main.join(10); Th2_main.join(10);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}