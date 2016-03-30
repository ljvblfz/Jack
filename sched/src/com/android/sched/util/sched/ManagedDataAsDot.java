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

package com.android.sched.util.sched;

import com.android.sched.item.Feature;
import com.android.sched.item.Item;
import com.android.sched.item.AbstractItemManager;
import com.android.sched.item.Items;
import com.android.sched.item.ManagedItem;
import com.android.sched.item.Production;
import com.android.sched.marker.ManagedMarker;
import com.android.sched.marker.Marker;
import com.android.sched.marker.MarkerManager;
import com.android.sched.marker.MarkerNotConformException;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.ManagedSchedulable;
import com.android.sched.scheduler.ManagedVisitor;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.codec.PathCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Graphviz (dot) graph generator of {@code Schedulable}s, {@code Tag}s, {@code Marker}s,
 * {@code Component}s, {@code Feature}s and {@code Production}s.
 */
@ImplementationName(iface = ManagedDataListener.class, name = "dot")
@HasKeyId
public class ManagedDataAsDot implements ManagedDataListener {
  @Nonnull
  private static final PropertyId<File> DOT_FILE = PropertyId.create(
      "sched.dotfile", "Define in which file generates data as dot",
      new PathCodec()).addDefaultValue("schedlib.dot");

  @Nonnull
  private final PrintWriter out;

  @Nonnull
  private final Map<Class<? extends Item>, Class<?>> itemOnlyUsedOnType =
      new HashMap<Class<? extends Item>, Class<?>>();
  @Nonnull
  private final Map<Class<?>, ManagedRunnable> typeAtLeastUseBy =
      new HashMap<Class<?>, ManagedRunnable>();
  @Nonnull
  private final List<ManagedVisitor> visitorList = new LinkedList<ManagedVisitor>();

  @Nonnegative
  private int closeIfZero;

  @Nonnegative
  private int     nbItemManagerOpen = 0;
  private boolean noMoreItemManager = false;
  private boolean noMoreManagedSchedulable = false;

  public ManagedDataAsDot() throws IOException {
    Logger logger = LoggerFactory.getLogger();
    File   file   = ThreadConfig.get(ManagedDataAsDot.DOT_FILE);

    try {
      this.out = new PrintWriter(new BufferedWriter(new FileWriter(file), 102400));

      out.println("digraph g {");
      out.println("  compound=true;");
      out.println("  graph [");
      out.println("    rankdir = TD");
      out.println("  ];");
      out.println("  node [");
      out.println("    fontsize = 16");
      out.println("    shape = ellipse");
      out.println("  ];");
      out.println("  edge [");
      out.println("  ];");
      out.println();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to open graph file '" + file.getAbsolutePath() + "'", e);

      throw e;
    }

    closeIfZero = 2; // ManagedSchedulable & ItemManager
  }

  @Override
  public void notifyNewItemManager(@Nonnull AbstractItemManager itemManager) {
    assert !noMoreItemManager;

    nbItemManagerOpen++;
    closeIfZero++;
  }

