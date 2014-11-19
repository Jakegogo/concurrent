package lock.checkdeadlock;
/*
Java Threads, 3rd Edition
By Scott Oaks, Henry Wong
3rd Edition September 2004 
ISBN: 0-596-00782-5

*/

import lock.ObjectLock;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// This is a very slow implementation of a ReentrantLock class and is not for
//   everyday usage. The purpose of this class is to test for deadlocks. The
// lock()
//   method now throws a DeadlockDetectedException, if a deadlock occurs. This
//   Alternate version has some production properties, including faster
//   deadlock check, full implementation of all lock methods, and configurable
//   options.
public class AlternateDeadlockDetectingLock extends ObjectLock {
  // List of deadlock detecting locks.
  // This array is not thread safe, and must be externally synchronized
  //    by the class lock. Hence, it should only be called by static
  //    methods.
  private static List deadlockLocksRegistry = new ArrayList();

  private static synchronized void registerLock(
      AlternateDeadlockDetectingLock ddl) {
    if (!deadlockLocksRegistry.contains(ddl))
      deadlockLocksRegistry.add(ddl);
  }

  private static synchronized void unregisterLock(
      AlternateDeadlockDetectingLock ddl) {
    if (deadlockLocksRegistry.contains(ddl))
      deadlockLocksRegistry.remove(ddl);
  }

  // List of threads hard waiting for this lock.
  // This array is not thread safe, and must be externally synchronized
  //    by the class lock. Hence, it should only be called by static
  //    methods.
  private List hardwaitingThreads = new ArrayList();

  private static synchronized void markAsHardwait(List l, Thread t) {
    if (!l.contains(t))
      l.add(t);
  }

  private static synchronized void freeIfHardwait(List l, Thread t) {
    if (l.contains(t))
      l.remove(t);
  }

  //
  // Deadlock checking methods
  //
  // Given a thread, return all locks that are already owned
  // Must own class lock prior to calling this method
  private static Iterator getAllLocksOwned(Thread t) {
    AlternateDeadlockDetectingLock current;
    ArrayList results = new ArrayList();

    Iterator itr = deadlockLocksRegistry.iterator();
    while (itr.hasNext()) {
      current = (AlternateDeadlockDetectingLock) itr.next();
      if (current.getOwner() == t)
        results.add(current);
    }
    return results.iterator();
  }

  // Given a lock, return all threads that are hard waiting for the lock
  // Must own class lock prior to calling this method
  private static Iterator getAllThreadsHardwaiting(
      AlternateDeadlockDetectingLock l) {
    return l.hardwaitingThreads.iterator();
  }

  // Check to see if a thread can perform a hard wait on a lock
  // Must call synchronized version only...
  private static boolean canThreadWaitOnLock0(Thread t,
      AlternateDeadlockDetectingLock l) {
    Iterator locksOwned = getAllLocksOwned(t);
    while (locksOwned.hasNext()) {
      AlternateDeadlockDetectingLock current = (AlternateDeadlockDetectingLock) locksOwned
          .next();

      // Thread can't wait if lock is already owned. This is the end
      // condition
      //      for the recursive algorithm -- as the initial condition should be
      //      already tested for.
      if (current == l)
        return false;

      Iterator waitingThreads = getAllThreadsHardwaiting(current);
      while (waitingThreads.hasNext()) {
        Thread otherthread = (Thread) waitingThreads.next();

        // In order for the thread to safely wait on the lock, it can't
        //   own any locks that have waiting threads that already owns
        //   lock. etc. etc. etc. recursively etc.
        if (!canThreadWaitOnLock0(otherthread, l)) {
          return false;
        }
      }
    }
    return true;
  }

