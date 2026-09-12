# CLAUDE.md

## What this repository is

NeoLegal's fork of [tabulapdf/tabula-java](https://github.com/tabulapdf/tabula-java), a library that
extracts tables from PDF files. Published to Maven Central as `fr.neolegal:tabula`. Java 17, Maven,
JUnit 5, PDFBox 3.

The fork exists to handle PDFs whose table borders are drawn imprecisely. It adds options to
`SpreadsheetExtractionAlgorithm`; it does not change how tabula works by default.

## The rule that matters most

**An unconfigured extractor must behave exactly like upstream.** Every fork behaviour is an option,
off by default. The whole upstream test suite passes unmodified, and that is the check: if a change
makes an upstream test fail, the change is wrong, not the test.

This is what keeps the fork rebasable. The fork once diverged by defaulting its own behaviours on;
`master` was red by 26 tests as a result, and every upstream commit conflicted.

Concretely, when adding behaviour:

- add an overload, never change an existing signature — the existing one delegates with the
  historical constants (`Ruling.PERPENDICULAR_PIXEL_EXPAND_AMOUNT`,
  `Ruling.COLINEAR_OR_PARALLEL_PIXEL_EXPAND_AMOUNT`) so its behaviour is untouched;
- expose it through a `withXxx()` builder method on `SpreadsheetExtractionAlgorithm`, defaulting to
  the value that reproduces upstream (`0` / `false` / the upstream constant);
- add it to `neolegalDefaults()` only if NeoLegal actually runs with it in production;
- cover it with a test that asserts *both* states — default off, and the option on.

**Do not reformat.** Not imports, not brace style, not line width. Gratuitous reformatting was the
single largest source of divergence in this fork's history — it touched 28 files and changed no
behaviour, while making every upstream rebase conflict.

## Commands

```bash
mvn test                                  # full suite, ~1 min (TestTableDetection alone is ~35 s)
mvn test -Dtest=TestRuling,TestSpreadsheetExtractor   # comma-separated, not '+'
mvn clean compile assembly:single         # the jar-with-dependencies
mvn javadoc:javadoc                       # target/site/apidocs/
```

`mvn` on this machine is the Windows install under WSL; it works, but it is slow to start.

## Staying close to upstream

```bash
git remote add upstream https://github.com/tabulapdf/tabula-java.git   # once
git fetch upstream
git diff --numstat upstream/master HEAD | awk '{d+=$2} END {print d}'  # conflict surface
```

That last number — upstream lines deleted or modified — is the metric to keep small. Additions are
nearly free at rebase time; edits to existing upstream lines are what conflicts. It was 815 before
the cleanup and 218 after.

When rebasing onto a new upstream: re-apply the fork's intent on top of upstream's files rather than
replaying 20+ commits. The fork's substance is small (`pom.xml`, `Ruling`, `SpreadsheetExtractionAlgorithm`,
the JUnit 5 migration, the release workflow); everything else in a raw diff is noise.

## Where the fork's code lives

- `Ruling` — parameterized expansion amounts on `nearlyIntersects`, `intersectionPoint`,
  `findIntersections`; `parallelTo`; and the `magnetRadius` second pass of
  `collapseOrientedRulings`, which merges parallel overlapping rulings that draw the same border.
  The first (colinear) pass is upstream's, kept verbatim.
- `extractors/SpreadsheetExtractionAlgorithm` — the `withXxx()` options, `neolegalDefaults()`,
  `findGaps` cell autocompletion, and the cell text overflow area.
- `extractors/SpreadsheetExtractionAlgorithmTest` — coverage for each option.

Watch out for two traps found the hard way:

- `ObjectExtractor.close()` closes the underlying `PDDocument`. Never wrap an `ObjectExtractor` in
  try-with-resources when the caller still has to iterate the `PageIterator` it returned, or every
  page comes back null. `CommandLineApp` already closes the document in its `finally`.
- `Page` hands out the rulings it caches. Code that collapses or expands rulings must copy them
  first, or a second extraction of the same page starts from a mutated state.

## Releasing

`mvn deploy -P release` publishes to Maven Central through the Central Portal
(`central-publishing-maven-plugin`, `autoPublish=true`). It needs a `central` server in
`~/.m2/settings.xml` holding a Central Portal token, and a GPG signing key in the local keyring.

A **Maven Central release is permanent** — a version can never be replaced or withdrawn. Confirm the
version number with the maintainer before deploying, and remember that an upstream bump that changes
the PDFBox major version, or a change to a default, is breaking for downstream applications and
deserves more than a patch bump.

`.github/workflows/release-to-maven-central.yml` does the same thing from CI (`workflow_dispatch`),
but it needs `OSS_SONATYPE_USERNAME`, `OSS_SONATYPE_PASSWORD`, `MAVEN_GPG_PRIVATE_KEY` and
`MAVEN_GPG_PASSPHRASE` as repository secrets, which are not configured yet.
