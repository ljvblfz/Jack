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

package com.android.sched.util.sched;

import com.google.common.collect.Iterators;

import com.android.sched.item.AbstractComponent;
import com.android.sched.item.Component;
import com.android.sched.item.Feature;
import com.android.sched.item.Item;
import com.android.sched.item.ItemManager;
import com.android.sched.item.Items;
import com.android.sched.item.ManagedItem;
import com.android.sched.item.Production;
import com.android.sched.item.Tag;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.marker.ManagedMarker;
import com.android.sched.marker.ManagedMarker.InternalDynamicValidOn;
import com.android.sched.marker.Marker;
import com.android.sched.marker.MarkerNotConformException;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.ManagedSchedulable;
import com.android.sched.scheduler.ManagedVisitor;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.print.DataModel;
import com.android.sched.util.print.DataModelList;
import com.android.sched.util.print.DataModelListAdapter;
import com.android.sched.util.print.DataType;
import com.android.sched.util.print.DataView;
import com.android.sched.util.print.DataViewBuilder;
import com.android.sched.util.print.Printer;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Report generator of {@code Schedulable}s, {@code Tag}s, {@code Marker}s,
 * {@code Component}s, {@code Feature}s and {@code Production}s.
 */
@ImplementationName(iface = ManagedDataListener.class, name = "text")
@HasKeyId
public class ManagedDataAsReport implements ManagedDataListener {
  @Nonnull
  private static final ReflectFactoryPropertyId<Printer> PRINTER = ReflectFactoryPropertyId
      .create("sched.report.format", "Define which format to use", Printer.class)
      .addArgType(PrintStream.class).addDefaultValue("text")
      .requiredIf(ManagedDataListenerFactory.DATA_LISTENER.getClazz()
          .isSubClassOf(ManagedDataAsReport.class));

  @Nonnull
  public static final PropertyId<OutputStreamFile> STREAM = PropertyId
      .create("sched.report.file", "The file where to print the report",
          new OutputStreamCodec(Existence.MAY_EXIST).allowStandardOutputOrError())
      .addDefaultValue("-").requiredIf(ManagedDataListenerFactory.DATA_LISTENER.getClazz()
          .isSubClassOf(ManagedDataAsReport.class));

  @Nonnegative
  private int closeIfZero;

  @Nonnegative
  private int     nbItemManagerOpen = 0;
  private boolean noMoreItemManager = false;
  private boolean noMoreManagedSchedulable = false;

  public ManagedDataAsReport() {
    closeIfZero = 2; // ManagedSchedulable & ItemManager
  }

