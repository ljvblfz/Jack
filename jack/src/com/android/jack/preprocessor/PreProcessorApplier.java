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

package com.android.jack.preprocessor;

import com.android.jack.ir.ast.JSession;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.log.LoggerFactory;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} applies the rules defined in the PreProcessor file.
 */
@Description("Apply the rules defined in the PreProcessor file.")
@Support(PreProcessor.class)
public class PreProcessorApplier implements RunnableSchedulable<JSession> {

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    InputStreamFile input = ThreadConfig.get(PreProcessor.FILE);
    InputStream inputStream = input.getInputStream();
    ANTLRInputStream in = new ANTLRInputStream(inputStream);
    try {
    PreProcessorLexer lexer = new PreProcessorLexer(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    PreProcessorParser parser = new PreProcessorParser(tokens);
    Collection<Rule> rules = parser.rules(session);
    Scope scope = new TypeToEmitScope(session);
    List<TransformationRequest> requests = new ArrayList<TransformationRequest>(rules.size());
    for (Rule rule : rules) {
      Context context = new Context();
      if (!rule.getSet().eval(scope, context).isEmpty()) {
        requests.add(context.getRequest(session));
      }
    }

    for (TransformationRequest request : requests) {
      request.commit();
    }
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Failed to close input stream on '"
            + input.getLocation().getDescription() + "'", e);
      }
    }

  }

}
