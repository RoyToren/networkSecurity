package RandomizedAlgoSimulation;


import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;

import static java.util.Collections.synchronizedCollection;

public class Algo {

    private CyclicBarrier currRoundBarrier;
    private Collection<Clock> clocks;
    private int numOfRounds;
    /* Since we process one round at a time, we collect messages here. */
    private ConcurrentMap<Integer, Integer> currRoundValues;
    private int clockAmount;
    private int byzantineAmount;
    private int id = 0;
    private int maxClockSize;
    private boolean isReady = false;

    Algo(int clockAmount, int byzantineAmount, int maxClockSize, int numberOfRounds) {
        this.clockAmount = clockAmount;
        this.maxClockSize = maxClockSize;
        this.clocks = synchronizedCollection(new ArrayList<Clock>());
        this.byzantineAmount = byzantineAmount;
        this.numOfRounds = numberOfRounds;
        currRoundValues = new ConcurrentHashMap<>();
        currRoundBarrier = new CyclicBarrier(this.clockAmount-this.byzantineAmount, new HandleRoundEnd());
    }


    /**
     * Report for duty. Registers Clock instance with the Algo and blocks
     * until all clocks are accounted for. Since method is synchronized,
     * callers acquire intrinsic lock on this object and we can do stuff like
     * wait() and notify().
     * Could also use CyclicBarrier here.. not as much fun.
     *
     * @param clock
     */
    public synchronized void initializeClocks(Clock clock) throws InterruptedException {
        clock.assignId(id++);
        clock.setCurrValue(getRandomNumberInClockSize(maxClockSize));
        clocks.add(clock);
        currRoundValues.put(clock.getId(), clock.getCurrValue());
        if (clocks.size() + byzantineAmount == clockAmount) {
            // We are last clock to report, let's begin.
            isReady = true;
            notifyAll();
        } else {
            /* Loop to handle spurious wakeups; e.g. from signals. */
            while (!isReady) {
                wait();
            }
        }
    }

    /**
     * Block until all clocks are heard from, then return received messages.
     */
    public void handleRound(Clock currClock, int round) throws BrokenBarrierException, InterruptedException {
        int counter = 0;
        for (Map.Entry<Integer, Integer> entry : currRoundValues.entrySet()) {
            if (currClock.getId() != entry.getKey()) {
                if (currClock.getCurrValue() == entry.getValue()) {
                    counter++;
                }
            }
        }
        for (int i = 0; i < byzantineAmount; i++) {
            counter += getRandomNumberInClockSize(1);//(int) Math.round(Math.random()); //todo: need to add a timeout cas as well
        }
        if (counter < clockAmount - byzantineAmount - 1) {
            currClock.setCurrValue(0);
            currClock.setLastIncrement(false);
        } else {
            if (currClock.getCurrValue() != 0) {
                currClock.setCurrValue((currClock.getCurrValue() + 1) % maxClockSize);
                currClock.setLastIncrement(true);
            } else {
                if (currClock.isLastIncrement()) {
                    currClock.setCurrValue(1);
                } else {
                    currClock.setCurrValue(getRandomNumberInClockSize(1));
                }
                if (currClock.getCurrValue() == 1) {
                    currClock.setLastIncrement(true);
                } else {
                    currClock.setLastIncrement(false);
                }
            }
        }
        System.out.println("round: " + round + " id: " + currClock.getId() + " value is: " + currClock.getCurrValue() + " amount of clocks with the same value I had: " + counter);
        int arrive_index = currRoundBarrier.await();

        // This is not racy because threads will all reach send barrier
        // before reentering here.
        if (arrive_index == 0) {
            currRoundBarrier.reset();
        }
    }

    private static int getRandomNumberInClockSize(int max) {
        Random r = new Random();
        return r.nextInt((max - 0) + 1) + 0;
    }

    public int getNumOfRounds() {
        return numOfRounds;
    }


    private class HandleRoundEnd implements Runnable {
        @Override
        public void run() {
            System.out.println("\n end of current round \n");
            System.out.println("current values:");
            for (Clock clock : clocks) {
                currRoundValues.replace(clock.getId(), clock.getCurrValue());
                System.out.println("id: " + clock.getId() + " value: " + clock.getCurrValue());
            }
        }
    }
}
