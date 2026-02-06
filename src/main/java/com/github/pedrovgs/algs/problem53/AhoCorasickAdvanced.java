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
package com.github.pedrovgs.algs.problem53;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Advanced Aho-Corasick variant with weighted patterns, match extraction and
 * non-overlapping maximum score selection.
 */
public class AhoCorasickAdvanced {

  private final List<Node> nodes = new ArrayList<Node>();
  private final List<Pattern> patterns = new ArrayList<Pattern>();
  private boolean built;
  private boolean strictValidation;
  private boolean caseInsensitive;
  private boolean normalizeWhitespace;

  public AhoCorasickAdvanced() {
    nodes.add(new Node());
    nodes.get(0).fail = 0;
  }

  /**
   * Enables extra validation and error paths. Default is false.
   */
  public void setStrictValidation(boolean strictValidation) {
    ensureNotBuilt("change validation settings");
    this.strictValidation = strictValidation;
  }

  /**
   * Enables case-insensitive matching. Default is false.
   */
  public void setCaseInsensitive(boolean caseInsensitive) {
    ensureNotBuilt("change case sensitivity");
    this.caseInsensitive = caseInsensitive;
  }

  /**
   * Enables whitespace normalization (all whitespace becomes a single space). Default is false.
   */
  public void setNormalizeWhitespace(boolean normalizeWhitespace) {
    ensureNotBuilt("change whitespace normalization");
    this.normalizeWhitespace = normalizeWhitespace;
  }

  public int addPattern(String pattern, int weight) {
    if (pattern == null) {
      throw new IllegalArgumentException("Pattern can't be null.");
    }
    if (built) {
      throw new IllegalStateException("Cannot add patterns after build().");
    }
    if (strictValidation && pattern.length() == 0) {
      throw new IllegalArgumentException("Pattern can't be empty when strict validation is enabled.");
    }
    if (strictValidation && weight == 0) {
      throw new IllegalArgumentException("Pattern weight can't be zero when strict validation is enabled.");
    }
    if (strictValidation && weight < 0) {
      throw new IllegalArgumentException("Pattern weight can't be negative when strict validation is enabled.");
    }
    String normalizedPattern = normalizePattern(pattern);
    int current = 0;
    for (int i = 0; i < normalizedPattern.length(); i++) {
      char ch = normalizedPattern.charAt(i);
      Integer next = nodes.get(current).next.get(ch);
      if (next == null) {
        next = nodes.size();
        nodes.get(current).next.put(ch, next);
        nodes.add(new Node());
      }
      current = next;
    }
    int id = patterns.size();
    patterns.add(new Pattern(normalizedPattern, weight));
    nodes.get(current).output.add(id);
    return id;
  }

  public void build() {
    if (built) {
      throw new IllegalStateException("build() has already been called.");
    }
    if (strictValidation && patterns.isEmpty()) {
      throw new IllegalStateException("At least one pattern is required when strict validation is enabled.");
    }
    Queue<Integer> q = new ArrayDeque<Integer>();
    for (Map.Entry<Character, Integer> e : nodes.get(0).next.entrySet()) {
      int v = e.getValue();
      nodes.get(v).fail = 0;
      if (!nodes.get(0).output.isEmpty()) {
        nodes.get(v).output.addAll(nodes.get(0).output);
      }
      q.add(v);
    }
    while (!q.isEmpty()) {
      int v = q.poll();
      for (Map.Entry<Character, Integer> e : nodes.get(v).next.entrySet()) {
        char ch = e.getKey();
        int u = e.getValue();
        int f = nodes.get(v).fail;
        while (f != 0 && !nodes.get(f).next.containsKey(ch)) {
          f = nodes.get(f).fail;
        }
        if (nodes.get(f).next.containsKey(ch)) {
          f = nodes.get(f).next.get(ch);
        }
        nodes.get(u).fail = f;
        nodes.get(u).output.addAll(nodes.get(f).output);
        q.add(u);
      }
    }
    built = true;
  }

  public int[] countOccurrencesByPattern(String text) {
    ensureBuiltForSearch();
    validateText(text);
    int[] counts = new int[patterns.size()];
    int state = 0;
    for (int i = 0; i < text.length(); i++) {
      state = advanceState(state, text.charAt(i));
      for (int id : nodes.get(state).output) {
        counts[id]++;
      }
    }
    return counts;
  }

  public long scoreMatches(String text) {
    ensureBuiltForSearch();
    validateText(text);
    long score = 0;
    int state = 0;
    for (int i = 0; i < text.length(); i++) {
      state = advanceState(state, text.charAt(i));
      for (int id : nodes.get(state).output) {
        score += patterns.get(id).weight;
      }
    }
    return score;
  }

  public List<Match> searchAll(String text) {
    return searchAll(text, Integer.MAX_VALUE);
  }

