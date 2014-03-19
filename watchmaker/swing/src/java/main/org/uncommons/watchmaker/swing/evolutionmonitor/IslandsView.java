//=============================================================================
// Copyright 2006-2010 Daniel W. Dyer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//=============================================================================
package org.uncommons.watchmaker.swing.evolutionmonitor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.islands.IslandEvolutionObserver;

/**
 * An evolution monitor view that gives an insight into how the evolution is progressing on
 * individual islands.
 * @author Daniel Dyer
 */
class IslandsView extends JPanel implements IslandEvolutionObserver<Object>
{
    private static final String FITTEST_INDIVIDUAL_LABEL = "Fittest Individual";
    private static final String MEAN_FITNESS_LABEL = "Mean Fitness/Standard Deviation";

    private final DefaultCategoryDataset bestDataSet = new DefaultCategoryDataset();
    private final DefaultStatisticalCategoryDataset meanDataSet = new DefaultStatisticalCategoryDataset();
    private final StatisticalLineAndShapeRenderer meanRenderer = new StatisticalLineAndShapeRenderer();

    private final AtomicInteger islandCount = new AtomicInteger(0);
    private final Object maxLock = new Object();
    private double max = 0;



    IslandsView()
    {
        super(new BorderLayout());
        add(createControls(), BorderLayout.SOUTH);
    }


    /**
     * Creates the GUI controls for toggling graph display options.
     * @return A component that can be added to the main panel.
     */
    private JComponent createControls()
    {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        final JCheckBox meanCheckBox = new JCheckBox("Show Mean and Standard Deviation", false);
        meanCheckBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent itemEvent)
            {
            }
        });
        controls.add(meanCheckBox);

        return controls;
    }



    public void islandPopulationUpdate(final int islandIndex, final PopulationData<? extends Object> populationData)
    {
        // Make sure the bars are added to the chart in order of island index, regardless of which island
        // reports its results first.
        if (islandIndex >= islandCount.get())
        {
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    public void run()
                    {
                        // Don't need synchronisation here because SwingUtilities queues these updates
                        // and if a second update gets queued, the loop will be a no-op so it's not a problem.
                        for (Integer i = islandCount.get(); i <= islandIndex; i++)
                        {
                            bestDataSet.addValue(0, FITTEST_INDIVIDUAL_LABEL, i);
                            meanDataSet.add(0, 0, MEAN_FITNESS_LABEL, i);
                            islandCount.incrementAndGet();
                        }
                    }
                });
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
            catch (InvocationTargetException ex)
            {
                throw new IllegalStateException(ex.getCause());
            }
        }
    }


    public void populationUpdate(PopulationData<? extends Object> populationData)
    {
        synchronized (maxLock)
        {
            max = 0;
        }
    }
}
