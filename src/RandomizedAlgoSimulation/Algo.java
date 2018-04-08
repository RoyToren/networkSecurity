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


    public synchronized void initializeClocks(Clock clock) throws InterruptedException {
        clock.assignId(id++);
        clock.setCurrValue(getRandomNumberInClockSize(maxClockSize));
        clocks.add(clock);
        currRoundValues.put(clock.getId(), clock.getCurrValue());
        if (clocks.size() + byzantineAmount == clockAmount) {
            isReady = true;
            notifyAll();
        } else {
            while (!isReady) {
                wait();
            }
        }
    }

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
        System.out.println("id: " + currClock.getId() + " new value: " + currClock.getCurrValue() + " clocks amount with same value: " + counter + " round: " + round);
        int arrive_index = currRoundBarrier.await();

        // This is not racy because threads will all reach send barrier todo: check if needed
        // before reentering here.
 /*       if (arrive_index == 0) {
            currRoundBarrier.reset();
        }*/
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
            System.out.println("values at round start:");
            for (Clock clock : clocks) {
                currRoundValues.replace(clock.getId(), clock.getCurrValue());
                System.out.println("id: " + clock.getId() + " value: " + clock.getCurrValue());
            }
        }
    }
}
