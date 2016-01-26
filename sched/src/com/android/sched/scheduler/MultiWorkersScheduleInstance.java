/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sched.scheduler;

import com.android.sched.item.Component;
import com.android.sched.item.Items;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.schedulable.VisitorSchedulable;
import com.android.sched.util.codec.ImplementationFilter;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.codec.ReflectFactorySelector;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ReflectFactory;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.IntegerPropertyId;
import com.android.sched.util.config.id.ListPropertyId;
import com.android.sched.util.config.id.LongPropertyId;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.LoggerFactory;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Allows to run a {@link Plan} using a pool of worker threads.
 *
 * @param <T> the root <i>data</i> type
 */
@HasKeyId
@ImplementationName(iface = ScheduleInstance.class, name = "multi-threaded")
public class MultiWorkersScheduleInstance<T extends Component>
    extends MultipleScheduleInstance<T> {
  @Nonnull
  private static final BooleanPropertyId MANAGED_SYNC = BooleanPropertyId.create(
      "sched.runner.thread.synchronized",
      "If scheduler manages synchronized schedulable by itself").requiredIf(
      ScheduleInstance.DEFAULT_RUNNER.getClazz().isSubClassOf(MultiWorkersScheduleInstance.class))
      .addDefaultValue(Boolean.TRUE);

  @Nonnull
  private static final IntegerPropertyId CHECK_FREQUENCY = IntegerPropertyId.create(
      "sched.runner.thread.detector.frequency",
      "Define at which frequency the detector is triggered (in ms)")
      .withMin(100).requiredIf(ScheduleInstance.DEFAULT_RUNNER.getClazz()
          .isSubClassOf(MultiWorkersScheduleInstance.class)).addDefaultValue("30000");

  @Nonnegative
  private final int checkEvery = ThreadConfig.get(CHECK_FREQUENCY).intValue();

  @Nonnull
  private final Synchronized[] syncs;

  public MultiWorkersScheduleInstance(@Nonnull Plan<T> plan) throws Exception {
    super(plan);

    boolean isSynchronizedManaged = ThreadConfig.get(MANAGED_SYNC).booleanValue();
    syncs = new Synchronized[plan.size()];
    if (isSynchronizedManaged) {
      int idx = 0;
      for (PlanStep step : plan) {
        if (step.getManagedSchedulable().isSynchronized(steps[idx].instance)) {
          syncs[idx] = new Synchronized();
        }
        ++idx;
      }
    }
  }

  private static class Worker extends Thread implements Runnable {
    @Nonnull
    BlockingDeque<Task> queue;

    @CheckForNull
    private Task currentTask = null;

    @Nonnegative
    private long currentTaskStartOn;

    public Worker(@Nonnull String name, @Nonnull BlockingDeque<Task> queue,
        @Nonnegative long stackSize) {
      super(null, null, name, stackSize);
      this.queue = queue;
      this.setDaemon(true);
    }

    /*
     * Synchronized blocks are to maintain atomicity when writing currentTask and currentTaskStartOn
     * fields. By this way, the synchronized getStatus() method retrieves a coherent view. No other
     * concurrent access to currentTask and currentTaskStartOn fields are allowed.
     */
    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    @Override
    public void run() {
      while (true) {
        try {
          synchronized (this) {
            currentTask = null;
            currentTaskStartOn = System.currentTimeMillis();
          }

          Task task = queue.take();

          synchronized (this) {
            currentTask = task;
            currentTaskStartOn = System.currentTimeMillis();
          }

          assert currentTask != null;
          assert currentTask.assertProcessable();

          if (currentTask.process()) {
            synchronized (this) {
              currentTask = null;
              currentTaskStartOn = System.currentTimeMillis();
            }

            return;
          }
        } catch (InterruptedException e) {
          // Nothing to do
        }
      }
    }

    @Nonnull
    public synchronized WorkerStatus getStatus() {
      return new WorkerStatus(currentTask, currentTaskStartOn);
    }
  }

  private static class WorkerStatus {
    @CheckForNull
    private final Task currentTask;

    @Nonnegative
    private final long currentTaskStartOn;

    public WorkerStatus(@CheckForNull Task currentTask, @Nonnegative long currentTaskStartOn) {
      this.currentTask = currentTask;
      this.currentTaskStartOn = currentTaskStartOn;
    }

    @CheckForNull
    public Task getCurrentTask() {
      return currentTask;
    }

    @Nonnegative
    public long getCurrentTaskStartOn() {
      return currentTaskStartOn;
    }
  }

  private static class Synchronized {
    @Nonnull
    private final List<Task> queue = new LinkedList<Task>();

    private boolean taken = false;

    public synchronized boolean tryLock(@Nonnull Task task) {
      if (!taken) {
        taken = true;
        return true;
      } else {
        queue.add(task);
        return false;
      }
    }

    public synchronized void unlock() {
      taken = false;

      if (!queue.isEmpty()) {
        queue.remove(0).enqueue();
      }
    }
  }

  private abstract static class Task {
    @CheckForNull
    private final Task  blocking;
    @Nonnull
    protected final Deque<Task> queue;

    @Nonnull
    private int blockCounter = 0;
    private boolean commited = false;

    public Task(@Nonnull Deque<Task> queue) {
      this.queue = queue;
      blocking = null;
    }

    public Task(@Nonnull Deque<Task> queue, @Nonnull Task blocking) {
      this.queue    = queue;
      this.blocking = blocking;
      synchronized (blocking) {
        assert blocking.commited == false;

        blocking.blockCounter++;
      }
    }

    public void commit() {
      synchronized (this) {
        assert commited == false;
        assert blockCounter >= 0 : "blockCounter = " + blockCounter;

        commited = true;
        if (blockCounter == 0) {
          enqueue();
        }
      }
    }

    public synchronized void prepare() {
      assert blockCounter == 0 : "blockCounter = " + blockCounter;
      assert commited == true;

      commited = false;
    }

    public void notifyEnd() {
      if (blocking != null) {
        synchronized (blocking) {
          assert blocking.blockCounter > 0 : "blockCounter = " + blocking.blockCounter;

          blocking.blockCounter--;
          if (blocking.commited && blocking.blockCounter == 0) {
            blocking.enqueue();
          }
        }
      }
    }

    protected void enqueue() {
      assert assertProcessable();

      queue.addLast(this);
    }

    public synchronized boolean assertProcessable() {
      assert blockCounter == 0 : "blockCounter = " + blockCounter;
      assert commited == true;

      return true;
    }

    abstract boolean process();
  }

  private static class ShutdownTask extends Task {
    public ShutdownTask(@Nonnull Deque<Task> queue) {
      super(queue);
    }

    @SuppressWarnings("unused")
    public void throwPending() throws ProcessException, AssertionError {
    }

    @Override
    protected void enqueue() {
      queue.addFirst(this);
    }

    @Override
    public boolean process() {
      // Re-queue for other threads
      prepare();
      commit();
      // Terminate this thread
      return true;
    }

    @Override
    @Nonnull
    public String toString() {
      return "a shutdown task";
    }
  }

  private static class ProcessExceptionTask extends ShutdownTask {
    @Nonnull
    private final ProcessException exception;

    public ProcessExceptionTask(@Nonnull Deque<Task> queue, @Nonnull ProcessException exception) {
      super(queue);

      this.exception = exception;
    }

    @Override
    public void throwPending() throws ProcessException {
      throw exception;
    }

    @Override
    @Nonnull
    public String toString() {
      return "an exception task (" + exception.getClass().getCanonicalName() + ": "
          + exception.getMessage();
    }
  }

  private static class AssertionErrorTask extends ShutdownTask {
    @Nonnull
    private final AssertionError error;

    public AssertionErrorTask(@Nonnull Deque<Task> queue, @Nonnull AssertionError error) {
      super(queue);

      this.error = error;
    }

    @Override
    public void throwPending() throws AssertionError {
      throw error;
    }

    @Override
    @Nonnull
    public String toString() {
      return "an error task (" + error.getClass().getCanonicalName() + ": "
          + error.getMessage();
    }
  }

  private static class SequentialTask<U extends Component> extends Task {
    @Nonnull
    private final U data;
    private int next = 0;
    @Nonnull
    private final MultiWorkersScheduleInstance<U> instances;

    public SequentialTask(@Nonnull Deque<Task> queue,
        @Nonnull MultiWorkersScheduleInstance<U> instances, @Nonnull U data,
        @Nonnull Task blocking) {
      super(queue, blocking);

      this.data = data;
      this.instances = instances;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean process() {
      while (true) {
        if (next < instances.steps.length) {
          SchedStep step = instances.steps[next];
          Synchronized sync = instances.syncs[next];
          Schedulable instance = step.getInstance();

          if (sync != null && !sync.tryLock(this)) {
            break;
          }

          next++;
          try {
            if (instance instanceof AdapterSchedulable) {
              Iterator<U> dataIter = instances.adaptWithLog((AdapterSchedulable) instance, data);
              if (dataIter.hasNext()) {
                MultiWorkersScheduleInstance<?> subSchedInstance =
                    (MultiWorkersScheduleInstance<?>) step.getSubSchedInstance();
                assert subSchedInstance != null;

                prepare();
                do {
                  new SequentialTask(queue, subSchedInstance, dataIter.next(), this).commit();
                } while (dataIter.hasNext());
                commit();
                break;
              }

              // No data, next in SequentialTask.
            } else if (instance instanceof RunnableSchedulable) {
              instances.runWithLog((RunnableSchedulable) instance, data);
            } else if (instance instanceof VisitorSchedulable) {
              instances.visitWithLog((VisitorSchedulable) instance, data);
            } else {
              throw new AssertionError();
            }
          } catch (ProcessException e) {
            new ProcessExceptionTask(queue, e).commit();
            break;
          } finally {
            if (sync != null) {
              sync.unlock();
            }
          }
        } else {
          notifyEnd();
          break;
        }
      }

      // Next in Queue, does not quit the thread
      return false;
    }

    @Override
    @Nonnull
    public String toString() {
      return "a sequential task running " + Items.getName(
          instances.steps[next - 1].getInstance().getClass()) + " on '" + data + "'";
    }
  }

  @Override
  public <X extends VisitorSchedulable<T>, U extends Component> void process(@Nonnull T data)
      throws ProcessException       {
    BlockingDeque<Task> queue = new LinkedBlockingDeque<Task>();

    // Initialize queue with the initial plan, and block a shutdown Task on it
    Task shutdown = new ShutdownTask(queue);
    new SequentialTask<T>(queue, this, data, shutdown).commit();
    shutdown.commit();

    // Create threads
    int threadPoolSize = getThreadPoolSize();

    List<Worker> activeWorkers = new ArrayList<Worker>(threadPoolSize);
    long stackSize = ThreadConfig.get(ScheduleInstance.DEFAULT_STACK_SIZE).longValue();
    for (int i = 0; i < threadPoolSize; i++) {
      Worker worker = new Worker("sched-worker-" + i, queue, stackSize);

      worker.start();
      activeWorkers.add(worker);
    }

    List<Detector> detectors;
    {
      List<ReflectFactory<Detector>> factories = ThreadConfig.get(Detector.DETECTORS);
      detectors = new ArrayList<Detector>(factories.size());
      for (ReflectFactory<Detector> factory : factories) {
        detectors.add(factory.create(Integer.valueOf(activeWorkers.size())));
      }
    }

    // Wait for threads termination
    boolean shutdownInProgress = false;
    while (activeWorkers.size() > 0) {
      Thread thread = activeWorkers.get(0);

      try {
        thread.join(checkEvery);
      } catch (InterruptedException e) {
        // Nothing to do
      }
      if (!thread.isAlive()) {
        activeWorkers.remove(0);
      }

      for (Detector detector : detectors) {
        if (!detector.check(activeWorkers) && !shutdownInProgress) {
          // If there is a blocked thread detected, shutdown all tasks
          shutdownInProgress = true;
          new AssertionErrorTask(queue, new AssertionError()).commit();
        }
      }
    }

    // Process the termination
    ((ShutdownTask) queue.pop()).throwPending();

    // Check that no task remains in the queue
    assert queue.isEmpty() : "Queue is not empty, size is " + queue.size();
  }

  //
  // Detector interface
  //

  @HasKeyId
  @VariableName("detector")
  private abstract static class Detector {
    @Nonnull
    public static final ListPropertyId<ReflectFactory<Detector>> DETECTORS =
        new ListPropertyId<ReflectFactory<Detector>>("sched.runner.thread.detectors",
            "Set a list of detectors", new ReflectFactorySelector<Detector>(Detector.class)
                .addArgType(Integer.TYPE).bypassAccessibility())
            .minElements(1)
            .requiredIf(
                ScheduleInstance.DEFAULT_RUNNER.getClazz().isSubClassOf(
                    MultiWorkersScheduleInstance.class)).addDefaultValue("deadlock,long-running")
            .addDefaultValue("long-running");

    protected Detector(@Nonnegative int size) {
    }

    public abstract boolean check(@Nonnull List<Worker> activeWorkers);
  }

  //
  // No detector
  //

  @ImplementationName(iface = Detector.class, name = "none")
  private static class None extends Detector {
    protected None(@Nonnegative  int size) {
      super(size);
    }

    @Override
    public boolean check(@Nonnull List<Worker> activeWorkers) {
      return true;
    }
  }

  //
  // Long running task detector (based on timeout)
  //

  @HasKeyId
  @ImplementationName(iface = Detector.class, name = "long-running")
  private static class LongRunning extends Detector {
    @Nonnull
    private static final LongPropertyId TIMEOUT = LongPropertyId.create(
        "sched.runner.thread.detector.long-running.timeout",
        "Duration allowed by the detector before aborting compilation (in ms)")
        .addDefaultValue("3600000")
        .withMin(0);

    @Nonnull
    private final Logger logger = LoggerFactory.getLogger();

    @Nonnegative
    private final long timeout = ThreadConfig.get(TIMEOUT).longValue();

    @Nonnull
    private final List<Worker> blockedWorkers;

    protected LongRunning(@Nonnegative  int size) {
      super(size);
      blockedWorkers = new ArrayList<Worker>(size);
    }

    @Override
    public boolean check(@Nonnull List<Worker> activeWorkers) {
      // Check for blocked threads
      long time = System.currentTimeMillis();
      Iterator<Worker> iter = activeWorkers.iterator();

      while (iter.hasNext()) {
        Worker worker = iter.next();
        WorkerStatus status = worker.getStatus();

        int duration = (int) (time - status.getCurrentTaskStartOn());
        if (status.getCurrentTask() != null && duration > timeout) {
          iter.remove();
          blockedWorkers.add(worker);
        }
      }

      if (blockedWorkers.size() > 0) {
        if (activeWorkers.size() == 0) {
          dump(blockedWorkers);
        }

        return false;
      }

      return true;
    }

    private void dump(@Nonnull List<Worker> workers) {
      Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();

      logger.log(Level.SEVERE, "Timeout detected during run:");

      boolean atLeastOne = false;
      for (Worker worker : workers) {
        if (worker.isAlive()) {
          WorkerStatus status = worker.getStatus();

          atLeastOne = true;
          logger.log(Level.SEVERE, "  Thread ''{0}'' ({1}) {2}",
              new Object[] {worker.getName(), Long.valueOf(worker.getId()), worker.getState()});
          logger.log(Level.SEVERE, "    Works on {0} during {1} ms", new Object[] {
              status.getCurrentTask(), Integer.valueOf(
                  (int) (System.currentTimeMillis() - status.getCurrentTaskStartOn()))});
          logger.log(Level.SEVERE, "    Stack traces:");

          StackTraceElement[] traces = stackTraces.get(worker);
          if (traces != null) {
            for (StackTraceElement stackTraceElement : traces) {
              logger.log(Level.SEVERE, "      {0}", stackTraceElement);
            }
          } else {
            logger.log(Level.SEVERE, "      no stack traces available");
          }
        }
      }

      if (!atLeastOne) {
        logger.log(Level.SEVERE,
            "  No thread. Wrong detection. Try to increase timeout with ''{0}'' property",
            TIMEOUT.getName());
      }
    }
  }

  //
  // MXBean based deadlock detector
  //

  @ImplementationName(iface = Detector.class, name = "deadlock", filter = DeadLock.Filter.class)
  private static class DeadLock extends Detector {
    private static class Filter implements ImplementationFilter {
      @Override
      public boolean isValid() {
        try {
          ThreadMXBean threadManager = ManagementFactory.getThreadMXBean();
          return threadManager.isSynchronizerUsageSupported()
              && threadManager.isObjectMonitorUsageSupported();
        } catch (Throwable e) {
          return false;
        }
      }
    }

    @Nonnull
    private final Logger logger = LoggerFactory.getLogger();

    @Nonnull
    ThreadMXBean threadManager = ManagementFactory.getThreadMXBean();

    @Nonnull
    private final List<Worker> blockedWorkers;

    protected DeadLock(@Nonnegative int size) {
      super(size);
      blockedWorkers = new ArrayList<Worker>(size);
    }

    @Override
    public boolean check(@Nonnull List<Worker> activeWorkers) {
      long[] deadlockedThreadIds = threadManager.findDeadlockedThreads();

      if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
        if (activeWorkers.size() > 0) {
          // Remove deadlocked threads or thread waiting for deadlocked threads from active workers
          Iterator<Worker> iter = activeWorkers.iterator();
          while (iter.hasNext()) {
            Worker worker = iter.next();

            for (long id : deadlockedThreadIds) {
              if (worker.getId() == id) {
                blockedWorkers.add(worker);
                iter.remove();
                break;
              }

              if (threadManager.getThreadInfo(worker.getId()).getLockOwnerId() == id) {
                blockedWorkers.add(worker);
                iter.remove();
                break;
              }
            }
          }
        }

        if (activeWorkers.size() == 0) {
          assert deadlockedThreadIds != null;
          dump(deadlockedThreadIds, activeWorkers);
        }

        return false;
      }

      return true;
    }

    private void dump(@Nonnull long[] deadlockedThreadIds, @Nonnull List<Worker> activeWorkers) {
      logger.log(Level.SEVERE, "Deadlock detected during run:");

      for (ThreadInfo thread : threadManager.getThreadInfo(deadlockedThreadIds, true, true)) {
        if (thread != null) {
          boolean found = false;

          for (Worker worker : blockedWorkers) {
            if (worker.getId() == thread.getThreadId()) {
              found = true;
              dump(thread, worker);
              break;
            }
          }

          if (!found) {
            dump(thread, null);
          }
        }
      }
    }

    private void dump(@Nonnull ThreadInfo thread, @CheckForNull Worker worker) {
      logger.log(Level.SEVERE, "  Thread ''{0}'' ({1}) {2} {3}", new Object[] {
          thread.getThreadName(), Long.valueOf(thread.getThreadId()), thread.getThreadState(),
          (thread.isInNative() ? "(in native)" : "")});

      if (worker != null) {
        WorkerStatus status = worker.getStatus();

        logger.log(Level.SEVERE, "    Works on {0} during {1} ms", new Object[] {
            status.getCurrentTask(), Integer.valueOf(
                (int) (System.currentTimeMillis() - status.getCurrentTaskStartOn()))});
      }

      String lockName = thread.getLockName();
      if (lockName != null) {
        String lockOwnerName = thread.getLockOwnerName();
        logger.log(Level.SEVERE, "    Blocked on ''{0}'' owned by ''{1}'' ({2})", new Object[] {
            thread.getLockName(), (lockOwnerName != null ? lockOwnerName : "<unknown>"),
            Long.valueOf(thread.getLockOwnerId())});
      }

      LockInfo[] locks = thread.getLockedSynchronizers();
      if (locks.length > 0) {
        logger.log(Level.SEVERE, "    Owned locks:");
        for (LockInfo lock : locks) {
          logger.log(Level.SEVERE, "      {0}", lock);
        }
      }

      logger.log(Level.SEVERE, "    Stack traces:");
      StackTraceElement[] traces = thread.getStackTrace();
      if (traces != null && traces.length > 0) {
        for (StackTraceElement stackTraceElement : traces) {
          logger.log(Level.SEVERE, "      {0}", stackTraceElement);
          for (MonitorInfo monitor : thread.getLockedMonitors()) {
            if (monitor.getLockedStackFrame().equals(stackTraceElement)) {
              logger.log(Level.SEVERE, "      |- locked {0}", monitor);
            }
          }
        }
      } else {
        logger.log(Level.SEVERE, "      no stack traces available");
      }
    }
  }
}
