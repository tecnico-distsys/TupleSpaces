package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class OrderedDelayer implements Iterable<Integer> {
    
    class DelayPair implements Comparable<DelayPair> {
        public int id;
        public int delay;

        public DelayPair(int id) {
            this.id = id;
            this.delay = 0;
        }

        @Override
        public int compareTo(DelayPair o) {
            int i = Integer.valueOf(this.delay).compareTo(o.delay);
            return i;
        }
    }

    class DelayPairIterator implements Iterator<Integer> {

        Iterator<DelayPair> iter;
        int alreadySlept = 0;

        public DelayPairIterator(ArrayList<DelayPair> orderedDelayPairs) {
            iter = orderedDelayPairs.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }
    
        @Override
        public Integer next() {
            DelayPair dp = iter.next();
            if (dp != null) {
                int secsToSleep = dp.delay - alreadySlept;
                if (secsToSleep > 0)
                    try {
                        // System.out.println("will sleep for " + secsToSleep);
                        Thread.sleep((dp.delay - alreadySlept) * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                alreadySlept += secsToSleep;
                return dp.id;
            } else
                return null;
        }

    }

    ArrayList<DelayPair> orderedDelayPairs;

    public OrderedDelayer(int numItems) {
        orderedDelayPairs = new ArrayList<DelayPair>();
        for (int i = 0; i < numItems; i++)
            orderedDelayPairs.add(new DelayPair(i));
    }

    public int setDelay(int id, int delay) {
        Iterator<DelayPair> iter = orderedDelayPairs.iterator();

        while (iter.hasNext()) {
            DelayPair dp = iter.next();
            if (dp.id == id) {
                dp.delay = delay;
                Collections.sort(orderedDelayPairs);
                return delay;
            }
        }
        return -1;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new DelayPairIterator(orderedDelayPairs);
    }
}
