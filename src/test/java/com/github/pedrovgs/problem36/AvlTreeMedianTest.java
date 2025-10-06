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
package com.github.pedrovgs.problem36;

import com.github.pedrovgs.binarytree.BinaryNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Vicente Gómez Sánchez.
 */
public class AvlTreeMedianTest {

  private static final double DELTA = 0.1;

  private AvlTreeMedian avlTreeMedian;

  @Before public void setUp() {
    avlTreeMedian = new AvlTreeMedian();
  }

  @Test(expected = IllegalArgumentException.class) public void shouldNotAcceptNullTrees() {
    avlTreeMedian.find(null);
  }

  @Test public void shouldReturnRootElementIfTheTreeContainsJustOneElement() {
    BinaryNode<Integer> root = new BinaryNode<Integer>(1);

    double median = avlTreeMedian.find(root);

    assertEquals(1, median, DELTA);
  }

  @Test public void shouldReturnRootElementIfTheTreeContainsTFiveElements() {
    BinaryNode<Integer> root = new BinaryNode<Integer>(2);
    BinaryNode<Integer> n1 = new BinaryNode<Integer>(1);
    BinaryNode<Integer> n3 = new BinaryNode<Integer>(3);
    BinaryNode<Integer> n4 = new BinaryNode<Integer>(4);
    BinaryNode<Integer> n5 = new BinaryNode<Integer>(-1);

    root.setLeft(n1);
    root.setRight(n3);
    n3.setRight(n4);
    n1.setLeft(n5);

    double median = avlTreeMedian.find(root);

    assertEquals(2, median, DELTA);
  }
}
