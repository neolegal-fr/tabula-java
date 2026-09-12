package technology.tabula.extractors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import technology.tabula.Cell;
import technology.tabula.Page;
import technology.tabula.PageDims;
import technology.tabula.Rectangle;
import technology.tabula.RectangleSpatialIndex;
import technology.tabula.Ruling;
import technology.tabula.Table;
import technology.tabula.TextElement;
import technology.tabula.UtilsForTesting;

public class SpreadsheetExtractionAlgorithmTest {

    @Test
    public void testIsWithin() {
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 0, 20));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 10, 20));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 0, 10));

        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 20, 0));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 20, 10));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 10, 0));

        assertFalse(SpreadsheetExtractionAlgorithm.isWithin(10, 0, 9));
        assertFalse(SpreadsheetExtractionAlgorithm.isWithin(10, 11, 20));
        assertFalse(SpreadsheetExtractionAlgorithm.isWithin(10, 9, 0));
        assertFalse(SpreadsheetExtractionAlgorithm.isWithin(10, 20, 11));
    }

    @Test
    public void sameRow_whenTrue() {
        Cell cell = new Cell(10, 50, 10, 10);
        Cell leftCell = new Cell(10, 40, 10, 10);
        Cell lefterCell = new Cell(10, 30, 10, 10);
        Cell rightCell = new Cell(10, 60, 10, 10);
        Cell righterCell = new Cell(10, 70, 10, 10);

        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(cell, leftCell));
        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(cell, lefterCell));
        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(cell, rightCell));
        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(cell, righterCell));
    }

    @Test
    public void sameRow_whenFalse() {
        Cell cell = new Cell(10, 50, 10, 10);
        Cell topCell = new Cell(0, 40, 10, 10);
        Cell bottomCell = new Cell(20, 40, 10, 10);

        assertFalse(SpreadsheetExtractionAlgorithm.sameRow(cell, topCell));
        assertFalse(SpreadsheetExtractionAlgorithm.sameRow(cell, bottomCell));
    }

    @Test
    public void sameRow_whenRowsOnlyPartiallyOverlap() {
        // a spanning cell covers the rows of the cells on its side
        Cell tallCell = new Cell(10, 50, 10, 30);
        Cell firstRowCell = new Cell(10, 40, 10, 10);
        Cell lastRowCell = new Cell(30, 40, 10, 10);

        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(tallCell, firstRowCell));
        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(tallCell, lastRowCell));
        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(firstRowCell, tallCell));
        assertTrue(SpreadsheetExtractionAlgorithm.sameRow(lastRowCell, tallCell));
    }

    @Test
    public void findGaps_whenMissingCellBetweenTwoAlignedOnes() {
        Cell cell = new Cell(10, 50, 10, 10);
        Cell lefterCell = new Cell(10, 30, 10, 10);

        List<Cell> actual = SpreadsheetExtractionAlgorithm.findGaps(Arrays.asList(cell, lefterCell));
        assertEquals(1, actual.size());
        assertEquals(new Cell(10, 40, 10, 10), actual.get(0));
    }

    @Test
    public void findGaps_whenMissingCellAtTheBeginningOfTheRow() {
        Cell cell = new Cell(10, 50, 10, 10);
        Cell topCell = new Cell(0, 50, 10, 10);
        Cell topLeftCell = new Cell(0, 40, 10, 10);

        // ___________
        // |____|____|
        //      |____|

        List<Cell> actual = SpreadsheetExtractionAlgorithm.findGaps(Arrays.asList(cell, topCell, topLeftCell));
        assertEquals(1, actual.size());
        assertEquals(new Cell(10, 40, 10, 10), actual.get(0));
    }

    @Test
    public void findGaps_whenTableIsComplete() {
        List<Cell> cells = Arrays.asList(
                new Cell(0, 40, 10, 10), new Cell(0, 50, 10, 10),
                new Cell(10, 40, 10, 10), new Cell(10, 50, 10, 10));

        assertTrue(SpreadsheetExtractionAlgorithm.findGaps(cells).isEmpty());
    }

    @Test
    public void findGaps_whenNoCell() {
        assertTrue(SpreadsheetExtractionAlgorithm.findGaps(new ArrayList<>()).isEmpty());
    }

    /**
     * The leading cells of that table have no border of their own, so their text is dropped unless
     * the missing cells are rebuilt.
     */
    @Test
    public void cellAutocompletion_recoversACellWithoutItsOwnBorders() throws IOException {
        Page page = UtilsForTesting.getAreaFromPage(
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf", 1,
                150.56f, 58.9f, 654.7f, 536.12f);

        Table withoutAutocompletion = new SpreadsheetExtractionAlgorithm().extract(page).get(0);
        assertEquals(6, withoutAutocompletion.getColCount());
        assertEquals("", withoutAutocompletion.getCell(1, 0).getText());

        Table withAutocompletion = new SpreadsheetExtractionAlgorithm()
                .withCellAutocompletion(true).extract(page).get(0);
        assertEquals(7, withAutocompletion.getColCount());
        assertEquals("PRODUCTS", withAutocompletion.getCell(1, 0).getText());
    }

    /**
     * That page draws the leftmost border of its table twice, 2 points apart, which leaves a sliver
     * of a column between the two.
     */
    @Test
    public void minColumnWidth_mergesTheTwoHalvesOfADoubleDrawnBorder() throws IOException {
        Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/20.pdf", 1);

        float[] leftsWithoutMerging = {105.549774f, 107.52332f, 160.58167f, 377.1792f, 434.95804f, 488.21783f};
        assertEquals(leftsWithoutMerging.length, page.getVerticalRulings().size());
        for (int i = 0; i < leftsWithoutMerging.length; i++) {
            assertEquals(leftsWithoutMerging[i], page.getVerticalRulings().get(i).getLeft(), 0.1);
        }

        List<Ruling> merged = Ruling.collapseOrientedRulings(new ArrayList<>(page.getVerticalRulings()),
                Ruling.COLINEAR_OR_PARALLEL_PIXEL_EXPAND_AMOUNT, Ruling.PERPENDICULAR_PIXEL_EXPAND_AMOUNT, 3f);
        merged.sort((a, b) -> Float.compare(a.getLeft(), b.getLeft()));

        // the two borders become one, placed at the average of their positions
        float[] leftsWithMerging = {106.536545f, 160.58167f, 377.1792f, 434.95804f, 488.21783f};
        assertEquals(leftsWithMerging.length, merged.size());
        for (int i = 0; i < leftsWithMerging.length; i++) {
            assertEquals(leftsWithMerging[i], merged.get(i).getLeft(), 0.1);
        }
    }

    @Test
    public void minColumnWidth_leavesGenuinelyDistinctColumnsAlone() throws IOException {
        Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/20.pdf", 1);

        // 1 point apart is below the gap between the two halves of the double-drawn border
        List<Ruling> merged = Ruling.collapseOrientedRulings(new ArrayList<>(page.getVerticalRulings()),
                Ruling.COLINEAR_OR_PARALLEL_PIXEL_EXPAND_AMOUNT, Ruling.PERPENDICULAR_PIXEL_EXPAND_AMOUNT, 1f);

        assertEquals(page.getVerticalRulings().size(), merged.size());
    }

    /**
     * A cell whose right border is drawn over the last letter of its content: the letter is only
     * collected if the cell is widened a little first.
     */
    @Test
    public void cellTextOverflowRatio_catchesALetterOverlappedByTheRightBorder() {
        Page page = pageWithOneCellClippingItsLastLetter();

        assertEquals("AB", new SpreadsheetExtractionAlgorithm()
                .extract(page).get(0).getCell(0, 0).getText());
        assertEquals("ABC", new SpreadsheetExtractionAlgorithm().withCellTextOverflowRatio(0.05f)
                .extract(page).get(0).getCell(0, 0).getText());
    }

    /**
     * A single 100x20 cell holding "ABC", the "C" straddling the right border of the cell.
     */
    private static Page pageWithOneCellClippingItsLastLetter() {
        List<Ruling> rulings = Arrays.asList(
                new Ruling(new Point2D.Float(10f, 10f), new Point2D.Float(110f, 10f)),
                new Ruling(new Point2D.Float(10f, 30f), new Point2D.Float(110f, 30f)),
                new Ruling(new Point2D.Float(10f, 10f), new Point2D.Float(10f, 30f)),
                new Ruling(new Point2D.Float(110f, 10f), new Point2D.Float(110f, 30f)));

        List<TextElement> textElements = new ArrayList<>();
        String text = "ABC";
        for (int i = 0; i < text.length(); i++) {
            // "A" and "B" sit inside the cell, "C" ends 2 points past its right border (110)
            textElements.add(new TextElement(14f, 82f + i * 10f, 10f, 12f, null, 12f,
                    String.valueOf(text.charAt(i)), 10f));
        }

        RectangleSpatialIndex<TextElement> index = new RectangleSpatialIndex<>();
        for (TextElement textElement : textElements) {
            index.add(textElement);
        }

        return Page.Builder.newInstance()
                .withPageDims(PageDims.of(0, 0, 200, 200))
                .withNumber(1)
                .withRulings(rulings)
                .withTextElements(textElements)
                .withMinCharWidth(10f)
                .withMinCharHeight(12f)
                .withIndex(index)
                .build();
    }

    @Test
    public void neolegalDefaults_enablesAutocompletionAndTextOverflow() throws IOException {
        Page page = UtilsForTesting.getAreaFromPage(
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf", 1,
                150.56f, 58.9f, 654.7f, 536.12f);

        Table table = SpreadsheetExtractionAlgorithm.neolegalDefaults().extract(page).get(0);
        assertEquals(7, table.getColCount());
        assertEquals("PRODUCTS", table.getCell(1, 0).getText());
    }

    @Test
    public void defaultConfiguration_matchesTheUpstreamCellDetection() throws IOException {
        Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/spanning_cells.pdf", 1);

        List<Rectangle> viaTheDefaultConfiguration = new ArrayList<>(
                new SpreadsheetExtractionAlgorithm().extract(page));
        List<Rectangle> viaTheStaticEntryPoint = SpreadsheetExtractionAlgorithm.findSpreadsheetsFromCells(
                SpreadsheetExtractionAlgorithm.findCells(page.getHorizontalRulings(), page.getVerticalRulings()));

        assertEquals(viaTheStaticEntryPoint.size(), viaTheDefaultConfiguration.size());
    }
}
