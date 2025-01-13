package firstPart;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CellTest {

    @Test
    void isNumberTest() {
        Cell cell = new Cell("", null);
        assertTrue(cell.isNumber("0")); // Test with zero
        assertTrue(cell.isNumber("123.456")); // Test with a decimal number
        assertFalse(cell.isNumber("12.34.56")); // Test with multiple decimals
        assertFalse(cell.isNumber("123abc")); // Test with alphanumeric content
    }

    @Test
    void isTextTest() {
        Cell cell = new Cell("", null);
        assertTrue(cell.isText("Hello World")); // Plain text
        assertTrue(cell.isText("Special chars #$%")); // Text with special characters
        assertFalse(cell.isText("=(A1+A2)")); // Formula
        assertFalse(cell.isText("123")); // Numeric string
    }

    @Test
    void isFormTest() {
        Cell cell = new Cell("", null);
        assertTrue(cell.isForm("=A1+A2")); // Basic formula
        assertTrue(cell.isForm("=(A1+A2)*B3")); // Formula with parentheses
        assertFalse(cell.isForm("123")); // Numeric value
        assertFalse(cell.isForm("")); // Empty string
    }

    @Test
    void resolveValueTest() {
        Spreadsheet sheet = new Spreadsheet(2, 2);
        Cell numericCell = new Cell("42", sheet);
        assertEquals("42", numericCell.resolveValue()); // Numeric value

        Cell formulaCell = new Cell("=1+1", sheet);
        assertEquals("2.0", formulaCell.resolveValue()); // Simple formula

        Cell refCell = new Cell("5", sheet);
        sheet.setCell(0, 0, refCell);
    }

    @Test
    void computeFormTest() {
        Spreadsheet sheet = new Spreadsheet(1, 1);
        Cell cell = new Cell("=(3+2)*(4/2)", sheet); // Complex formula with parentheses
        sheet.setCell(0, 0, cell);

        // Verify that computeForm evaluates the expression correctly
        assertEquals(10.0, cell.computeForm(cell.getContent())); // The expected result is 10.0
    }
}