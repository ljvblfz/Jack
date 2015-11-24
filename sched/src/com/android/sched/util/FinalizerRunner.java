/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sched.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Allows to run actions when an object is reclaimed by the garbage collection.
 */
public class FinalizerRunner {
  @Nonnull
  private static final Logger logger = Logger.getLogger(FinalizerRunner.class.getName());

  private class FinalizerThread extends Thread {
    private FinalizerThread(@Nonnull String name) {
      super(name);
    }

    @Override
    public void run() {
      boolean isRunning = true;
      try {
        while (isRunning) {
          logger.log(Level.FINE, "Finalizer thread " + getName() + " pulling for next deleter");
          FinalizerReference removedReference = (FinalizerReference) queue.remove();
          synchronized (referenceList) {
            boolean removed = referenceList.remove(removedReference);
            assert removed;
            if (referenceList.isEmpty()) {
              isRunning = false;
              thread = null;
            }
          }
          removedReference.run();
        }
      } catch (InterruptedException e) {
        logger.log(Level.FINE, "Finalizer thread " + getName() + " was interrupted");
        FinalizerReference removedReference = (FinalizerReference) queue.poll();
        while (removedReference != null) {
          removedReference.run();
          removedReference = (FinalizerReference) queue.poll();
        }
        interrupt();
      }
    }
  }

  private static class FinalizerReference extends PhantomReference<Object> implements Runnable {

    @Nonnull
    private final Runnable finalizer;

    public FinalizerReference(@Nonnull Object referent, @Nonnull ReferenceQueue<Object> queue,
        @Nonnull Runnable finalizer) {
      super(referent, queue);
      this.finalizer = finalizer;
    }

    @Override
    public void run() {
      finalizer.run();
    }

    @Override
    public String toString() {
      return "Finalizer for " + finalizer;
    }
  }

  @Nonnull
  private final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

  /*
   * Protects PhantomReference from garbage collection.
   */
  @Nonnull
  private final List<PhantomReference<Object>> referenceList =
    new LinkedList<PhantomReference<Object>>();

  @CheckForNull
  private Thread thread;

  @Nonnull
  private final String name;

  private boolean shutDown = false;

  public FinalizerRunner(@Nonnull String name) {
    this.name = name;
  }

  public void registerFinalizer(@Nonnull final Runnable finalizer, @Nonnull Object watched) {
    synchronized (referenceList) {
      assert !shutDown;
      if (thread == null) {
        Thread finalizerThread = new FinalizerThread(name);
        finalizerThread.setDaemon(true);
        thread = finalizerThread;
        finalizerThread.start();
      }
      FinalizerReference reference = new FinalizerReference(watched, queue, finalizer);
      referenceList.add(reference);
    }
  }

  public void shutdown() {
    synchronized (referenceList) {
      shutDown = true;
      Thread finalizerThread = thread;
      if (finalizerThread != null) {
        finalizerThread.interrupt();
      }
    }
  }
}