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
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.ProcessorSchedulable;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.ReaderFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * @param <T> the root <i>data</i> type
 */
@ImplementationName(iface = Planner.class, name = "deserializer")
public class DeserializerPlanner<T extends Component> implements Planner<T> {

  @Nonnull
  private final ReaderFile planFile = ThreadConfig.get(PlannerFactory.PLANNER_FILE);

  @SuppressWarnings("unchecked")
  @Nonnull
  @Override
  public Plan<T> buildPlan(@Nonnull Request request, @Nonnull Class<T> rootRunOn)
      throws PlanNotFoundException {
    PlanBuilder<T> builder = new PlanBuilder<T>(request, rootRunOn);
    BufferedReader reader = null;
    try {
      try {
        reader = new BufferedReader(planFile.getBufferedReader());
        Stack<SubPlanBuilder<T>> subPlanStack = new Stack<SubPlanBuilder<T>>();
        subPlanStack.add(builder);
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          line = line.trim();
          if (line.equals("}")) {
            subPlanStack.pop();
          } else if (line.equals("{")) {
            // nothing to do
          } else {
            Class<?> runnableClass = this.getClass().getClassLoader().loadClass(line);
            if (AdapterSchedulable.class.isAssignableFrom(runnableClass)) {
              SubPlanBuilder<T> subPlanBuilder = subPlanStack.peek();
              SubPlanBuilder<T> newSubPlanBuilder = subPlanBuilder.appendSubPlan(
                  (Class<? extends AdapterSchedulable<T, T>>) runnableClass);
              subPlanStack.push(newSubPlanBuilder);
            } else {
              SubPlanBuilder<T> subPlanBuilder = subPlanStack.peek();
              subPlanBuilder.append((Class<? extends ProcessorSchedulable<T>>) runnableClass);
            }
          }
        }
      } finally {
        if (reader != null) {
          reader.close();
        }
      }
    } catch (IOException e) {
      throw new PlanNotFoundException("Error reading plan from file", e);
    } catch (ClassNotFoundException e) {
      throw new PlanNotFoundException("Error instantiating schedulable from plan file", e);
    }

    Plan<T> plan = builder.getPlan();

    return plan;
  }

}
