import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Sklad sklad = new Sklad();

        Proizvoditel p1 = new Proizvoditel(sklad);
        p1.setName("Производитель №1");
        p1.setDaemon(true);

        Proizvoditel p2 = new Proizvoditel(sklad);
        p2.setName("Производитель №2");
        p2.setDaemon(true);

        Proizvoditel p3 = new Proizvoditel(sklad);
        p3.setName("Производитель №3");
        p3.setDaemon(true);

        Potrebitel c1 = new Potrebitel(sklad);
        c1.setName("Потребитель №1");

        Potrebitel c2 = new Potrebitel(sklad);
        c2.setName("Потребитель №2");

        Potrebitel c3 = new Potrebitel(sklad);
        c3.setName("Потребитель №3");

        Potrebitel c4 = new Potrebitel(sklad);
        c4.setName("Потребитель №4");

        p1.start();
        p2.start();
        p3.start();
        c1.start();
        c2.start();
        c3.start();
        c4.start();

        while(c1.isAlive() || c2.isAlive() || c3.isAlive() || c4.isAlive()) {
        }

        System.out.println("\n=== Все потоки завершены ===");
    }
}

// Класс склада
class Sklad {
    private ArrayList<Integer> tovary = new ArrayList<Integer>();
    private int maxRazmer = 5;

    public synchronized void vzyat(String imya) {
        while (tovary.size() < 1) {
            try {
                System.out.println(imya + " ждет. Склад пуст!");
                wait();
            } catch (InterruptedException e) {
            }
        }

        int tovar = tovary.get(tovary.size() - 1);
        tovary.remove(tovary.size() - 1);
        System.out.println(imya + " взял со склада: " + tovar);

        pokazatSklad();

        notifyAll();
    }

    // Метод для помещения товара на склад
    public synchronized void polozit(String imya, int tovar1, int tovar2) {
        while (tovary.size() + 2 > maxRazmer) {
            try {
                System.out.println(imya + " ждет. Склад полон!");
                wait();
            } catch (InterruptedException e) {
            }
        }

        tovary.add(tovar1);
        tovary.add(tovar2);
        System.out.println(imya + " поместил на склад два числа: " + tovar1 + ", " + tovar2);

        pokazatSklad();

        notifyAll();
    }

    // Метод для показа состояния склада
    private void pokazatSklad() {
        if (tovary.size() != 0) {
            System.out.print("На складе имеется " + tovary.size() + " единиц -> ");
            for (int tovar : tovary) {
                System.out.print(tovar + " ");
            }
            System.out.println();
        } else {
            System.out.println("Склад пуст");
        }
    }
}

// Класс производителя
class Proizvoditel extends Thread {
    private Sklad sklad;

    public Proizvoditel(Sklad sklad) {
        this.sklad = sklad;
    }

    @Override
    public void run() {
        int[] nechetnyeChisla = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29};

        while (true) {
            int chislo1 = nechetnyeChisla[(int)(Math.random() * nechetnyeChisla.length)];
            int chislo2 = nechetnyeChisla[(int)(Math.random() * nechetnyeChisla.length)];

            sklad.polozit(getName(), chislo1, chislo2);

            try {
                Thread.sleep((int)(Math.random() * 100));
            } catch (InterruptedException e) {
            }
        }
    }
}

// Класс потребителя
class Potrebitel extends Thread {
    private Sklad sklad;
    private int nuzhnoTovarov = 2;

    public Potrebitel(Sklad sklad) {
        this.sklad = sklad;
    }

    @Override
    public void run() {
        for (int i = 0; i < nuzhnoTovarov; i++) {
            sklad.vzyat(getName());

            try {
                Thread.sleep((int)(Math.random() * 100));
            } catch (InterruptedException e) {
            }
        }

        System.out.println(getName() + " взял " + nuzhnoTovarov + " числа. Поток завершен");
    }
}