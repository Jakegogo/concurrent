package dbcache.test;

import com.lmax.disruptor.*;

import dbcache.test.Test8.Event;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author LiangZengLe
 */
public class RingBufferQueue<E> {
    private RingBuffer<Event<E>> ringBuffer;
    private MultiProducerSequencer sequencer;
    private AtomicLong seq = new AtomicLong();

    @SuppressWarnings("unchecked")
    public RingBufferQueue(int bufferSize) {
        Constructor<RingBuffer> constructor;
        try {
            constructor = RingBuffer.class.getDeclaredConstructor(EventFactory.class, Sequencer.class);
            constructor.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sequencer = new MultiProducerSequencer(bufferSize, new BlockingWaitStrategy());
        try {
            ringBuffer = constructor.newInstance(new EventFactory<Event>() {
        		public Event newInstance() {
        			return new Event();
        		}
        	}, sequencer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long size() {
        return sequencer.getCursor() - seq.get() + 1;
    }

    public boolean offer(E e) {
        long next;
        try {
            next = ringBuffer.tryNext();
        } catch (InsufficientCapacityException ex) {
            throw new RuntimeException(ex);
        }
        try {
            ringBuffer.get(next).e = e;
        } finally {
            ringBuffer.publish(next);
        }

        return true;
    }

    public E poll() {
        long sequence;
        do {
            sequence = seq.get();
            if (size() == 0) {
                return null;
            }
        } while (!seq.compareAndSet(sequence, sequence + 1));
        Event<E> e = ringBuffer.get(sequence);
        if (e == null) {
        	throw new IllegalStateException();
        }
        return e.e;
    }

    private static class Event<E> {
        private E e;
    }
}
