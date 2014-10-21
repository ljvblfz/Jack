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
import com.android.sched.vfs.InputRootVDir;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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

    Collection<Rule> rules = new ArrayList<Rule>();

    if (ThreadConfig.get(PreProcessor.ENABLE).booleanValue()) {
      InputStreamFile input = ThreadConfig.get(PreProcessor.FILE);
      InputStream inputStream = input.getInputStream();
      try {
        rules.addAll(parseRules(session, inputStream));
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          // nothing to handle for inputs
        }
      }
    }

    for (Iterator<InputRootVDir> iter = session.getPathSources(); iter.hasNext();) {
      InputRootVDir dir = iter.next();
      for (InputVElement sub : dir.list()) {
        if (sub.getName().equals("JACK-INF") && sub.isVDir()) {
          for (InputVElement inf : ((InputVDir) sub).list()) {
            if (inf.getName().endsWith(".jpp") && !inf.isVDir()) {
              InputStream inputStream = ((InputVFile) inf).openRead();
              try {
                rules.addAll(parseRules(session, inputStream));
              } finally {
                try {
                  inputStream.close();
                } catch (IOException e) {
                  // nothing to handle for inputs
                }
              }
            }
          }
          break;
        }
      }
    }

    applyRules(rules, session);
  }

  @Nonnull
  private Collection<Rule> parseRules(
      @Nonnull JSession session,
      @Nonnull InputStream inputStream) throws IOException, RecognitionException {
    ANTLRInputStream in = new ANTLRInputStream(inputStream);
    PreProcessorLexer lexer = new PreProcessorLexer(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    PreProcessorParser parser = new PreProcessorParser(tokens);
    return parser.rules(session);
  }

  private void applyRules(@Nonnull Collection<Rule> rules,
      @Nonnull JSession session) {
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
  }

}
