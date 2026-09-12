tabula-java — NeoLegal fork [![Java CI](https://github.com/neolegal-fr/tabula-java/actions/workflows/tests.yml/badge.svg)](https://github.com/neolegal-fr/tabula-java/actions/workflows/tests.yml)
===========================

`tabula-java` is a library for extracting tables from PDF files — it is the table extraction engine that powers [Tabula](http://tabula.technology/) ([repo](http://github.com/tabulapdf/tabula)). You can use `tabula-java` as a command-line tool to programmatically extract tables from PDFs.

This repository is [NeoLegal](https://neolegal.fr)'s fork of [tabulapdf/tabula-java](https://github.com/tabulapdf/tabula-java). It follows upstream and adds options for PDFs whose table borders are drawn imprecisely — see [NeoLegal fork](#neolegal-fork) below. **Every option is off by default: an unconfigured extractor behaves exactly like upstream.**

© 2014-2020 Manuel Aristarán. Available under MIT License. See [`LICENSE`](LICENSE).

## Download

This fork is published to Maven Central as `fr.neolegal:tabula`:

```xml
<dependency>
    <groupId>fr.neolegal</groupId>
    <artifactId>tabula</artifactId>
    <version>1.1.0</version>
</dependency>
```

```groovy
implementation 'fr.neolegal:tabula:1.1.0'
```

A jar with all dependencies included, that works on Mac, Windows and Linux, is on the [releases page](../../releases).

## Commandline Usage Examples

`tabula-java` provides a command line application:

```
$ java -jar target/tabula-1.1.0-jar-with-dependencies.jar --help
usage: tabula [-a <AREA>] [-b <DIRECTORY>] [-c <COLUMNS>] [-f <FORMAT>]
       [-g] [-h] [-i] [-l] [-n] [-o <OUTFILE>] [-p <PAGES>] [-r] [-s
       <PASSWORD>] [-t] [-u] [-v]

Tabula helps you extract tables from PDFs

 -a,--area <AREA>           -a/--area = Portion of the page to analyze.
                            Example: --area 269.875,12.75,790.5,561.
                            Accepts top,left,bottom,right i.e. y1,x1,y2,x2
                            where all values are in points relative to the
                            top left corner. If all values are between
                            0-100 (inclusive) and preceded by '%', input
                            will be taken as % of actual height or width
                            of the page. Example: --area %0,0,100,50. To
                            specify multiple areas, -a option should be
                            repeated. Default is entire page
 -b,--batch <DIRECTORY>     Convert all .pdfs in the provided directory.
 -c,--columns <COLUMNS>     X coordinates of column boundaries. Example
                            --columns 10.1,20.2,30.3. If all values are
                            between 0-100 (inclusive) and preceded by '%',
                            input will be taken as % of actual width of
                            the page. Example: --columns %25,50,80.6
 -f,--format <FORMAT>       Output format: (CSV,TSV,JSON). Default: CSV
 -g,--guess                 Guess the portion of the page to analyze per
                            page.
 -h,--help                  Print this help text.
 -i,--silent                Suppress all stderr output.
 -l,--lattice               Force PDF to be extracted using lattice-mode
                            extraction (if there are ruling lines
                            separating each cell, as in a PDF of an Excel
                            spreadsheet)
 -n,--no-spreadsheet        [Deprecated in favor of -t/--stream] Force PDF
                            not to be extracted using spreadsheet-style
                            extraction (if there are no ruling lines
                            separating each cell)
 -o,--outfile <OUTFILE>     Write output to <file> instead of STDOUT.
                            Default: -
 -p,--pages <PAGES>         Comma separated list of ranges, or all.
                            Examples: --pages 1-3,5-7, --pages 3 or
                            --pages all. Default is --pages 1
 -r,--spreadsheet           [Deprecated in favor of -l/--lattice] Force
                            PDF to be extracted using spreadsheet-style
                            extraction (if there are ruling lines
                            separating each cell, as in a PDF of an Excel
                            spreadsheet)
 -s,--password <PASSWORD>   Password to decrypt document. Default is empty
 -t,--stream                Force PDF to be extracted using stream-mode
                            extraction (if there are no ruling lines
                            separating each cell)
 -u,--use-line-returns      Use embedded line returns in cells. (Only in
                            spreadsheet mode.)
 -v,--version               Print version and exit.
```

It also includes a debugging tool, run `java -cp ./target/tabula-1.1.0-jar-with-dependencies.jar technology.tabula.debug.Debug -h` for the available options.

You can also integrate `tabula-java` with any JVM language. For Java examples, see the [`tests`](src/test/java/technology/tabula/) folder.

JVM start-up time is a lot of the cost of the `tabula` command, so if you're trying to extract many tables from PDFs, you have a few options for speeding it up:

 - the -b option, which allows you to convert all pdfs in a given directory
 - the [drip](https://github.com/ninjudd/drip) utility
 - the [Ruby](http://github.com/tabulapdf/tabula-extractor), [Python](https://github.com/chezou/tabula-py), [R](https://github.com/leeper/tabulizer), and [Node.js](https://github.com/ezodude/tabula-js) bindings
 - writing your own program in any JVM language (Java, JRuby, Scala) that imports tabula-java.
 - waiting for us to implement an API/server-style system (it's on the [roadmap](https://github.com/tabulapdf/tabula-api))

## NeoLegal fork

`SpreadsheetExtractionAlgorithm` detects tables from the rulings a PDF draws. When those rulings are
drawn imprecisely — a border split into two slightly offset segments, a horizontal line that stops
just short of the vertical border it should meet, a leading cell with no border of its own — cells
get multiplied or their text gets dropped. These options relax the detection for such documents:

```java
SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm()
        // rebuild the cells missing on the left of the detected ones, for tables whose
        // leading cells have no border of their own
        .withCellAutocompletion(true)
        // widen each cell by 1% before collecting its text, to catch a trailing letter
        // that the right border of the cell is drawn over
        .withCellTextOverflowRatio(0.01f)
        // merge two vertical borders less than 3 points apart: a PDF generator often draws
        // one border as two slightly offset segments, which would enclose a sliver of a column
        .withMinColumnWidth(3f)
        // the same, for horizontal borders
        .withMinRowHeight(3f)
        // tolerate a 4 point gap between two aligned rulings, and between a ruling and the
        // perpendicular border it should meet
        .withMaxGapBetweenAlignedHorizontalRulings(4)
        .withMaxGapBetweenAlignedVerticalRulings(4);
```

`SpreadsheetExtractionAlgorithm.neolegalDefaults()` returns the configuration NeoLegal runs in
production, equivalent to `withCellAutocompletion(true).withCellTextOverflowRatio(0.01f)`.

### Upgrading from 1.0.x

1.1.0 rebases the fork onto current upstream, and two things change for callers:

- **The fork's behaviours are now opt-in.** Up to 1.0.12 cell autocompletion and the 1% text
  overflow were always on. `new SpreadsheetExtractionAlgorithm()` now reproduces upstream exactly;
  use `SpreadsheetExtractionAlgorithm.neolegalDefaults()` to get the 1.0.x behaviour back.
- **PDFBox 2.0.31 → 3.0.4.** If your own code touches `PDDocument` around tabula, `PDDocument.load(f)`
  becomes `Loader.loadPDF(f)`. See PDFBox's [migration guide](https://pdfbox.apache.org/3.0/migration.html).

### Other differences from upstream

The fork also differs in a few ways that are not options:

- it compiles and runs on **Java 17** (upstream targets Java 8);
- `slf4j-simple` is a test dependency, so the library imposes no logger implementation on the
  applications that use it — bring your own binding;
- `Ruling.collapseOrientedRulings` no longer edits the rulings it is handed: `Page` hands out the
  very rulings it caches, so two successive extractions of the same page used not to start from the
  same state.

## API Usage Examples

A simple Java code example which extracts all rows and cells from all tables of all pages of a PDF document:

```java
InputStream in = this.getClass().getResourceAsStream("my.pdf");
// PDFBox 3 loads documents through Loader, not PDDocument.load
try (PDDocument document = Loader.loadPDF(in.readAllBytes())) {
    SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
    PageIterator pi = new ObjectExtractor(document).extract();
    while (pi.hasNext()) {
        // iterate over the pages of the document
        Page page = pi.next();
        List<Table> table = sea.extract(page);
        // iterate over the tables of the page
        for(Table tables: table) {
            List<List<RectangularTextContainer>> rows = tables.getRows();
            // iterate over the rows of the table
            for (List<RectangularTextContainer> cells : rows) {
                // print all column-cells of the row plus linefeed
                for (RectangularTextContainer content : cells) {
                    // Note: Cell.getText() uses \r to concat text chunks
                    String text = content.getText().replace("\r", " ");
                    System.out.print(text + "|");
                }
                System.out.println();
            }
        }
    }
}
```


For more detail information check the Javadoc. 
The Javadoc API documentation can be generated (see also '_Building from Source_' section) via

```
mvn javadoc:javadoc
```

which generates the HTML files to directory ```target/site/apidocs/```

## Building from Source

Clone this repo and run:

```
mvn clean compile assembly:single
```

## Contributing

Interested in helping out? We'd love to have your help!

You can help by:

- [Reporting a bug](https://github.com/neolegal-fr/tabula-java/issues) — or [upstream](https://github.com/tabulapdf/tabula-java/issues) if it is not specific to this fork.
- Adding or editing documentation.
- Contributing code via a Pull Request.
- Spreading the word about `tabula-java` to people who might be able to benefit from using it.

### Backers

You can also support our continued work on `tabula-java` with a one-time or monthly donation [on OpenCollective](https://opencollective.com/tabulapdf#support). Organizations who use `tabula-java` can also [sponsor the project](https://opencollective.com/tabulapdf#support) for acknowledgement on [our official site](http://tabula.technology/) and this README.

Special thanks to the following users and organizations for generously supporting Tabula with donations and grants:

<a href="https://opencollective.com/tabulapdf/backer/0/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/0/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/1/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/1/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/2/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/2/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/3/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/3/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/4/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/4/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/5/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/5/avatar"></a>

<a title="The John S. and James L. Knight Foundation" href="http://www.knightfoundation.org/" target="_blank"><img alt="The John S. and James L. Knight Foundation" src="https://knightfoundation.org/wp-content/uploads/2019/10/KF_Logotype_Icon-and-Stacked-Name.png" width="300"></a>
<a title="The Shuttleworth Foundation" href="https://shuttleworthfoundation.org/" target="_blank"><img width="200" alt="The Shuttleworth Foundation" src="https://raw.githubusercontent.com/tabulapdf/tabula/gh-pages/shuttleworth.jpg"></a>
