package RandomizedAlgoSimulation;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulation {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("please enter number of clocks and number of byzantine");
        int clockAmount = input.nextInt();
        int byzantineAmount = input.nextInt();
        int maxClockSize = 10;

        ExecutorService ex = Executors.newFixedThreadPool(clockAmount);
        System.out.println("Started " + clockAmount + " Clocks.");
        Algo algo = new Algo(clockAmount, byzantineAmount, maxClockSize);

        createClocks(clockAmount, byzantineAmount, maxClockSize, ex, algo);
        System.out.println("Clocks finished, exiting");
        ex.shutdown();
    }

    private static void createClocks(int clockAmount, int byzantineAmount, int maxClockSize, ExecutorService ex, Algo algo) {
        for (int i = 0; i < clockAmount - byzantineAmount; i++) {
            Clock clk = new Clock(false, maxClockSize, algo);
            ex.execute(clk);
        }
        for (int i = 0; i < byzantineAmount; i++) {
            Clock clk = new Clock(true, maxClockSize, algo);
            ex.execute(clk);
        }
    }
}

