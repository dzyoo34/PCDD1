import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;




class Th1 implements Runnable {
    private final int[] mas;
    private final String name;

    public Th1(int[] mas, String name) {
        this.mas = mas;
        this.name = name;
    }

    public void run() {
        int pos1 = -1; 
        for (int i = 0; i < mas.length; i++) {
            
            if (mas[i] % 2 != 0) {
                if (pos1 == -1) {
                    pos1 = i;
                } else {
                    int pos2 = i;
                    int suma = pos1 + pos2;

                    System.out.printf("%s: %d, %d, %d, %d, %d %n",
                            name,
                            pos1, pos2,
                            suma,
                            mas[pos1], mas[pos2]);

                    pos1 = -1;
                }
            }
        }
        
    }
}


class Th2 implements Runnable {
    private final int[] mas;
    private final String name;

    public Th2(int[] mas, String name) {
        this.mas = mas;
        this.name = name;
    }

    public void run() {
        int pos1 = -1; 

        for (int i = mas.length - 1; i >= 0; i--) {
            
            if (mas[i] % 2 != 0) {
                if (pos1 == -1) {
                    pos1 = i;
                } else {
                    int pos2 = i;
                    int suma = pos1 + pos2;
                    
                    System.out.printf("%s: %d, %d, %d, %d, %d %n",
                            name,
                            pos2, pos1,
                            suma,
                            mas[pos2], mas[pos1]);

                    pos1 = -1;
                }
            }
        }
        
    }
}


public class Main {
    public static void main(String[] args) {
        int[] mas = new int[100];

        System.out.println("Массив данных:");
        for(int i = 0; i < 100; i++) {
            mas[i] = (int)(Math.random() * 100) + 1;
            System.out.printf("%3d ", mas[i]);

            if((i + 1) % 20 == 0) {
                System.out.println();
            }
        }
        System.out.println("\n");
        
        Th1 runnable1 = new Th1(mas, "Поток-1");
        Th2 runnable2 = new Th2(mas, "Поток-2");

        Thread thread1 = new Thread(runnable1);
        Thread thread2 = new Thread(runnable2);

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch(InterruptedException e) {
            System.out.println("Ошибка при ожидании потоков");
        }

        System.out.println("\n========================================");
        String info = "Лабораторная работа выполнена студентами группы: Степанович Никита и Згурский Дмитрий";
        for(int i = 0; i < info.length(); i++) {
            System.out.print(info.charAt(i));
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                System.out.println("Ошибка при задержке");
            }
        }

        System.out.println("\n========================================");
    }
}