  @Override
  public void notifyNewItemManager(@Nonnull ItemManager itemManager) {
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
    } else if (Tag.class.isAssignableFrom(item.getItem())) {
      addManagedTag(item);
    } else if (AbstractComponent.class.isAssignableFrom(item.getItem())) {
      addManagedComponent(item);
    } else if (Production.class.isAssignableFrom(item.getItem())) {
      addManagedProduction(item);
    } else if (Feature.class.isAssignableFrom(item.getItem())) {
      addManagedFeature(item);
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
    assert!noMoreManagedSchedulable;

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

  /*
   * List of Ids
   */

  @Nonnull
  private static final DataView ID_VIEW = DataViewBuilder.getStructure()
      .addField("id", DataType.STRING)
      .build();

  private static class IdList extends DataModelListAdapter<Class<? extends Item>> {
    public IdList(@Nonnull final Class<? extends Item> category) {
      super(new Converter<Class<? extends Item>>() {
        @Override
        @Nonnull
        public DataModel apply(final Class<? extends Item> data) {
          return new DataModel() {
            @Override
            public Iterator<Object> iterator() {
              return Iterators.<Object>forArray(getId(data, category));
            }

            @Override
            @Nonnull
            public DataView getDataView() {
              return ID_VIEW;
            }
          };
        }
      });
    }
  }

  /*
   * Markers
   */

  private static class ManagedMarkerModel implements DataModel {
    @Nonnull
    private static final DataView MARKER_VIEW = DataViewBuilder.getStructure()
        .addField("name", DataType.STRING)
        .addField("id", DataType.STRING)
        .addField("description", DataType.STRING)
        .addField("sValidOn", DataType.LIST)
        .addField("dValidOn", DataType.LIST)
        .build();

    @Nonnull
    private static final DataView ID_METHOD_VIEW = DataViewBuilder.getStructure()
        .addField("id", DataType.STRING)
        .addField("method", DataType.STRING)
        .build();

    @Nonnull
    private final ManagedMarker marker;

    public ManagedMarkerModel(@Nonnull ManagedMarker marker) {
      this.marker = marker;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Object> iterator() {
      return Iterators.forArray(
          // name
          marker.getName(),
          // id
          getId(marker.getMarker(), Marker.class),
          // description
          marker.getDescription(),
          // sValidOn
          new IdList(Marker.class).addAll((Class<? extends Item>[]) marker.getStaticValidOn()),
          // dValidOn
          new DataModelListAdapter<InternalDynamicValidOn>(
              new DataModelListAdapter.Converter<InternalDynamicValidOn>() {
                @Override
                @Nonnull
                public DataModel apply(final InternalDynamicValidOn data) {
                  return new DataModel() {
                    @Override
                    public Iterator<Object> iterator() {
                      return Iterators.<Object>forArray(getId(data.getValidOn(), Marker.class),
                          data.getMethod().getDeclaringClass().getCanonicalName() + "#"
                              + data.getMethod().getName());
                }

                @Override
                @Nonnull
                public DataView getDataView() {
                  return ID_METHOD_VIEW;
                }};
            }}).addAll(marker.getDynamicValidOn()));
    }

    @Override
    @Nonnull
    public DataView getDataView() {
      return MARKER_VIEW;
    }
  }

  @Nonnull
  private final DataModelList markers = new DataModelList();

  private void addManagedMarker(@Nonnull final ManagedMarker marker) {
    markers.add(new ManagedMarkerModel(marker));
  }

  private void addManagedMarkerError(@Nonnull ManagedItem item) {
  }

  /*
   * ToCoPoF
   */

  private static class ManagedItemModel implements DataModel {
    private static final DataView ITEM_VIEW = DataViewBuilder.getStructure()
        .addField("name", DataType.STRING)
        .addField("id", DataType.STRING)
        .addField("description", DataType.STRING)
        .addField("composedOfId", DataType.LIST).build();

    @Nonnull
    private final ManagedItem item;
    @Nonnull
    private final Class<? extends Item> category;

    public ManagedItemModel(@Nonnull ManagedItem item, @Nonnull Class<? extends Item> catagory) {
      this.item = item;
      this.category = catagory;
    }

    @Override
    public Iterator<Object> iterator() {
      return Iterators.forArray(
          // name
          item.getName(),
          // id
          getId(item.getItem(), category),
          // description
          item.getDescription(),
          // composedof
          new IdList(category).addAll(Items.getComposedOf(item.getItem())));
    }

    @Override
    @Nonnull
    public DataView getDataView() {
      return ITEM_VIEW;
    }
  }

  /* Tag */

  @Nonnull
  private final DataModelList tags = new DataModelList();

  private void addManagedTag(@Nonnull ManagedItem item) {
    tags.add(new ManagedItemModel(item, Tag.class));
  }

  /* Component */

  @Nonnull
  private final DataModelList components = new DataModelList();

  private void addManagedComponent(@Nonnull ManagedItem item) {
    components.add(new ManagedItemModel(item, Component.class));
  }

  /* Production */

  @Nonnull
  private final DataModelList productions = new DataModelList();

  private void addManagedProduction(@Nonnull ManagedItem item) {
    productions.add(new ManagedItemModel(item, Production.class));
  }

  /* Feature */

  @Nonnull
  private final DataModelList features = new DataModelList();

  private void addManagedFeature(@Nonnull ManagedItem item) {
    features.add(new ManagedItemModel(item, Feature.class));
  }

  /*
   * Schedulable
   */

  private static class SynchronizedModel implements DataModel {
    @Nonnull
    private static final DataView SYNCHRONIZED_VIEW = DataViewBuilder.getStructure()
        .addField("static", DataType.BOOLEAN)
        .addField("dynamic", DataType.STRING)
        .build();

    @Nonnull
    private final ManagedSchedulable schedulable;

    public SynchronizedModel(@Nonnull ManagedSchedulable schedulable) {
      this.schedulable = schedulable;
    }

    @Override
    public Iterator<Object> iterator() {
      String dynamic = null;
      Method method = schedulable.getDynamicSynchronizedMethod();
      if (method != null) {
        dynamic = method.getDeclaringClass().getCanonicalName() + "#" + method.getName();
      }

      return Iterators.<Object> forArray(
          Boolean.valueOf(schedulable.isStaticSynchronized()),
          dynamic
      );
    }

    @Override
    @Nonnull
    public DataView getDataView() {
      return SYNCHRONIZED_VIEW;
    }
  }

  /* Runnable */

  private static class ConstraintModel implements DataModel {
    @Nonnull
    private static final DataView CONSTRAINT_VIEW = DataViewBuilder.getStructure()
        .addField("need", DataType.LIST)
        .addField("no", DataType.LIST)
        .build();

    @Nonnull
    private final ManagedRunnable runner;
    @CheckForNull
    private final FeatureSet features;

    public ConstraintModel(@Nonnull ManagedRunnable runner, @Nonnull FeatureSet features) {
      this.runner = runner;
      this.features = features;
    }

    public ConstraintModel(@Nonnull ManagedRunnable runner) {
      this.runner = runner;
      this.features = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Object> iterator() {
        return Iterators.<Object> forArray(
            // need
          new IdList(TagOrMarkerOrComponent.class)
              .addAll((Iterator<Class<? extends Item>>) (Object) ((features == null)
                  ? runner.getDefaultNeededTags().getCompactSet()
                  : runner.getNeededTags(features).getCompactSet()).iterator()),
              // no
          new IdList(TagOrMarkerOrComponent.class)
              .addAll((Iterator<Class<? extends Item>>) (Object) ((features == null)
                  ? runner.getDefaultUnsupportedTags().getCompactSet()
                  : runner.getUnsupportedTags(features).getCompactSet()).iterator()));
    }

    @Override
    @Nonnull
    public DataView getDataView() {
      return CONSTRAINT_VIEW;
    }
  }

  private static class TransformModel implements DataModel {
    @Nonnull
    private static final DataView TRANSFORM_VIEW = DataViewBuilder.getStructure()
        .addField("add", DataType.LIST)
        .addField("remove", DataType.LIST)
        .build();

    @Nonnull
    private final ManagedRunnable runner;

    public TransformModel(@Nonnull ManagedRunnable runner) {
      this.runner = runner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Object> iterator() {
        return Iterators.<Object> forArray(
            // add
          new IdList(TagOrMarkerOrComponent.class)
              .addAll((Iterator<Class<? extends Item>>) (Object) (runner.getAddedTags()
                  .getCompactSet().iterator())),
          // remove
          new IdList(TagOrMarkerOrComponent.class)
              .addAll((Iterator<Class<? extends Item>>) (Object) (runner.getRemovedTags()
                  .getCompactSet().iterator())));
    }

    @Override
    @Nonnull
    public DataView getDataView() {
      return TRANSFORM_VIEW;
    }
  }

  private static class OptionalModel implements DataModel {
    @Nonnull
    private static final DataView OPTIONAL_VIEW = DataViewBuilder.getStructure()
        .addField("supports", DataType.LIST)
        .addField("constraints", DataType.STRUCT)
        .build();

    @Nonnull
    private final ManagedRunnable runner;
    @Nonnull
    private final FeatureSet features;

    public OptionalModel(@Nonnull ManagedRunnable runner, @Nonnull FeatureSet features) {
      this.runner = runner;
      this.features = features;
    }

    @Override
    public Iterator<Object> iterator() {

      return Iterators.<Object>forArray(
          new DataModelListAdapter<Class<? extends Feature>>(
            new DataModelListAdapter.Converter<Class<? extends Feature>>() {
              @Override
              @Nonnull
              public DataModel apply(final Class<? extends Feature> data) {
                return new DataModel() {
                  @Override
                  public Iterator<Object> iterator() {
                    return Iterators.<Object>forArray(getId(data, Feature.class));
                  }

                  @Override
                  @Nonnull
                  public DataView getDataView() {
                    return ID_VIEW;
                  }
                };
              }
          }).addAll(features.getCompactSet().iterator()), new ConstraintModel(runner, features));
    }

    @Override
    @Nonnull
    public DataView getDataView() {
      return OPTIONAL_VIEW;
    }
  }

  @Nonnull
  private static final DataView RUNNER_VIEW = DataViewBuilder.getStructure()
      .addField("name", DataType.STRING)
      .addField("id", DataType.STRING)
      .addField("description", DataType.STRING)
      .addField("runOnId", DataType.STRING)
      .addField("supports", DataType.LIST)
      .addField("optional", DataType.LIST)
      .addField("produces", DataType.LIST)
      .addField("constraints", DataType.STRUCT)
      .addField("transforms", DataType.STRUCT)
      .addField("synchronized", DataType.STRUCT)
      .build();

  private static class ManagedRunnerModel implements DataModel {
    @Nonnull
    private final ManagedRunnable runner;

    public ManagedRunnerModel(@Nonnull ManagedRunnable runner) {
      this.runner = runner;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Iterator<Object> iterator() {
      return Iterators.forArray(
          // name
          runner.getName(),
          // id
          getId(runner.getSchedulable(), RunnableSchedulable.class),
          // description
          runner.getDescription(),
          // runOn
          getId(runner.getRunOn(), Component.class),
          // supports
          new IdList(Feature.class).addAll((Iterator<Class<? extends Item>>) (Object) (runner
              .getSupportedFeatures().getCompactSet().iterator())),
          // optional
          new DataModelListAdapter<FeatureSet>(new DataModelListAdapter.Converter<FeatureSet>() {
            @Override
            @Nonnull
            public DataModel apply(final FeatureSet data) {
              return new OptionalModel(runner, data);
            }
          }).addAll(runner.getOptionalFeatures()),
          // productions
          new IdList(Production.class).addAll((Iterator<Class<? extends Item>>) (Object) (runner
              .getProductions().getCompactSet().iterator())),
          // contraints
          new ConstraintModel(runner),
          // transform
          new TransformModel(runner),
          // synchronized
          new SynchronizedModel(runner)
      );
    }

    @Override
    @Nonnull
    public DataView getDataView() {
      return RUNNER_VIEW;
    }
  }

  @Nonnull
  private final DataModelListAdapter<ManagedRunnable> runners =
      new DataModelListAdapter<ManagedRunnable>(
          new DataModelListAdapter.Converter<ManagedRunnable>() {
            @Override
            @Nonnull
            public DataModel apply(ManagedRunnable data) {
              return new ManagedRunnerModel(data);
            }
          });

  private void addManagedRunnable(@Nonnull ManagedRunnable runnable) {
    runners.add(runnable);
  }

  /* Adapter */

  private static class ManagedVisitorModel implements DataModel {
    @Nonnull
    private static final DataView ADAPTER_VIEW = DataViewBuilder.getStructure()
        .addField("name", DataType.STRING)
        .addField("id", DataType.STRING)
        .addField("description", DataType.STRING)
        .addField("runOnId", DataType.STRING)
        .addField("adaptToId", DataType.STRING)
        .addField("synchronized", DataType.STRUCT)
        .build();

    @Nonnull
    private final ManagedVisitor adapter;

    public ManagedVisitorModel(@Nonnull ManagedVisitor adapter) {
      this.adapter = adapter;
    }

    @Override
    public Iterator<Object> iterator() {
      return Iterators.<Object> forArray(
          // name
          adapter.getName(),
          // id
          getId(adapter.getSchedulable(), AdapterSchedulable.class),
          // description
          adapter.getDescription(),
          // runOnId
          getId(adapter.getRunOn(), Component.class),
          // adaptToId
          getId(adapter.getRunOnAfter(), Component.class),
          // synchronized
          new SynchronizedModel(adapter)
      );
     }

    @Override
    @Nonnull
    public DataView getDataView() {
      return ADAPTER_VIEW;
    }
  }

  @Nonnull
  private final DataModelList adapters = new DataModelList();

  private void addManagedVisitor(@Nonnull ManagedVisitor visitor) {
    adapters.add(new ManagedVisitorModel(visitor));
  }

  /*
   * Report
   */

  @Nonnull
  private static final DataView DATA_VIEW = DataViewBuilder.getStructure()
      .addField("markers", DataType.LIST)
      .addField("tags", DataType.LIST)
      .addField("productions", DataType.LIST)
      .addField("features", DataType.LIST)
      .addField("components", DataType.LIST)
      .addField("adapters", DataType.LIST)
      .addField("runners", DataType.LIST)
      .build();

  private void close() {
    PrintStream stream = ThreadConfig.get(STREAM).getPrintStream();
    Printer provider = ThreadConfig.get(PRINTER).create(stream)
        .addResourceBundles(ResourceBundle.getBundle(ManagedDataAsReport.class.getCanonicalName()));

    try {
      stream.print("ctx=");
      provider.print(new DataModel() {
            @Override
            public Iterator<Object> iterator() {
              return Iterators.<Object>forArray(
                  markers,
                  tags,
                  productions,
                  features,
                  components,
                  adapters,
                  runners);
            }

            @Override
            @Nonnull
            public DataView getDataView() {
              return DATA_VIEW;
            }
          });
    } finally {
      stream.close();
    }
  }

  @Nonnull
  private static String getId(@Nonnull Class<?> cls, @Nonnull Class<?> category) {
    String id = cls.getCanonicalName();

    if (Feature.class.isAssignableFrom(category)) {
      return "f-" + id;
    } else if (TagOrMarkerOrComponent.class.isAssignableFrom(category)) {
      return "tcm-" + id;
    } else if (RunnableSchedulable.class.isAssignableFrom(category)) {
      return "r-" + id;
    } else if (AdapterSchedulable.class.isAssignableFrom(category)) {
      return "a-" + id;
    } else if (Production.class.isAssignableFrom(category)) {
      return "p-" + id;
    }

    throw new AssertionError("No 'id' for '" + id + "'");
  }
}
