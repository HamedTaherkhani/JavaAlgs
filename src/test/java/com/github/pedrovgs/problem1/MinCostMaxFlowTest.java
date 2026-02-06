/*
 * Copyright (C) 2014 Pedro Vicente Gómez Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pedrovgs.problem1;

import com.github.pedrovgs.pair.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MinCostMaxFlowTest {

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNonPositiveNodeCount() {
    new MinCostMaxFlow(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectInvalidEdgeNodes() {
    MinCostMaxFlow flow = new MinCostMaxFlow(3);
    flow.addEdge(-1, 2, 1, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNegativeCapacity() {
    MinCostMaxFlow flow = new MinCostMaxFlow(3);
    flow.addEdge(0, 1, -1, 1);
  }

  @Test
  public void shouldComputeMinCostMaxFlow() {
    MinCostMaxFlow flow = new MinCostMaxFlow(4);
    flow.addEdge(0, 1, 2, 2);
    flow.addEdge(0, 2, 1, 6);
    flow.addEdge(1, 2, 1, 2);
    flow.addEdge(1, 3, 1, 1);
    flow.addEdge(2, 3, 2, 2);

    Pair<Integer, Long> result = flow.minCostMaxFlow(0, 3);
    assertEquals(new Pair<Integer, Long>(3, 17L), result);
  }

  @Test
  public void shouldRespectMaxFlowLimit() {
    MinCostMaxFlow flow = new MinCostMaxFlow(4);
    flow.addEdge(0, 1, 2, 2);
    flow.addEdge(0, 2, 1, 6);
    flow.addEdge(1, 2, 1, 2);
    flow.addEdge(1, 3, 1, 1);
    flow.addEdge(2, 3, 2, 2);

    Pair<Integer, Long> result = flow.minCostFlow(0, 3, 2);
    assertEquals(new Pair<Integer, Long>(2, 9L), result);
  }

  @Test
  public void shouldAllowNegativeCostsByDefault() {
    MinCostMaxFlow flow = new MinCostMaxFlow(3);
    flow.addEdge(0, 1, 1, -5);
    flow.addEdge(1, 2, 1, 2);
    flow.addEdge(0, 2, 1, 4);

    Pair<Integer, Long> result = flow.minCostMaxFlow(0, 2);
    assertEquals(new Pair<Integer, Long>(2, 1L), result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNegativeCostsWhenNotAllowed() {
    MinCostMaxFlow flow = new MinCostMaxFlow(2);
    flow.addEdge(0, 1, 1, -1);

    MinCostMaxFlow.FlowOptions options = MinCostMaxFlow.FlowOptions.defaults()
        .withAllowNegativeCosts(false);
    flow.minCostMaxFlow(0, 1, options);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldDetectNegativeCyclesWhenEnabled() {
    MinCostMaxFlow flow = new MinCostMaxFlow(3);
    flow.addEdge(0, 1, 1, -2);
    flow.addEdge(1, 2, 1, -2);
    flow.addEdge(2, 0, 1, -2);

    MinCostMaxFlow.FlowOptions options = MinCostMaxFlow.FlowOptions.defaults()
        .withNegativeCycleCheck(true);
    flow.minCostMaxFlow(0, 2, options);
  }

  @Test
  public void shouldReturnZeroFlowWhenSourceEqualsSink() {
    MinCostMaxFlow flow = new MinCostMaxFlow(2);
    flow.addEdge(0, 1, 5, 1);

    Pair<Integer, Long> result = flow.minCostMaxFlow(0, 0);
    assertEquals(new Pair<Integer, Long>(0, 0L), result);
  }
}
