import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;


class Th1 extends Thread {
    private final int[] arr;
 
    private final int start;
    private final int end;
    private final int step = 1; 

   
    public Th1(int[] arr) {
        this.arr = arr;
        this.start = 0; 
        this.end = arr.length; 
    }

    
    @Override
    public void run() {
        int pos1 = -1; 

       
        for (int i = start; i < end; i += step) {

          
            if (arr[i] % 2 != 0) {
                if (pos1 == -1) {
                    
                    pos1 = i;
                } else {
                   
                    int pos2 = i;
                    int sumOfPositions = pos1 + pos2;

                   
                    System.out.printf("%s %d %d %d %d %d%n",
                            getName(),
                            pos1, pos2,
                            sumOfPositions,
                            arr[pos1], arr[pos2]);

                   
                    pos1 = -1;
                }
            }
        }
    }
}



class Th2 extends Thread {
    private final int[] arr;
    private final int start;
    private final int end = -1; 
    private final int step = -1; 

    
    public Th2(int[] arr) {
        this.arr = arr;
        this.start = arr.length - 1; 
    }

    
    @Override
    public void run() {
        int pos1 = -1; 

       
        for (int i = start; i > end; i += step) { 

           
            if (arr[i] % 2 != 0) {
                if (pos1 == -1) {
                   
                    pos1 = i;
                } else {
                  
                    int pos2 = i;
                    int sumOfPositions = pos1 + pos2;

                    
                    System.out.printf("%s %d %d %d %d %d%n",
                            getName(),
                            pos1, pos2,
                            sumOfPositions,
                            arr[pos1], arr[pos2]);

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
            System.out.print(mas[i] + " ");

            if((i + 1) % 20 == 0) {
                System.out.println();
            }
        }
        System.out.println("\n");

        Th1 thread1 = new Th1(mas);
        Th2 thread2 = new Th2(mas);

        thread1.setName("Поток-1");
        thread2.setName("Поток-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch(InterruptedException e) {
            System.out.println("Ошибка при ожидании потоков");
        }

        System.out.println("\n========================================");
        String info = "Лабораторная работа выполнена студентами группы:Степанович Никита и Згурский Дмитрий";
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