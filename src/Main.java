import java.util.*;

class Th1 implements Runnable {
    private int[] mas;
    private String name;

    public Th1(int[] mas, String name) {
        this.mas = mas;
        this.name = name;
    }

    public void run() {
        System.out.println(name + " начало");
        int i = 0;

        while(i < mas.length) {
            if(mas[i] % 2 == 0) {
                int pos1 = i;
                i++;
                while(i < mas.length) {
                    if(mas[i] % 2 == 0) {
                        int pos2 = i;
                        int suma = pos1 + pos2;

                        System.out.println(name + ": позиция1=" + pos1 +
                                ", позиция2=" + pos2 +
                                ", сумма позиций=" + suma +
                                ", значения: [" + mas[pos1] + ", " + mas[pos2] + "]");
                        break;
                    }
                    i++;
                }
            }
            i++;
        }

        System.out.println(name + " конец");
    }
}

class Th2 implements Runnable {
    private int[] mas;
    private String name;

    public Th2(int[] mas, String name) {
        this.mas = mas;
        this.name = name;
    }

    public void run() {
        System.out.println(name + " начало");

        int i = mas.length - 1;

        while(i >= 0) {
            if(mas[i] % 2 == 0) {
                int pos1 = i;
                i--;
                while(i >= 0) {
                    if(mas[i] % 2 == 0) {
                        int pos2 = i;
                        int suma = pos1 + pos2;

                        System.out.println(name + ": позиция1=" + pos1 +
                                ", позиция2=" + pos2 +
                                ", сумма позиций=" + suma +
                                ", значения: [" + mas[pos1] + ", " + mas[pos2] + "]");
                        break;
                    }
                    i--;
                }
            }
            i--;
        }

        System.out.println(name + " конец");
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