  @Override
  public void notifyNoMoreItemManager() {
    assert !noMoreItemManager;

    noMoreItemManager = true;
    closeIfZero--;
    closeIfZero();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void notifyNewManagedItem(@Nonnull ManagedItem item) {
    assert nbItemManagerOpen > 0;

    if (Marker.class.isAssignableFrom(item.getItem())) {
      try {
        addManagedMarker(new ManagedMarker((Class<? extends Marker>) item.getItem()));
      } catch (MarkerNotConformException e) {
        addManagedMarkerError(item);
      }
    } else {
      addManagedItem(item);
    }
  }

  @Override
  public void notifyNoMoreManagedItem(@Nonnull Class<? extends Item> type) {
    assert nbItemManagerOpen > 0;

    nbItemManagerOpen--;
    closeIfZero--;
    closeIfZero();
  }

  @Override
  public void notifyNewManagedSchedulable(@Nonnull ManagedSchedulable schedulable) {
    assert !noMoreManagedSchedulable;

    if (schedulable instanceof ManagedRunnable) {
      addManagedRunnable((ManagedRunnable) schedulable);
    } else if (schedulable instanceof ManagedVisitor) {
      addManagedVisitor((ManagedVisitor) schedulable);
    } else {
      throw new AssertionError();
    }
  }

  @Override
  public void notifyNoMoreManagedSchedulable() {
    assert !noMoreManagedSchedulable;

    noMoreManagedSchedulable = true;
    closeIfZero--;
    closeIfZero();
  }

  private void closeIfZero() {
    if (closeIfZero == 0) {
      close();
    }
  }

  private void addManagedMarker(@Nonnull ManagedMarker marker) {
    out.println("\"" + marker.getMarker().getCanonicalName() + "\"");
    out.println("[");
    out.println("  shape=none,");
    out.println("  margin=0,");
    out.println("  label =<");
    out.println("    <TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
    out.println("      <TR><TD PORT=\"header\" BGCOLOR=\"black\"><FONT COLOR=\"white\">"
        + marker.getName() + "</FONT></TD></TR>");

    for (Class<? extends MarkerManager> cls : marker.getStaticValidOn()) {
      out.println("      <TR><TD>" + cls.getSimpleName() + "</TD></TR>");
    }

    for (ManagedMarker.InternalDynamicValidOn dvo : marker.getDynamicValidOn()) {
      out.println("      <TR><TD><FONT COLOR=\"#aaaaaa\">" + dvo.getValidOn().getSimpleName()
          + "</FONT></TD></TR>");
    }

    out.println("    </TABLE>>");
    out.println("];");
    out.println();
  }

  private void addManagedMarkerError(@Nonnull ManagedItem item) {
    out.println("\"" + item.getItem().getCanonicalName() + "\"");

    out.println("[");
    out.println("  shape = box,");
    out.println("  style = filled,");
    out.println("  color = red,");
    out.println("  fillcolor = red,");
    out.println("  fontcolor = black,");
    out.println("  label = \"" + item.getName() + "\",");
    out.println("];");
    out.println();
  }

  private void addManagedItem(@Nonnull ManagedItem item) {
    if (Feature.class.isAssignableFrom(item.getItem())) {
      return;
    }

    out.println("\"" + item.getItem().getCanonicalName() + "\"");

    out.println("[");
    out.println("  shape = box,");
    out.println("  style = filled,");
    if (Production.class.isAssignableFrom(item.getItem())) {
      out.println("  color = gold3,");
      out.println("  fillcolor = gold3,");
      out.println("  fontcolor = black,");
    } else {
      out.println("  color = black,");
      out.println("  fillcolor = black,");
      out.println("  fontcolor = white,");
    }
    out.println("  label = \"" + item.getName() + "\",");
    out.println("];");
    out.println();
  }

  private void addManagedRunnable(@Nonnull ManagedRunnable runnable) {
    if (typeAtLeastUseBy.get(runnable.getRunOn()) == null) {
      typeAtLeastUseBy.put(runnable.getRunOn(), runnable);
    }

    out.println("subgraph \"cluster_" + runnable.getRunOn().getCanonicalName() + "\"");
    out.println("{");
    out.println("  label=\"" + runnable.getRunOn().getSimpleName() + "\";");
    out.println("  color=blue;");
    out.println("  fontcolor = blue;");
    out.println("  \"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
    out.println("  [");
    out.println("  shape=none,");
    out.println("  margin=0,");
    out.println("  label =<");
    out.println("    <TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
    out.println("      <TR><TD BGCOLOR=\"white\"><FONT COLOR=\"black\">" + runnable.getName()
        + "</FONT></TD></TR>");

    for (Class<? extends Feature> cls : runnable.getSupportedFeatures()) {
      out.println(
          "      <TR><TD><FONT COLOR=\"black\">" + Items.getName(cls) + "</FONT></TD></TR>");
    }

    FeatureSet all = new FeatureSet(runnable.getSupportedFeatures());
    all.clear();
    for (FeatureSet features : runnable.getOptionalFeatures()) {
      all.addAll(features);
    }

    for (Class<? extends Feature> feature : all) {
      out.println(
          "      <TR><TD><FONT COLOR=\"grey\">" + Items.getName(feature) + "</FONT></TD></TR>");
    }

    out.println("    </TABLE>>");
    out.println("  ];");
    out.println("}");
    out.println();

    for (Class<? extends Item> item : runnable.getAddedTags()) {
      out.print("\"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
      out.print(" -> ");
      out.println("\"" + item.getCanonicalName() + "\"");
      out.println("[");
      out.println("  arrowhead=normal,");
      out.println("  color=green4");
      out.println("];");
      out.println();

      if (itemOnlyUsedOnType.get(item) != null) {
        if (itemOnlyUsedOnType.get(item) != runnable.getRunOn()) {
          itemOnlyUsedOnType.remove(item);
          itemOnlyUsedOnType.put(item, null);
        }
      } else {
        itemOnlyUsedOnType.put(item, runnable.getRunOn());
      }
    }

    for (Class<? extends Item> item : runnable.getRemovedTags()) {
      out.print("\"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
      out.print(" -> ");
      out.println("\"" + item.getCanonicalName() + "\"");
      out.println("[");
      out.println("  arrowhead=normal,");
      out.println("  color=red4");
      out.println("];");
      out.println();

      if (itemOnlyUsedOnType.get(item) != null) {
        if (itemOnlyUsedOnType.get(item) != runnable.getRunOn()) {
          itemOnlyUsedOnType.remove(item);
          itemOnlyUsedOnType.put(item, null);
        }
      } else {
        itemOnlyUsedOnType.put(item, runnable.getRunOn());
      }
    }

    for (Class<? extends Item> item : runnable.getDefaultNeededTags()) {
      out.print("\"" + item.getCanonicalName() + "\"");
      out.print(" -> ");
      out.println("\"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
      out.println("[");
      out.println("  arrowhead=normal,");
      out.println("  style=dashed,");
      out.println("  color=green4");
      out.println("];");
      out.println();

      if (itemOnlyUsedOnType.get(item) != null) {
        if (itemOnlyUsedOnType.get(item) != runnable.getRunOn()) {
          itemOnlyUsedOnType.remove(item);
          itemOnlyUsedOnType.put(item, null);
        }
      } else {
        itemOnlyUsedOnType.put(item, runnable.getRunOn());
      }
    }

    for (Class<? extends Item> item : runnable.getDefaultUnsupportedTags()) {
      out.print("\"" + item.getCanonicalName() + "\"");
      out.print(" -> ");
      out.println("\"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
      out.println("[");
      out.println("  arrowhead=normal,");
      out.println("  style=dashed,");
      out.println("  color=red4");
      out.println("];");
      out.println();

      if (itemOnlyUsedOnType.get(item) != null) {
        if (itemOnlyUsedOnType.get(item) != runnable.getRunOn()) {
          itemOnlyUsedOnType.remove(item);
          itemOnlyUsedOnType.put(item, null);
        }
      } else {
        itemOnlyUsedOnType.put(item, runnable.getRunOn());
      }
    }

    for (FeatureSet features : runnable.getOptionalFeatures()) {
      for (Class<? extends Item> item : runnable.getNeededTags(features)) {
        out.print("\"" + item.getCanonicalName() + "\"");
        out.print(" -> ");
        out.println("\"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
        out.println("[");
        out.println("  arrowhead=normal,");
        out.println("  style=dotted,");
        out.println("  label=\"" + features + "\"");
        out.println("  color=green4");
        out.println("];");
        out.println();

        if (itemOnlyUsedOnType.get(item) != null) {
          if (itemOnlyUsedOnType.get(item) != runnable.getRunOn()) {
            itemOnlyUsedOnType.remove(item);
            itemOnlyUsedOnType.put(item, null);
          }
        } else {
          itemOnlyUsedOnType.put(item, runnable.getRunOn());
        }
      }

      for (Class<? extends Item> item : runnable.getUnsupportedTags(features)) {
        out.print("\"" + item.getCanonicalName() + "\"");
        out.print(" -> ");
        out.println("\"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
        out.println("[");
        out.println("  arrowhead=normal,");
        out.println("  style=dotted,");
        out.println("  label=\"" + features + "\"");
        out.println("  color=red4");
        out.println("];");
        out.println();

        if (itemOnlyUsedOnType.get(item) != null) {
          if (itemOnlyUsedOnType.get(item) != runnable.getRunOn()) {
            itemOnlyUsedOnType.remove(item);
            itemOnlyUsedOnType.put(item, null);
          }
        } else {
          itemOnlyUsedOnType.put(item, runnable.getRunOn());
        }
      }
    }

    for (Class<? extends Item> item : runnable.getProductions()) {
      out.print("\"" + runnable.getRunnableSchedulable().getCanonicalName() + "\"");
      out.print(" -> ");
      out.println("\"" + item.getCanonicalName() + "\"");
      out.println("[");
      out.println("  arrowhead=normal,");
      out.println("  color=gold3");
      out.println("];");
      out.println();

      if (itemOnlyUsedOnType.get(item) != null) {
        if (itemOnlyUsedOnType.get(item) != runnable.getRunOn()) {
          itemOnlyUsedOnType.remove(item);
          itemOnlyUsedOnType.put(item, null);
        }
      } else {
        itemOnlyUsedOnType.put(item, runnable.getRunOn());
      }
    }
  }

  private void addManagedVisitor(@Nonnull ManagedVisitor visitor) {
    visitorList.add(visitor);
  }

  private void close() {
    for (Entry<Class<? extends Item>, Class<?>> entry : itemOnlyUsedOnType.entrySet()) {
      if (entry.getValue() != null) {
        out.println("subgraph \"cluster_" + entry.getValue().getCanonicalName() + "\"");
        out.println("{");
        out.println("  \"" + entry.getKey().getCanonicalName() + "\"");
        out.println("}");
        out.println();
      }
    }

    for (ManagedVisitor visitor : visitorList) {
      out.println("\"" + visitor.getVisitorSchedulable().getCanonicalName() + "\"");
      out.println("[");
      out.println("  shape=hexagon,");
      out.println("  color=blue,");
      out.println("  fontcolor=blue,");
      out.println("  label=\"" + visitor.getName() + "\"");
      out.println("];");
      out.println();

      if (typeAtLeastUseBy.get(visitor.getRunOn()) == null) {
        out.println("subgraph \"cluster_" + visitor.getRunOn().getCanonicalName() + "\"");
        out.println("{");
        out.println("  label=\"" + visitor.getRunOn().getSimpleName() + "\";");
        out.println("  color=blue;");
        out.println("  fontcolor = blue;");
        out.println("  \"" + visitor.getRunOn().getCanonicalName() + "\"");
        out.println("  [");
        out.println("    shape=point");
        out.println("  ];");
        out.println("}");
        out.println();
      }

      if (typeAtLeastUseBy.get(visitor.getRunOn()) != null) {
        out.print("\"" + typeAtLeastUseBy.get(visitor.getRunOn())
            .getRunnableSchedulable().getCanonicalName() + "\"");
      } else {
        out.print("\"" + visitor.getRunOn().getCanonicalName() + "\"");
      }
      out.print(" -> ");
      out.println("\"" + visitor.getVisitorSchedulable().getCanonicalName() + "\"");
      out.println("[");
      out.println("  arrowhead=normal,");
      out.println("  color=blue,");
      out.println("  ltail=\"cluster_" + visitor.getRunOn().getCanonicalName() + "\"");
      out.println("];");
      out.println();

      if (typeAtLeastUseBy.get(visitor.getRunOnAfter()) == null) {
        out.println("subgraph \"cluster_" + visitor.getRunOnAfter().getCanonicalName() + "\"");
        out.println("{");
        out.println("  label=\"" + visitor.getRunOnAfter().getSimpleName() + "\";");
        out.println("  color=blue;");
        out.println("  fontcolor = blue;");
        out.println("  \"" + visitor.getRunOnAfter().getCanonicalName() + "\"");
        out.println("  [");
        out.println("    shape=point,");
        out.println("  ];");
        out.println("}");
        out.println();
      }

      out.print("\"" + visitor.getVisitorSchedulable().getCanonicalName() + "\"");
      out.print(" -> ");
      if (typeAtLeastUseBy.get(visitor.getRunOnAfter()) != null) {
        out.println("\"" + typeAtLeastUseBy.get(visitor.getRunOnAfter())
            .getRunnableSchedulable().getCanonicalName() + "\"");
      } else {
        out.println("\"" + visitor.getRunOnAfter().getCanonicalName() + "\"");
      }
      out.println("[");
      out.println("  arrowhead=normal,");
      out.println("  color=blue,");
      out.println("  lhead=\"cluster_" + visitor.getRunOnAfter().getCanonicalName() + "\"");
      out.println("];");
      out.println();
    }

    out.println("}");
    out.close();
  }
}
