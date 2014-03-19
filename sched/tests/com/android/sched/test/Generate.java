/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sched.test;

import java.io.FileNotFoundException;
import java.io.PrintStream;

// TODO(jplesot) Update
public class Generate {
  static void generateTags(int nb) throws FileNotFoundException {
    for (int i = 0; i < nb; i++) {
      generateTag(i);
    }
  }

  static void generateProductions(int nb) throws FileNotFoundException {
    for (int i = 0; i < nb; i++) {
      generateProduction(i);
    }
  }

  static void generateSchedulables(int nb) throws FileNotFoundException {
    for (int i = 0; i < nb; i++) {
      generateSchedulable(i, i);
    }
  }

  private static void generateTag(int i) throws FileNotFoundException {
    PrintStream out = new PrintStream("Tag" + i + ".java");

    out.println("// Generated file");
    out.println();
    out.println("package com.google.sched.test;");
    out.println();
    out.println("import com.google.sched.item.Description;");
    out.println("import com.google.sched.item.Name;");
    out.println("import com.google.sched.tag.Tag;");
    out.println();
    out.println("@Name(\"Tag " + i + "\")");
    out.println("@Description(\"Tag " + i + " description\")");
    out.println("public class Tag" + i + " implements Tag {");
    out.println("}");

    out.close();
  }

  private static void generateProduction(int i) throws FileNotFoundException {
    PrintStream out = new PrintStream("Production" + i + ".java");

    out.println("// Generated file");
    out.println();
    out.println("package com.google.sched.test;");
    out.println();
    out.println("import com.google.sched.item.Description;");
    out.println("import com.google.sched.item.Name;");
    out.println("import com.google.sched.production.Production;");
    out.println();
    out.println("@Name(\"Production " + i + "\")");
    out.println("@Description(\"Production " + i + " description\")");
    out.println("public class Production" + i + " implements Production {");
    out.println("}");

    out.close();
  }

  private static void generateSchedulable(int i, int tag) throws FileNotFoundException {
    PrintStream out = new PrintStream("Schedulable" + i + ".java");

    out.println("// Generated file");
    out.println();
    out.println("package com.google.sched.test;");
    out.println();
    out.println("import com.google.sched.item.Description;");
    out.println("import com.google.sched.item.Name;");
    out.println("import com.google.sched.schedulable.Transform;");
    out.println("import com.google.sched.scheduler.SchedulableInterface;");
    out.println("import com.google.sched.scheduler.SchedulableTest;");
    out.println();
    out.println("@Name(\"Schedulable " + i + "\")");
    out.println("@Description(\"Schedulable " + i + " description\")");
    out.println("@Transform(add = Tag" + tag + ".class)");
    out.println("public class Schedulable" + i
        + " extends SchedulableTest implements SchedulableInterface {");
    out.println("  public void run(Void v) throws Exception {");
    out.println("    add(Tag" + i + ".class);");
    out.println("  }");
    out.println("}");

    out.close();
  }


  public static void main(String[] args) throws FileNotFoundException {
    generateTags(250);
    generateProductions(5);
    generateSchedulables(250);
  }
}