  public List<Match> searchAll(String text, int limit) {
    ensureBuiltForSearch();
    validateText(text);
    if (limit < 0) {
      throw new IllegalArgumentException("Limit must be non-negative.");
    }
    if (limit == 0) {
      return Collections.emptyList();
    }
    if (patterns.isEmpty()) {
      if (strictValidation) {
        throw new IllegalStateException("No patterns available for search under strict validation.");
      }
      return Collections.emptyList();
    }
    List<Match> matches = new ArrayList<Match>();
    int state = 0;
    for (int i = 0; i < text.length(); i++) {
      state = advanceState(state, text.charAt(i));
      for (int id : nodes.get(state).output) {
        Pattern p = patterns.get(id);
        int start = i - p.length + 1;
        matches.add(new Match(id, start, i, p.weight));
        if (matches.size() >= limit) {
          return matches;
        }
      }
    }
    return matches;
  }

  public Match searchFirst(String text) {
    List<Match> all = searchAll(text, 1);
    return all.isEmpty() ? null : all.get(0);
  }

  public Match findLongestMatch(String text) {
    ensureBuiltForSearch();
    validateText(text);
    int state = 0;
    Match best = null;
    for (int i = 0; i < text.length(); i++) {
      state = advanceState(state, text.charAt(i));
      for (int id : nodes.get(state).output) {
        Pattern p = patterns.get(id);
        int start = i - p.length + 1;
        Match m = new Match(id, start, i, p.weight);
        if (best == null || m.length() > best.length()
            || (m.length() == best.length() && m.start < best.start)) {
          best = m;
        }
      }
    }
    return best;
  }

  /**
   * Select a set of non-overlapping matches with maximum total weight.
   */
  public List<Match> selectMaxScoreNonOverlapping(String text) {
    List<Match> matches = searchAll(text);
    if (matches.isEmpty()) {
      return Collections.emptyList();
    }
    Collections.sort(matches, Comparator.comparingInt(m -> m.end));
    int n = matches.size();
    int[] prev = new int[n];
    for (int i = 0; i < n; i++) {
      prev[i] = findPrevNonOverlapping(matches, i);
    }
    long[] dp = new long[n];
    boolean[] take = new boolean[n];
    for (int i = 0; i < n; i++) {
      long include = matches.get(i).weight;
      int p = prev[i];
      if (p >= 0) {
        include += dp[p];
      }
      long exclude = (i > 0) ? dp[i - 1] : 0;
      if (include >= exclude) {
        dp[i] = include;
        take[i] = true;
      } else {
        dp[i] = exclude;
      }
    }
    List<Match> result = new ArrayList<Match>();
    for (int i = n - 1; i >= 0; ) {
      if (take[i]) {
        result.add(matches.get(i));
        i = prev[i];
      } else {
        i--;
      }
    }
    Collections.reverse(result);
    return result;
  }

  private void ensureNotBuilt(String action) {
    if (built) {
      throw new IllegalStateException("Cannot " + action + " after build().");
    }
  }

  private void ensureBuiltForSearch() {
    if (!built) {
      throw new IllegalStateException("build() must be called before search().");
    }
  }

  private void validateText(String text) {
    if (text == null) {
      throw new IllegalArgumentException("Text can't be null.");
    }
    if (strictValidation && text.length() == 0) {
      throw new IllegalArgumentException("Text can't be empty when strict validation is enabled.");
    }
  }

  private String normalizePattern(String pattern) {
    if (!caseInsensitive && !normalizeWhitespace) {
      return pattern;
    }
    StringBuilder sb = new StringBuilder(pattern.length());
    for (int i = 0; i < pattern.length(); i++) {
      sb.append(normalizeChar(pattern.charAt(i)));
    }
    return sb.toString();
  }

  private char normalizeChar(char ch) {
    char normalized = ch;
    if (caseInsensitive) {
      normalized = Character.toLowerCase(normalized);
    }
    if (normalizeWhitespace && Character.isWhitespace(normalized)) {
      normalized = ' ';
    }
    return normalized;
  }

  private int advanceState(int state, char rawChar) {
    char ch = normalizeChar(rawChar);
    while (state != 0 && !nodes.get(state).next.containsKey(ch)) {
      state = nodes.get(state).fail;
    }
    if (nodes.get(state).next.containsKey(ch)) {
      state = nodes.get(state).next.get(ch);
    }
    return state;
  }

  private int findPrevNonOverlapping(List<Match> matches, int idx) {
    int lo = 0;
    int hi = idx - 1;
    int ans = -1;
    int start = matches.get(idx).start;
    while (lo <= hi) {
      int mid = (lo + hi) >>> 1;
      if (matches.get(mid).end < start) {
        ans = mid;
        lo = mid + 1;
      } else {
        hi = mid - 1;
      }
    }
    return ans;
  }

  private static class Pattern {
    private final String value;
    private final int length;
    private final int weight;

    private Pattern(String value, int weight) {
      this.value = value;
      this.length = value.length();
      this.weight = weight;
    }
  }

  public static class Match {
    private final int patternId;
    private final int start;
    private final int end;
    private final int weight;

    public Match(int patternId, int start, int end, int weight) {
      this.patternId = patternId;
      this.start = start;
      this.end = end;
      this.weight = weight;
    }

    public int getPatternId() {
      return patternId;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }

    public int getWeight() {
      return weight;
    }

    public int length() {
      return end - start + 1;
    }
  }

  private static class Node {
    private final Map<Character, Integer> next = new HashMap<Character, Integer>();
    private final List<Integer> output = new ArrayList<Integer>();
    private int fail;
  }
}
