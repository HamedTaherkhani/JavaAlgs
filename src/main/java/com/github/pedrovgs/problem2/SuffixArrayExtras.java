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
package com.github.pedrovgs.problem2;

import java.util.Arrays;

/**
 * Extra string algorithms related to suffix processing.
 */
public class SuffixArrayExtras {

  private SuffixArrayExtras() {
    // No instances.
  }

  public static int[] buildSuffixArrayNaive(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Text can't be null.");
    }
    int n = s.length();
    Integer[] order = new Integer[n];
    for (int i = 0; i < n; i++) {
      order[i] = i;
    }
    Arrays.sort(order, (a, b) -> compareSuffixes(s, a, b));
    int[] sa = new int[n];
    for (int i = 0; i < n; i++) {
      sa[i] = order[i];
    }
    return sa;
  }

  public static String longestRepeatedSubstringNaive(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Text can't be null.");
    }
    int n = s.length();
    if (n == 0) {
      return "";
    }
    int[] sa = buildSuffixArrayNaive(s);
    int bestLen = 0;
    int bestPos = -1;
    for (int i = 1; i < n; i++) {
      int l = lcp(s, sa[i - 1], sa[i]);
      if (l > bestLen) {
        bestLen = l;
        bestPos = sa[i];
      }
    }
    if (bestLen == 0) {
      return "";
    }
    return s.substring(bestPos, bestPos + bestLen);
  }

  public static int countDistinctSubstrings(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Text can't be null.");
    }
    int n = s.length();
    if (n == 0) {
      return 0;
    }
    int[] sa = buildSuffixArrayNaive(s);
    long total = (long) n * (n + 1) / 2;
    long lcpSum = 0;
    for (int i = 1; i < n; i++) {
      lcpSum += lcp(s, sa[i - 1], sa[i]);
    }
    long distinct = total - lcpSum;
    if (distinct > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    return (int) distinct;
  }

  public static int[] zAlgorithm(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Text can't be null.");
    }
    int n = s.length();
    int[] z = new int[n];
    int l = 0;
    int r = 0;
    for (int i = 1; i < n; i++) {
      if (i <= r) {
        z[i] = Math.min(r - i + 1, z[i - l]);
      }
      while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
        z[i]++;
      }
      if (i + z[i] - 1 > r) {
        l = i;
        r = i + z[i] - 1;
      }
    }
    return z;
  }

  public static int[] prefixFunction(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Text can't be null.");
    }
    int n = s.length();
    int[] pi = new int[n];
    for (int i = 1; i < n; i++) {
      int j = pi[i - 1];
      while (j > 0 && s.charAt(i) != s.charAt(j)) {
        j = pi[j - 1];
      }
      if (s.charAt(i) == s.charAt(j)) {
        j++;
      }
      pi[i] = j;
    }
    return pi;
  }

  public static BwtResult burrowsWheelerTransform(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Text can't be null.");
    }
    char sentinel = chooseSentinel(s);
    String t = s + sentinel;
    int n = t.length();
    int[] sa = buildSuffixArrayNaive(t);
    char[] out = new char[n];
    int primary = -1;
    for (int i = 0; i < n; i++) {
      int idx = sa[i];
      if (idx == 0) {
        primary = i;
        out[i] = t.charAt(n - 1);
      } else {
        out[i] = t.charAt(idx - 1);
      }
    }
    if (primary < 0) {
      throw new IllegalStateException("Invalid primary index.");
    }
    return new BwtResult(new String(out), primary, sentinel);
  }

  public static String inverseBurrowsWheeler(BwtResult result) {
    if (result == null) {
      throw new IllegalArgumentException("Result can't be null.");
    }
    String bwt = result.getTransformed();
    int n = bwt.length();
    int[] count = new int[256];
    for (int i = 0; i < n; i++) {
      char c = bwt.charAt(i);
      if (c >= 256) {
        throw new IllegalArgumentException("Non-ASCII character in BWT.");
      }
      count[c]++;
    }
    int[] start = new int[256];
    int sum = 0;
    for (int i = 0; i < 256; i++) {
      int c = count[i];
      start[i] = sum;
      sum += c;
    }
    int[] occ = new int[256];
    int[] next = new int[n];
    for (int i = 0; i < n; i++) {
      char c = bwt.charAt(i);
      next[i] = start[c] + occ[c];
      occ[c]++;
    }
    char[] out = new char[n];
    int idx = result.getPrimaryIndex();
    for (int i = n - 1; i >= 0; i--) {
      char c = bwt.charAt(idx);
      out[i] = c;
      idx = next[idx];
    }
    String restored = new String(out);
    if (restored.charAt(n - 1) != result.getSentinel()) {
      throw new IllegalStateException("Invalid sentinel in restored text.");
    }
    return restored.substring(0, n - 1);
  }

  private static int lcp(String s, int a, int b) {
    int n = s.length();
    int len = 0;
    while (a + len < n && b + len < n && s.charAt(a + len) == s.charAt(b + len)) {
      len++;
    }
    return len;
  }

  private static int compareSuffixes(String s, int a, int b) {
    int n = s.length();
    while (a < n && b < n) {
      char ca = s.charAt(a);
      char cb = s.charAt(b);
      if (ca != cb) {
        return ca - cb;
      }
      a++;
      b++;
    }
    return (n - a) - (n - b);
  }

  private static char chooseSentinel(String s) {
    boolean[] used = new boolean[256];
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < 256) {
        used[c] = true;
      }
    }
    for (int c = 1; c < used.length; c++) {
      if (!used[c]) {
        return (char) c;
      }
    }
    throw new IllegalStateException("No ASCII sentinel available.");
  }

  public static class BwtResult {
    private final String transformed;
    private final int primaryIndex;
    private final char sentinel;

    public BwtResult(String transformed, int primaryIndex, char sentinel) {
      this.transformed = transformed;
      this.primaryIndex = primaryIndex;
      this.sentinel = sentinel;
    }

    public String getTransformed() {
      return transformed;
    }

    public int getPrimaryIndex() {
      return primaryIndex;
    }

    public char getSentinel() {
      return sentinel;
    }
  }
}
