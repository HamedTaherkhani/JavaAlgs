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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Minimum Cost Maximum Flow using successive shortest augmenting paths with potentials.
 * This implementation supports negative edge costs (no negative cycles in residual graph).
 */
public class MinCostMaxFlow {

  private static final long INF = Long.MAX_VALUE / 4;

  private final int n;
  private final List<Edge>[] graph;

  public MinCostMaxFlow(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Number of nodes must be positive.");
    }
    this.n = n;
    this.graph = new List[n];
    for (int i = 0; i < n; i++) {
      graph[i] = new ArrayList<Edge>();
    }
  }

  public void addEdge(int from, int to, int capacity, int cost) {
    if (from < 0 || from >= n || to < 0 || to >= n) {
      throw new IllegalArgumentException("Node index out of bounds.");
    }
    if (capacity < 0) {
      throw new IllegalArgumentException("Capacity must be non-negative.");
    }
    Edge fwd = new Edge(to, capacity, cost, true);
    Edge rev = new Edge(from, 0, -cost, false);
    fwd.rev = rev;
    rev.rev = fwd;
    graph[from].add(fwd);
    graph[to].add(rev);
  }

  /**
   * @return Pair(flow, cost)
   */
  public Pair<Integer, Long> minCostMaxFlow(int source, int sink) {
    return minCostMaxFlow(source, sink, FlowOptions.defaults());
  }

  public Pair<Integer, Long> minCostFlow(int source, int sink, int maxFlow) {
    return minCostMaxFlow(source, sink, FlowOptions.defaults().withMaxFlow(maxFlow));
  }

  public Pair<Integer, Long> minCostMaxFlow(int source, int sink, FlowOptions options) {
    if (options == null) {
      options = FlowOptions.defaults();
    }
    if (source < 0 || source >= n || sink < 0 || sink >= n) {
      throw new IllegalArgumentException("Node index out of bounds.");
    }
    if (source == sink) {
      return new Pair<Integer, Long>(0, 0L);
    }
    if (options.maxFlow < 0) {
      throw new IllegalArgumentException("Max flow must be non-negative.");
    }
    if (!options.allowNegativeCosts && hasNegativeCostEdge()) {
      throw new IllegalArgumentException("Negative costs are not allowed by options.");
    }
    if (options.checkNegativeCycles && hasNegativeCycle()) {
      throw new IllegalStateException("Negative cost cycle detected.");
    }

    int flow = 0;
    long cost = 0;
    long[] potential = options.useBellmanFordInitPotentials
        ? bellmanFordPotentials(source)
        : new long[n];

    while (true) {
      PathResult path = shortestPathWithPotentials(source, sink, potential);
      if (path.dist[sink] == INF) {
        break;
      }
      for (int i = 0; i < n; i++) {
        if (path.dist[i] < INF) {
          potential[i] += path.dist[i];
        }
      }
      int add = path.bottleneck[sink];
      int remaining = options.maxFlow - flow;
      if (remaining <= 0) {
        break;
      }
      if (add > remaining) {
        add = remaining;
      }
      if (add <= 0) {
        break;
      }
      flow += add;
      cost += (long) add * path.pathCost[sink];

      int v = sink;
      while (v != source) {
        Edge e = path.parentEdge[v];
        e.capacity -= add;
        e.rev.capacity += add;
        v = path.parentNode[v];
      }
    }

    if (options.verifyConservation) {
      verifyConservation(source, sink, flow);
    }

    return new Pair<Integer, Long>(flow, cost);
  }

  private PathResult shortestPathWithPotentials(int source, int sink, long[] potential) {
    long[] dist = new long[n];
    Arrays.fill(dist, INF);
    int[] bottleneck = new int[n];
    int[] parentNode = new int[n];
    Edge[] parentEdge = new Edge[n];
    long[] pathCost = new long[n];

    dist[source] = 0;
    bottleneck[source] = Integer.MAX_VALUE;
    pathCost[source] = 0;
    Arrays.fill(parentNode, -1);

    PriorityQueue<State> pq = new PriorityQueue<State>();
    pq.add(new State(source, 0));

    while (!pq.isEmpty()) {
      State cur = pq.poll();
      if (cur.dist != dist[cur.node]) {
        continue;
      }
      if (cur.node == sink) {
        break;
      }
      for (Edge e : graph[cur.node]) {
        if (e.capacity <= 0) {
          continue;
        }
        long w = e.cost + potential[cur.node] - potential[e.to];
        long nd = dist[cur.node] + w;
        if (nd < dist[e.to]) {
          dist[e.to] = nd;
          parentNode[e.to] = cur.node;
          parentEdge[e.to] = e;
          bottleneck[e.to] = Math.min(bottleneck[cur.node], e.capacity);
          pathCost[e.to] = pathCost[cur.node] + e.cost;
          pq.add(new State(e.to, nd));
        }
      }
    }

    return new PathResult(dist, bottleneck, parentNode, parentEdge, pathCost);
  }

  private boolean hasNegativeCostEdge() {
    for (int u = 0; u < n; u++) {
      for (Edge e : graph[u]) {
        if (e.original && e.capacity > 0 && e.cost < 0) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasNegativeCycle() {
    long[] dist = new long[n];
    Arrays.fill(dist, 0);
    for (int i = 0; i < n; i++) {
      boolean updated = false;
      for (int u = 0; u < n; u++) {
        for (Edge e : graph[u]) {
          if (!e.original || e.capacity <= 0) {
            continue;
          }
          long nd = dist[u] + e.cost;
          if (nd < dist[e.to]) {
            dist[e.to] = nd;
            updated = true;
          }
        }
      }
      if (!updated) {
        return false;
      }
    }
    return true;
  }

  private long[] bellmanFordPotentials(int source) {
    long[] dist = new long[n];
    Arrays.fill(dist, INF);
    dist[source] = 0;
    for (int i = 0; i < n - 1; i++) {
      boolean updated = false;
      for (int u = 0; u < n; u++) {
        if (dist[u] == INF) {
          continue;
        }
        for (Edge e : graph[u]) {
          if (!e.original || e.capacity <= 0) {
            continue;
          }
          long nd = dist[u] + e.cost;
          if (nd < dist[e.to]) {
            dist[e.to] = nd;
            updated = true;
          }
        }
      }
      if (!updated) {
        break;
      }
    }
    for (int i = 0; i < n; i++) {
      if (dist[i] == INF) {
        dist[i] = 0;
      }
    }
    return dist;
  }

  private void verifyConservation(int source, int sink, int totalFlow) {
    long[] balance = new long[n];
    for (int u = 0; u < n; u++) {
      for (Edge e : graph[u]) {
        if (!e.original) {
          continue;
        }
        int sent = e.initialCapacity - e.capacity;
        if (sent != 0) {
          balance[u] -= sent;
          balance[e.to] += sent;
        }
      }
    }
    for (int i = 0; i < n; i++) {
      long expected = 0;
      if (i == source) {
        expected = -totalFlow;
      } else if (i == sink) {
        expected = totalFlow;
      }
      if (balance[i] != expected) {
        throw new IllegalStateException("Flow conservation violated at node " + i);
      }
    }
  }

  private static class Edge {
    private final int to;
    private int capacity;
    private final int initialCapacity;
    private final int cost;
    private final boolean original;
    private Edge rev;

    private Edge(int to, int capacity, int cost, boolean original) {
      this.to = to;
      this.capacity = capacity;
      this.initialCapacity = capacity;
      this.cost = cost;
      this.original = original;
    }
  }

  private static class State implements Comparable<State> {
    private final int node;
    private final long dist;

    private State(int node, long dist) {
      this.node = node;
      this.dist = dist;
    }

    @Override
    public int compareTo(State other) {
      return Long.compare(this.dist, other.dist);
    }
  }

  private static class PathResult {
    private final long[] dist;
    private final int[] bottleneck;
    private final int[] parentNode;
    private final Edge[] parentEdge;
    private final long[] pathCost;

    private PathResult(
        long[] dist, int[] bottleneck, int[] parentNode, Edge[] parentEdge, long[] pathCost) {
      this.dist = dist;
      this.bottleneck = bottleneck;
      this.parentNode = parentNode;
      this.parentEdge = parentEdge;
      this.pathCost = pathCost;
    }
  }

  public static class FlowOptions {
    private int maxFlow = Integer.MAX_VALUE;
    private boolean checkNegativeCycles;
    private boolean verifyConservation;
    private boolean useBellmanFordInitPotentials;
    private boolean allowNegativeCosts = true;

    public static FlowOptions defaults() {
      return new FlowOptions();
    }

    public FlowOptions withMaxFlow(int maxFlow) {
      this.maxFlow = maxFlow;
      return this;
    }

    public FlowOptions withNegativeCycleCheck(boolean enabled) {
      this.checkNegativeCycles = enabled;
      return this;
    }

    public FlowOptions withConservationCheck(boolean enabled) {
      this.verifyConservation = enabled;
      return this;
    }

    public FlowOptions withBellmanFordInitPotentials(boolean enabled) {
      this.useBellmanFordInitPotentials = enabled;
      return this;
    }

    public FlowOptions withAllowNegativeCosts(boolean allowed) {
      this.allowNegativeCosts = allowed;
      return this;
    }
  }
}
