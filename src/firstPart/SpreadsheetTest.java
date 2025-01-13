package firstPart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpreadsheetTest {

    @Test
    void getCellByReference() {
        Spreadsheet sheet = new Spreadsheet(3, 3);
        Cell cell = new Cell("100", sheet);
        sheet.setCell(1, 1, cell); // Set cell at B2
        assertEquals(cell, sheet.getCellByReference("B2")); // Valid reference
        assertNull(sheet.getCellByReference("C3")); // Empty cell
    }

    @Test
    void xCell() {
        Spreadsheet sheet = new Spreadsheet(3, 3);
        assertEquals(0, sheet.xCell("A1")); // First column
        assertEquals(1, sheet.xCell("B1")); // Second column
        assertEquals(-1, sheet.xCell("AA1")); // Invalid reference
    }

    @Test
    void yCell() {
        Spreadsheet sheet = new Spreadsheet(3, 3);
        assertEquals(0, sheet.yCell("A1")); // First row
        assertEquals(2, sheet.yCell("A3")); // Third row
        assertEquals(-1, sheet.yCell("A0")); // Invalid row
    }

    @Test
    void eval() {
        Spreadsheet sheet = new Spreadsheet(2, 2);
        Cell cell = new Cell("123", sheet);
        sheet.setCell(0, 0, cell);
        assertEquals("123", sheet.eval(0, 0)); // Evaluate numeric cell
        assertNull(sheet.eval(1, 1)); // Evaluate empty cell
    }

    @Test
    void evalAll() {
        Spreadsheet sheet = new Spreadsheet(2, 2);
        sheet.setCell(0, 0, new Cell("10", sheet)); // A1 is a simple number
        sheet.setCell(1, 0, new Cell("20", sheet)); // B1 is a simple number
        sheet.setCell(0, 1, new Cell("=A1+B1", sheet)); // A2 is the sum of A1 and B1
        sheet.setCell(1, 1, new Cell("=A2*2", sheet)); // B2 is double the value of A2

        String[][] expected = {
                {"10", "20"},
                {"30.0", "60.0"} // A2 = 10 + 20, B2 = A2 * 2
        };
        assertArrayEquals(expected, sheet.evalAll()); // Verify all cell evaluations
    }

    @Test
    void depth() {
        Spreadsheet sheet = new Spreadsheet(2, 2);
        sheet.setCell(0, 0, new Cell("=A2", sheet)); // A1 depends on A2
        sheet.setCell(0, 1, new Cell("5", sheet)); // A2 is a constant

        int[][] expected = {
                {1, 0}, // A1 has depth 1, A2 has depth 0
                {0, 0}
        };
        assertArrayEquals(expected, sheet.depth());
    }

    @Test
    void calculateDepth() {
        Spreadsheet sheet = new Spreadsheet(2, 2);
        sheet.setCell(0, 0, new Cell("=A2", sheet)); // A1 depends on A2
        sheet.setCell(0, 1, new Cell("=A1", sheet)); // A2 depends on A1 (cyclic reference)

        assertEquals(-1, sheet.depth()[0][0]); // Detects cycle and returns -1
    }
}