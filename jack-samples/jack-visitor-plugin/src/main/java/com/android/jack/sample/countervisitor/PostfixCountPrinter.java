/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.sample.countervisitor;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JSession;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reporter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import javax.annotation.Nonnull;

/**
 * A schedulable that prints the number of postfix operations to the user.
 * <p>
 * It uses the {@link CountingMarker} that is added by the {@link PostfixCounter} schedulable to
 * know the number of postfix operations in the IR. This dependency is described by the
 * {@link Constraint} annotation.
 * If that constraint was not described, the {@link PostfixCounter} would not be executed and we
 * would not have access to the {@link CountingMarker}.
 * <p>
 * In order to be executed, a schedulable must either:
 * <ul>
 * <li>generate a {@link com.android.sched.item.Production} declared by the plugin with
 * {@link com.android.jack.plugin.v01.Plugin#getProductions}</li>
 * <li>support a {@link com.android.sched.item.Feature} declared by the plugin with
 * {@link com.android.jack.plugin.v01.Plugin#getFeatures}</li>
 * <li>add something (like a {@link com.android.sched.marker.Marker}) that is needed by another
 * schedulable</li>
 * </ul>
 * While we do not produce anything, we do support the {@link PostfixCountingFeature}. This is
 * described by the {@link Support} annotation.
 * The {@link CounterVisitorPlugin} declares that feature to Jack so that this schedulable is
 * executed. Since we also need the {@link CountingMarker}, this will also execute the
 * {@link PostfixCounter} schedulable before us.
 * <p>
 * This schedulable is also annotated with the mandatory {@link Description} annotation.
 */
@Description("Prints the number of postfix operator in the IR")
@Support(PostfixCountingFeature.class)
@Constraint(need = CountingMarker.class)
public class PostfixCountPrinter implements RunnableSchedulable<JSession> {

  /**
   * This method will process the Jack session that is used for the whole compilation. We just have
   * to extract its {@link CountingMarker} and print a message to the user.
   *
   * @param session the session processed by this schedulable
   * @throws Exception if something wrong happens while executing this schedulable
   */
  @Override
  public void run(@Nonnull JSession session) {
    // Get the CountingMarker from the session.
    CountingMarker marker = session.getMarker(CountingMarker.class);
    assert marker != null;

    // Get the count of postfix operations stored in the marker.
    final int postfixCount = marker.getCounter();

    // To report the count of postfix operations to the user, we use Jack's reporter with our own
    // Reportable message.
    // Since the reporter is more used to report warnings or errors, the user needs to pass the
    // "--verbose info" command-line option to make this message appear.
    Jack.getSession().getReporter().report(Reporter.Severity.NON_FATAL,
            new PostfixCountReportable(postfixCount));
  }

  /**
   * A {@link Reportable} to report the count of postfix operations to the user.
   */
  private static class PostfixCountReportable implements Reportable {
    private final int count;

    public PostfixCountReportable(int count) {
      this.count = count;
    }

    @Nonnull
    @Override
    public String getMessage() {
      return "Number of postfix operators: " + count;
    }

    @Nonnull
    @Override
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.INFO;
    }
  }
}
