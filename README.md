# Java Algorithms (Focused Set)

This repository contains a small, focused set of advanced algorithms implemented in Java, with JUnit 4 tests.

## Structure

- `src/main/java/com/github/pedrovgs/mincostmaxflow` — Min-cost max-flow implementation.
- `src/main/java/com/github/pedrovgs/suffixarrayextras` — Suffix array utilities.
- `src/main/java/com/github/pedrovgs/ahocorasickadvanced` — Advanced Aho-Corasick variant.
- `src/test/java/com/github/pedrovgs/...` — Tests for each algorithm package.

## Algorithms Included

- **MinCostMaxFlow**: Successive shortest augmenting paths with potentials; supports negative edge costs (no negative cycles).
- **SuffixArrayExtras**: Naive suffix array construction and distinct substring counting.
- **AhoCorasickAdvanced**: Weighted pattern matching, match extraction, and max-score non-overlapping selection.

## Run Tests

```sh
mvn test
```

## Coverage Report

```sh
mvn test jacoco:report
```

Open `target/site/jacoco/index.html` to view the report.
