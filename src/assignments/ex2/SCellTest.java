package assignments.ex2;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test class for SCell.
 */
public class SCellTest {

    private SCell cell;

    @BeforeEach
    void setUp() {
        cell = new SCell(Ex2Utils.EMPTY_CELL);
    }

    @Test
    void setDataTest() {
        cell.setData("123");
        assertEquals("123", cell.getData());
        assertEquals(Ex2Utils.NUMBER, cell.getType());

        cell.setData("=1+2");
        assertEquals("=1+2", cell.getData());
        assertEquals(Ex2Utils.FORM, cell.getType());

        cell.setData("Invalid");
        assertEquals("Invalid", cell.getData());
        assertEquals(Ex2Utils.TEXT, cell.getType());

        cell.setData("=(2+3)*4");
        assertEquals("=(2+3)*4", cell.getData());
        assertEquals(Ex2Utils.FORM, cell.getType());
    }

    @Test
    void computeValueTest() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5"); // A0
        sheet.set(1, 1, "3"); // B1

        cell.setData("=A0+B1");
        cell.computeValue(sheet);
        assertEquals("8.0", cell.toString());

        cell.setData("=(A0*B1)+(2)");
        cell.computeValue(sheet);
        assertEquals("17.0", cell.toString());
    }

    @Test
    void EvaluateFormulaWithNestedParenthesesTest() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10"); // A0
        sheet.set(1, 1, "5"); // B1

        cell.setData("=(A0-B1)*2");
        cell.computeValue(sheet);
        assertEquals("10.0", cell.toString());

        cell.setData("=(A0+(B1*2))/2");
        cell.computeValue(sheet);
        assertEquals("10.0", cell.toString());
    }

    @Test
    void toStringTest() {
        cell.setData("123");
        assertEquals("123", cell.toString());

        cell.setData("=1+2");
        cell.computeValue(new Ex2Sheet(5, 5));
        assertEquals("=1+2", cell.getData());
    }
}