  private static synchronized boolean canThreadWaitOnLock(Thread t,
      AlternateDeadlockDetectingLock l) {
    // Skip check if there is no owner
    // There is a race condition is the owner is null. However, it doesn't
    // matter.
    //     Testing for no owner ensures none of the threads in the thread wait
    //     tree will grab it later -- as all locks in the tree are owned.
    if (l.getOwner() == null) {
      return true;
    }
    return canThreadWaitOnLock0(t, l);
  }

  // Options: variable to control behavior
  //    FastFail: if set true, deadlock exception will thrown for every call
  // after
  //              first exception is detected
  //     CleanUp: if set true, lock will cleanup deadlock condition -- allowing
  //              for continued operation after failure. FastFail must be off.
  //    HWSWTime: # of seconds before a Softwait is to be considered as a
  // hardwait.
  //              Default is 60 seconds.
  private static boolean DDLFastFail = false;

  private static boolean DDLCleanUp = false;

  private static int DDLHWSWTime = 60;

  // Core Constructors
  //
  public AlternateDeadlockDetectingLock() {
    this(false, false);
  }

  public AlternateDeadlockDetectingLock(boolean fair) {
    this(fair, false);
  }

  public AlternateDeadlockDetectingLock(Object object) {
    super(object, false);
  }

  private boolean debugging;

  public AlternateDeadlockDetectingLock(boolean fair, boolean debug) {
    super(fair);
    debugging = debug;
    registerLock(this);
  }

  private static boolean DDLdeadlockDETECTED = false;

  //
  // Core Methods
  //
  public void lock() {
    if (DDLFastFail && DDLdeadlockDETECTED) {
      throw new DeadlockDetectedException("EARILER DEADLOCK DETECTED");
    }

    // Note: Owner can't change if current thread is owner. It is
    //       not guaranteed otherwise. Other owners can change due to
    //       condition variables.
    if (isHeldByCurrentThread()) {
      if (debugging)
        System.out.println("Already Own Lock");
      super.lock();
      freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      return;
    }

    // Note: The wait list must be marked before it is tested because
    //       there is a race condition between lock() method calls.
    markAsHardwait(hardwaitingThreads, Thread.currentThread());
    if (canThreadWaitOnLock(Thread.currentThread(), this)) {
      if (debugging)
        System.out.println("Waiting For Lock");
      super.lock();
      freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      if (debugging)
        System.out.println("Got New Lock");
    } else {
      DDLdeadlockDETECTED = true;
      if (DDLCleanUp)
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      throw new DeadlockDetectedException("DEADLOCK DETECTED");
    }
  }

  //
  // Note: It is debatable whether this is a hard or soft wait. Even if
  //       interruption is common, we don't know if the interrupting thread
  //       is also involved in the deadlock. In this alternate version, it
  //       will be treated as a hard wait.
  public void lockInterruptibly() throws InterruptedException {
    if (DDLFastFail && DDLdeadlockDETECTED) {
      throw new DeadlockDetectedException("EARILER DEADLOCK DETECTED");
    }

    // Note: Owner can't change if current thread is owner. It is
    //       not guaranteed otherwise. Other owners can change due to
    //       condition variables.
    if (isHeldByCurrentThread()) {
      if (debugging)
        System.out.println("Already Own Lock");
      try {
        super.lockInterruptibly();
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      }
      return;
    }

    // Note: The wait list must be marked before it is tested because
    //       there is a race condition between lock() method calls.
    markAsHardwait(hardwaitingThreads, Thread.currentThread());
    if (canThreadWaitOnLock(Thread.currentThread(), this)) {
      if (debugging)
        System.out.println("Waiting For Lock");
      try {
        super.lockInterruptibly();
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      }
      if (debugging)
        System.out.println("Got New Lock");
    } else {
      DDLdeadlockDETECTED = true;
      if (DDLCleanUp)
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      throw new DeadlockDetectedException("DEADLOCK DETECTED");
    }
  }

