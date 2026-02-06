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

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SuffixArrayExtrasTest {

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNullInputForSuffixArray() {
    SuffixArrayExtras.buildSuffixArrayNaive(null);
  }

  @Test
  public void shouldBuildSuffixArrayForBanana() {
    int[] sa = SuffixArrayExtras.buildSuffixArrayNaive("banana");
    assertArrayEquals(new int[] {5, 3, 1, 0, 4, 2}, sa);
  }


  @Test
  public void shouldCountDistinctSubstrings() {
    assertEquals(3, SuffixArrayExtras.countDistinctSubstrings("aaa"));
    assertEquals(3, SuffixArrayExtras.countDistinctSubstrings("ab"));
    assertEquals(0, SuffixArrayExtras.countDistinctSubstrings(""));
  }

  
}
