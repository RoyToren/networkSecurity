package RandomizedAlgoSimulation;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;

public class Clock implements Runnable {

    private Algo algo;
    public int currValue;
    public int id;
    public boolean isByzantine;
    public int maxClockSize;
    public boolean lastIncrement;
    public ArrayList<Integer> roundsValues;

    public Clock(boolean isByzantine, int maxClockSize, Algo algo) {
        this.lastIncrement = false;
        this.isByzantine = isByzantine;
        this.algo = algo;
        this.maxClockSize = maxClockSize;
    }

    /*        public boolean majority(Vector<Message> messages) {
            int truth_sum = 0;
            for (Message m : messages) {
                truth_sum += (m.value ? 1 : -1);
            }
            return (truth_sum > 0);  // More trues than falses?
        }*/

    public void assignId(int id) {
        this.id = id;
    }
    @Override
    public void run() {
        try {
            startSimulation();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    /**
     * Communication phase: Iterative approach.
     */
    public void startSimulation() throws InterruptedException, BrokenBarrierException {
        algo.InitializeClocks(this);
        System.out.println("initialized clock number " + id + "value:" + currValue);

        for (int round = 0; round < algo.numOfRounds; round++) {
            algo.handleRound(this, round);
        }
    }
}