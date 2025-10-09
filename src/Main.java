import java.util.*;

// Класс для первого потока - через интерфейс Runnable
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