  //
  // Note: It is debatable where is the point between a hard wait and a
  //      soft wait. Is it still a soft wait, if the timeout is large? As
  //      compromise, it is to be considered a hardwait if the timeout
  //      is larger than a specified time. Developers should modify this method
  //      as needed.
  public boolean tryLock(long time, TimeUnit unit)
      throws InterruptedException {
    if (DDLFastFail && DDLdeadlockDETECTED) {
      throw new DeadlockDetectedException("EARILER DEADLOCK DETECTED");
    }

    // Perform operation as a soft wait
    if (unit.toSeconds(time) < DDLHWSWTime) {
      return super.tryLock(time, unit);
    }

    // Note: Owner can't change if current thread is owner. It is
    //       not guaranteed otherwise. Other owners can change due to
    //       condition variables.
    if (isHeldByCurrentThread()) {
      if (debugging)
        System.out.println("Already Own Lock");
      try {
        return super.tryLock(time, unit);
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      }
    }

    // Note: The wait list must be marked before it is tested because
    //       there is a race condition between lock() method calls.
    markAsHardwait(hardwaitingThreads, Thread.currentThread());
    if (canThreadWaitOnLock(Thread.currentThread(), this)) {
      if (debugging)
        System.out.println("Waiting For Lock");
      try {
        return super.tryLock(time, unit);
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
        if (debugging)
          System.out.println("Got New Lock");
      }
    } else {
      DDLdeadlockDETECTED = true;
      if (DDLCleanUp)
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      throw new DeadlockDetectedException("DEADLOCK DETECTED");
    }
  }

  // Note 1: Deadlocks are possible with any hard wait -- this includes
  //      the reacquitition of the lock upon return from an await() method.
  //      As such, condition variables will mark for the future hard
  //      wait, prior to releasing the lock.
  // Note 2: There is no need to check for deadlock on this end because
  //      a deadlock can be created whether the condition variable owns the
  //      lock or is reacquiring it. Since we are marking *before* giving
  //      up ownership, the deadlock will be detected on the lock() side
  //      first. It is not possible to create a new deadlock just by releasing
  //      locks.
  public class DeadlockDetectingCondition implements Condition {
    Condition embedded;

    protected DeadlockDetectingCondition(ReentrantLock lock,
        Condition embedded) {
      this.embedded = embedded;
    }

    // Note: The algorithm can detect a deadlock condition if the thead is
    //    either waiting for or already owns the lock, or both. This is why
    //    we have to mark for waiting *before* giving up the lock.
    public void await() throws InterruptedException {
      try {
        markAsHardwait(hardwaitingThreads, Thread.currentThread());
        embedded.await();
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      }
    }

    public void awaitUninterruptibly() {
      markAsHardwait(hardwaitingThreads, Thread.currentThread());
      embedded.awaitUninterruptibly();
      freeIfHardwait(hardwaitingThreads, Thread.currentThread());
    }

    public long awaitNanos(long nanosTimeout) throws InterruptedException {
      try {
        markAsHardwait(hardwaitingThreads, Thread.currentThread());
        return embedded.awaitNanos(nanosTimeout);
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      }
    }

    public boolean await(long time, TimeUnit unit)
        throws InterruptedException {
      try {
        markAsHardwait(hardwaitingThreads, Thread.currentThread());
        return embedded.await(time, unit);
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      }
    }

    public boolean awaitUntil(Date deadline) throws InterruptedException {
      try {
        markAsHardwait(hardwaitingThreads, Thread.currentThread());
        return embedded.awaitUntil(deadline);
      } finally {
        freeIfHardwait(hardwaitingThreads, Thread.currentThread());
      }
    }

    public void signal() {
      embedded.signal();
    }

    public void signalAll() {
      embedded.signalAll();
    }
  }

  // Return a condition variable that support detection of deadlocks
  public Condition newCondition() {
    return new DeadlockDetectingCondition(this, super.newCondition());
  }


}

class DeadlockDetectedException extends RuntimeException {

    public DeadlockDetectedException(String s) {
        super(s);
    }
}
