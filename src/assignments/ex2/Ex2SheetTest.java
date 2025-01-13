package assignments.ex2;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Ex2Sheet.
 */
public class Ex2SheetTest {

    @Test
    void valueTest() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "Hello");
        sheet.set(1, 1, "123");

        assertEquals("Hello", sheet.value(0, 0));
        assertEquals("123", sheet.value(1, 1));
        assertEquals(Ex2Utils.EMPTY_CELL, sheet.value(2, 2));
    }

    @Test
    void setAndGetTest() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A0+5");

        assertEquals("5", sheet.get(0, 0).getData());
        assertEquals("=A0+5", sheet.get(0, 1).getData());
    }

    @Test
    void evalTest() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A0+5");
        sheet.eval();

        assertEquals("5", sheet.eval(0, 0));
        assertEquals("10.0", sheet.eval(0, 1));
    }

    @Test
    void isInTest() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        assertTrue(sheet.isIn(0, 0));
        assertTrue(sheet.isIn(2, 2));
        assertFalse(sheet.isIn(3, 3));
        assertFalse(sheet.isIn(-1, -1));
    }

    @Test
    void depthTest() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "1");
        sheet.set(1, 0, "=A0+2");
        sheet.set(2, 0, "=B0*3");
        sheet.eval();

        int[][] depth = sheet.depth();
        assertEquals(0, depth[0][0]); // A0 has no dependencies
        assertEquals(1, depth[1][0]); // B0 depends on A0
        assertEquals(2, depth[2][0]); // C0 depends on B0
    }

    @Test
    void loadTest() throws IOException {
        String fileName = "testLoad.txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("I2CS ArielU: SpreadSheet (Ex2) assignment\n");
            writer.write("0,0,1\n");
            writer.write("0,1,=A0+2\n");
            writer.write("1,0,Hello\n");
        }

        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.load(fileName);

        assertEquals("1", sheet.value(0, 0));
        assertEquals("3.0", sheet.value(0, 1)); // A0 + 2
        assertEquals("Hello", sheet.value(1, 0));

        // Clean up
        new File(fileName).delete();
    }

    @Test
    void saveTest() throws IOException {
        String fileName = "testSave.txt";
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "1");
        sheet.set(0, 1, "=A0+2");
        sheet.set(1, 0, "Hello");
        sheet.save(fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            assertEquals("0,0,1", reader.readLine());
            assertEquals("0,1,=A0+2", reader.readLine());
            assertEquals("1,0,Hello", reader.readLine());
        }

        // Clean up
        new File(fileName).delete();
    }
}
