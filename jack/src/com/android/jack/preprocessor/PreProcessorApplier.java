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

import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputVFile;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
      InputStreamFile inputFile = ThreadConfig.get(PreProcessor.FILE);
      InputStream inputStream = inputFile.getInputStream();
      try {
        rules.addAll(parseRules(session, inputStream, inputFile.getLocation()));
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          // nothing to handle for inputs
        }
      }
    }

    for (Iterator<InputLibrary> iter = session.getPathSources(); iter.hasNext();) {
      InputLibrary inputLibrary = iter.next();
      Iterator<InputVFile> metaFileIt = inputLibrary.iterator(FileType.JPP);
      while (metaFileIt.hasNext()) {
        InputVFile inputFile = metaFileIt.next();
        InputStream inputStream = inputFile.getInputStream();
        try {
          rules.addAll(parseRules(session, inputStream, inputFile.getLocation()));
        } finally {
          try {
            inputStream.close();
          } catch (IOException e) {
            // nothing to handle for inputs
          }
        }
      }
    }

    applyRules(rules, session);
  }

  @Nonnull
  private Collection<Rule> parseRules(
      @Nonnull JSession session,
      @Nonnull InputStream inputStream,
      @Nonnull Location location) throws IOException, RecognitionException {
    ANTLRInputStream in = new ANTLRInputStream(inputStream);
    PreProcessorLexer lexer = new PreProcessorLexer(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    PreProcessorParser parser = new PreProcessorParser(tokens);

    return parser.rules(session, location);
  }

  private void applyRules(@Nonnull Collection<Rule> rules,
      @Nonnull JSession session) {
    Scope scope = new TypeToEmitScope(session);
    Collection<AddAnnotationStep> requests = new ArrayList<AddAnnotationStep>();
    for (Rule rule : rules) {
      Context context = new Context(rule);
      if (!rule.getSet().eval(scope, context).isEmpty()) {
        requests.addAll(context.getSteps());
      }
    }

    Map<Entry, Rule> map = new HashMap<Entry, Rule>();
    for (AddAnnotationStep request : requests) {
      request.apply(map);
    }
  }

  static class Entry {
    @Nonnull
    public final Annotable annotated;
    @Nonnull
    public final JAnnotationType annotationType;

    public Entry(@Nonnull Annotable annotated, @Nonnull JAnnotationType annotationType) {
      this.annotated = annotated;
      this.annotationType = annotationType;
    }

    @Override
    public final boolean equals(Object obj) {
      if (obj instanceof Entry) {
        Entry entry = (Entry) obj;

        return entry.annotated == annotated
            && entry.annotationType.equals(annotationType);
      }

      return false;
    }

    @Override
    public int hashCode() {
      return annotated.hashCode() ^ annotationType.hashCode();
    }
  }
}
