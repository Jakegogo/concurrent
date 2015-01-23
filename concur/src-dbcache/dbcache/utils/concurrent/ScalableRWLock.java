package dbcache.utils.concurrent;

/******************************************************************************
 * Copyright (c) 2012, Pedro Ramalhete
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************
 */

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;


/**
 * A Read-Write Lock that is scalable with the number of threads doing Read.
 * This v1.0
 *
 * @author Pedro Ramalhete: pramalhe@gmail.com
 */
public class ScalableRWLock {
    /* reader states */
    private final int RSTATE_UNUSED  = 0; // -> RSTATE_PREP
    private final int RSTATE_WAITING = 1; // -> RSTATE_PREP
    private final int RSTATE_PREP    = 2; // -> RSTATE_WAITING or -> STATE_READING
    private final int RSTATE_READING = 3; // -> RSTATE_UNUSED

    /* writer states */
    private final int WSTATE_UNUSED      = 0; // -> WSTATE_WRITEORWAIT
    private final int WSTATE_WRITEORWAIT = 1; // -> WSTATE_UNUSED

    private final int CACHE_LINE              = 64;  // size in bytes of a cache line
    private final int CACHE_PADD              = 16;  // CACHE_LINE/sizeof(int) = 16
    private final int MAX_NUM_THREADS         = 256; // Maximum number of concurrent threads at any given time

    private AtomicIntegerArray readers_states = null;
    private char[] pad1                       = null;
    private AtomicInteger writer_state        = null;

    private transient final ThreadLocal<Integer> tid = new ThreadLocal<Integer>();
    private final ReentrantLock mutex                = new ReentrantLock();
    // Protected by mutex
    private AtomicInteger num_assigned               = null;
    private long[] assigned_threads                  = null;



    /**
     * Default constructor.
     */
    ScalableRWLock() {
        // States of the Readers, one per thread
        readers_states = new AtomicIntegerArray(MAX_NUM_THREADS*CACHE_PADD);
        for (int i = 0; i < MAX_NUM_THREADS*CACHE_PADD; i+=CACHE_PADD) {
            readers_states.set(i, RSTATE_UNUSED);
        }

        pad1 = new char[CACHE_LINE];
        pad1[3] = 42;

        writer_state = new AtomicInteger(0);

        // This is AtomicInteger because it will be read outside of the mutex
        num_assigned = new AtomicInteger(0);
        assigned_threads = new long[MAX_NUM_THREADS];
        for (int i = 0; i < MAX_NUM_THREADS; i++)
            assigned_threads[i] = -1;
    }


    /**
     * Yes, this has a mutex protecting it, but it should be called only once
     * per thread throughout the program's execution.
     * TODO: See if we can change this error into an exception.
     */
    public void threadInit() {
        if (num_assigned.get() >= MAX_NUM_THREADS) {
            System.out.println("ERROR: MAX_NUM_THREADS exceeded");
            return;
        }

        mutex.lock();
        for (int i = 0; i < MAX_NUM_THREADS; i++) {
            if (assigned_threads[i] == -1) {
                assigned_threads[i] = Thread.currentThread().getId();
                tid.set(i);
                num_assigned.incrementAndGet();
                break;
            }
        }
        mutex.unlock();
    }


    /**
     * Must be called before the thread exits
     * TODO: check for a lock that has been left locked by looking in readers_states[tid.get()]
     */
    public void threadCleanup() {
        mutex.lock();
        assigned_threads[tid.get()] = -1;
        // Search the highest non-occupied entry and set the num_assigned to it
        for (int i = MAX_NUM_THREADS-1; i > 0; i--) {
            if (assigned_threads[i] != -1) {
                num_assigned.set(i+1);
                break;
            }
        }
        mutex.unlock();
    }



    /**
     * TODO: Implement reentrancy
     */
    public void readLock() {
        int local_tid = tid.get()*CACHE_PADD;
        readers_states.set(local_tid, RSTATE_PREP);
        if (writer_state.get() > 0) {
            // There is a Writer waiting or working, we must yield()
            while (writer_state.get() > 0) {
                readers_states.set(local_tid, RSTATE_WAITING);
                while(writer_state.get() > 0) Thread.yield();
                // TODO: This may be able to be optimized so we don't "flip" the state at every yield() unless write_waiting is actually zero
                readers_states.set(local_tid, RSTATE_PREP);
            }
        }

        // Read-Lock obtained
        readers_states.set(local_tid, RSTATE_READING);
    }



    /**
     * Unlock a previously held Read-Lock
     */
    public void readUnlock() {
        int local_tid = tid.get()*CACHE_PADD;

        if (readers_states.get(local_tid) != RSTATE_READING) {
            // ERROR or ignore: Tried to unlock a non-locked Read-Lock
        }

        readers_states.set(local_tid, RSTATE_UNUSED);
    }



    /**
     *
     * TODO: Implement reentrancy: maybe use part of the bits to identify
     * the owner thread and the remainder for number of loops. see this link
     * http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/java/util/concurrent/locks/ReentrantReadWriteLock.java#ReentrantReadWriteLock.Sync.writerShouldBlock%28%29
     */
    public void writeLock() {
        int[] wait_for_readers = new int[MAX_NUM_THREADS];
        int num_wait_readers = 0;

        while (!writer_state.compareAndSet(WSTATE_UNUSED, WSTATE_WRITEORWAIT))
            Thread.yield();

        // Write-Lock was acquired, now wait for the Readers
        for (int i = 0; i < num_assigned.get()*CACHE_PADD; i+=CACHE_PADD) {
            if (readers_states.get(i) > RSTATE_WAITING)
                wait_for_readers[num_wait_readers++] = i;
        }

        for (int i = 0; i < num_wait_readers; i++) {
            while (readers_states.get(wait_for_readers[i]) > RSTATE_WAITING)
                Thread.yield();
        }
    }



    public void writeUnlock() {

        if (writer_state.get() == 0) {
            // ERROR or ignore: Tried to unlock a non-locked Write-Lock
        }

        writer_state.set(0);
    }

}
