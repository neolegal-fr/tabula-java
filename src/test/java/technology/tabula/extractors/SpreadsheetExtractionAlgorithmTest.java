package technology.tabula.extractors;

import org.junit.jupiter.api.Test;
import technology.tabula.Cell;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpreadsheetExtractionAlgorithmTest {
    @Test
    public void testIsWithin() {
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 0, 20));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 10, 20));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 0, 10));

        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 20, 0));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 20, 0));
        assertTrue(SpreadsheetExtractionAlgorithm.isWithin(10, 0, 10));

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
    public void findGaps_whenMissingCellBetweenTwoAlignedOnes() {
        // Missing cell between two cells aligned
        Cell cell = new Cell(10, 50, 10, 10);
        Cell lefterCell = new Cell(10, 30, 10, 10);

        List<Cell> actual = SpreadsheetExtractionAlgorithm.findGaps(List.of(cell, lefterCell));
        assertEquals(1, actual.size());
        Cell expected = new Cell(10, 40, 10, 10);
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void findGaps_whenMissingCellAtTheBeginningOfTheRow() {
        // Missing a the beginning of the row
        Cell cell = new Cell(10, 50, 10, 10);
        Cell topCell = new Cell(0, 50, 10, 10);
        Cell topLeftCell = new Cell(0, 40, 10, 10);

        // ___________
        // |____|____|
        //      |____|

        List<Cell> actual = SpreadsheetExtractionAlgorithm.findGaps(List.of(cell, topCell, topLeftCell));
        assertEquals(1, actual.size());
        Cell expected = new Cell(10, 40, 10, 10);
        assertEquals(expected, actual.get(0));
    }

}
