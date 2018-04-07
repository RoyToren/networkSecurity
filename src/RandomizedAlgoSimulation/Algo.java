package RandomizedAlgoSimulation;


import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

import static java.util.Collections.synchronizedCollection;

public class Algo {

    public static int count = 0;
    public CyclicBarrier currRoundBarrier;
    public Collection<Clock> clocks;
    public ConcurrentHashMap<Integer, Clock> byzantineClocks;
    public int numOfRounds = 8;
    /* Since we process one round at a time, we collect messages here. */
    public ConcurrentHashMap<Integer, Integer> currRoundValues;
    int clockAmount, byzantineAmount;
    int id = 0;
    int maxClockSize;
    boolean isReady = false;

    public Algo(int clockAmount, int byzantineAmount, int maxClockSize) {
        this.clockAmount = clockAmount;
        this.maxClockSize = maxClockSize;
        this.clocks = synchronizedCollection(new ArrayList<Clock>());
        this.byzantineAmount = byzantineAmount;
        currRoundValues = new ConcurrentHashMap<>();
        byzantineClocks = new ConcurrentHashMap<>();
        currRoundBarrier = new CyclicBarrier(this.clockAmount, new handleRoundEnd());
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
    public synchronized void InitializeClocks(Clock clock) throws InterruptedException {
        clock.assignId(id++);
        clock.currValue = getRandomNumberInClockSize(maxClockSize);
        if (clock.isByzantine) {
            byzantineClocks.put(id, clock);
        } else {
            clocks.add(clock);
        }
        currRoundValues.put(clock.id, clock.currValue);
        if (clocks.size() + byzantineClocks.size() == clockAmount) {
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
        for (Integer key : currRoundValues.keySet()) {
            if (currClock.id != key && !currClock.isByzantine) {
                if (byzantineClocks.containsKey(id)) {
                    counter += getRandomNumberInClockSize(1);//(int) Math.round(Math.random()); //todo: need to add a timeout cas as well
                } else if (currClock.currValue == currRoundValues.get(key)) {
                    counter++;
                }
            }
        }
        if (counter < clockAmount - byzantineAmount - 1) {
            currClock.currValue = 0;
            currClock.lastIncrement = false;
        } else {
            if (currClock.currValue != 0) {
                currClock.currValue = (currClock.currValue + 1) % maxClockSize;
                currClock.lastIncrement = true;
            } else {
                if (currClock.lastIncrement) {
                    currClock.currValue = 1;
                } else {
                    currClock.currValue = getRandomNumberInClockSize(1);
                    //currClock.currValue = (int) Math.round(Math.random());
                }
                if (currClock.currValue == 1) {
                    currClock.lastIncrement = true;
                } else {
                    currClock.lastIncrement = false;
                }
            }
        }
        System.out.println("my id is: " + currClock.id + " and the round is: " + round + " my current value is: " + currClock.currValue);
        int arrive_index = currRoundBarrier.await();

        // This is not racy because threads will all reach send barrier
        // before reentering here.
        if (arrive_index == 0) {
            currRoundBarrier.reset();
        }
    }

    public static int getRandomNumberInClockSize(int max) {
        Random r = new Random();
        return r.nextInt((max - 0) + 1) + 0;
    }


    private class handleRoundEnd implements Runnable {
        @Override
        public void run() {
            System.out.println("\n end of current round \n");
            System.out.println("current values:");
            for (Clock clock : clocks) {
                currRoundValues.replace(clock.id, clock.currValue);
                System.out.println("id: " + clock.id + " value: " + clock.currValue);
            }
        }
    }
}
