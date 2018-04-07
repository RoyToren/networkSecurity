package RandomizedAlgoSimulation;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulation {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("please enter the following numbers with spaces to separate : number of clocks, number of byzantine, number of rounds and clock size");
        int clockAmount = input.nextInt();
        int byzantineAmount = input.nextInt();
        int rounds = input.nextInt();
        int maxClockSize = input.nextInt();
        ExecutorService ex = Executors.newFixedThreadPool(clockAmount);
        System.out.println("Started " + clockAmount + " Clocks.");
        Algo algo = new Algo(clockAmount, byzantineAmount, maxClockSize, rounds);

        createClocks(clockAmount, byzantineAmount, ex, algo);
        System.out.println("Clocks finished, exiting");
        ex.shutdown();
    }

    private static void createClocks(int clockAmount, int byzantineAmount, ExecutorService ex, Algo algo) {
        for (int i = 0; i < clockAmount - byzantineAmount; i++) {
            Clock clk = new Clock(algo);
            ex.execute(clk);
        }
    }
}

