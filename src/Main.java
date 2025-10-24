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


        ThreadGroup G2 = new ThreadGroup("G2");
        ThreadGroup G4 = new ThreadGroup(G2, "G4");
        ThreadGroup G1 = new ThreadGroup(G4, "G1");

        System.out.println("Создание потоков...");

        Thread Thc = new Thread(G1, dummyTask, "Thc");
        Thc.setPriority(8);
        Thc.start();

        Thread Tha = new Thread(G1, dummyTask, "Tha"); Tha.setPriority(1); Tha.start();
        Thread Thb = new Thread(G1, dummyTask, "Thb"); Thb.setPriority(3); Thb.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}


        System.out.println("\n=== ПРИОРИТЕТЫ ДО ИЗМЕНЕНИЯ ГРУППЫ G1 ===");
        System.out.println("Группа G1 Max Priority: " + G1.getMaxPriority());
        System.out.println("Поток Thc (заданный 8) фактический: " + Thc.getPriority()); // Будет 8


        G1.setMaxPriority(5);


        System.out.println("\n=== ПРИОРИТЕТЫ ПОСЛЕ G1.setMaxPriority(5) ===");
        System.out.println("Группа G1 Max Priority: " + G1.getMaxPriority());
        System.out.println("Поток Thc (заданный 8) фактический: " + Thc.getPriority());


        try {
            Thc.join(10);
            Tha.join(10);
            Thb.join(10);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        System.out.println("\nПрограмма завершена.");
    }
}