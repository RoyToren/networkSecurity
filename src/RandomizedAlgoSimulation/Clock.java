package RandomizedAlgoSimulation;

import java.util.concurrent.BrokenBarrierException;

public class Clock implements Runnable {

    private Algo algo;
    private int currValue;
    private int id;

    public void setLastIncrement(boolean lastIncrement) {
        this.lastIncrement = lastIncrement;
    }

    private boolean lastIncrement;

    public int getCurrValue() {
        return currValue;
    }

    public int getId() {
        return id;
    }

    public boolean isLastIncrement() {
        return lastIncrement;
    }

    public void setCurrValue(int currValue) {
        this.currValue = currValue;
    }

    Clock(Algo algo) {
        this.lastIncrement = false;
        this.algo = algo;
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
    private void startSimulation() throws InterruptedException, BrokenBarrierException {
        algo.initializeClocks(this);
        System.out.println("initialized clock number " + id + "value:" + currValue);

        for (int round = 0; round < algo.getNumOfRounds(); round++) {
            algo.handleRound(this, round);
        }
    }

}