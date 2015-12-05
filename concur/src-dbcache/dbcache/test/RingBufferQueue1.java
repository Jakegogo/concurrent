package dbcache.test;

import com.lmax.disruptor.*;
import org.springframework.util.Assert;


import java.lang.reflect.Constructor;

/**
 * @author LiangZengLe
 */
public class RingBufferQueue1<E> {
    private final RingBuffer<Event> ringBuffer;
    private final Sequencer sequencer;

    private final Sequence consumerSequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private static final EventTranslatorOneArg<Event, Object> TRANSLATOR = new EventTranslatorOneArg<Event, Object>() {
        public void translateTo(final Event event, long sequence, final Object arg0) {
            event.set(arg0);
        }
    };


    private static Constructor<RingBuffer> ringBufferConstructor;

    static {
        try {
            ringBufferConstructor = RingBuffer.class.getDeclaredConstructor(EventFactory.class, Sequencer.class);
            ringBufferConstructor.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public RingBufferQueue1(int bufferSize) {
        sequencer = new MultiProducerSequencer(bufferSize, new BlockingWaitStrategy());
        try {
            ringBuffer = ringBufferConstructor.newInstance(new EventFactory<Event>() {
                public Event newInstance() {
                    return new Event();
                }
            }, sequencer);

            ringBuffer.addGatingSequences(consumerSequence);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long size() {
        return sequencer.getCursor() - consumerSequence.get();
    }


    public void put(E e) {
        Assert.notNull(e, "null element is not allowed.");
        ringBuffer.publishEvent(TRANSLATOR, e);
    }


    public boolean offer(E e) {
        Assert.notNull(e, "null element is not allowed.");
        return ringBuffer.tryPublishEvent(TRANSLATOR, e);
    }

    public E poll() {

        long prev;
        long next;
        E e;
        do {


            prev = consumerSequence.get();
            next = prev + 1;
            if (!sequencer.isAvailable(next)) {
                return null;
            }

            e = ringBuffer.get(next).get();


        } while (!consumerSequence.compareAndSet(prev, next));

        return e;
    }

    @SuppressWarnings("unchecked")
    private static class Event {
        private Object e;

        private <T> T get() {
            return (T) e;
        }

        private void set(Object e) {
            this.e = e;
        }
    }
}
