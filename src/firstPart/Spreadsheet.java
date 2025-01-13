package firstPart;

/**
 * Represents a spreadsheet with cells arranged in a grid.
 * Provides methods to set and retrieve cell values, evaluate cell content,
 * and calculate cell dependencies and depths.
 */
public class Spreadsheet {
    private final int width; // Number of columns in the spreadsheet
    private final int height; // Number of rows in the spreadsheet
    private final Cell[][] cells; // 2D array of cells in the spreadsheet

    /**
     * Constructs a new spreadsheet with the specified dimensions.
     * @param width Number of columns in the spreadsheet
     * @param height Number of rows in the spreadsheet
     */
    public Spreadsheet(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height]; // Initialize the 2D array of cells
    }

    /**
     * Sets the content of a specific cell.
     * @param x The column index of the cell
     * @param y The row index of the cell
     * @param c The cell object to set at the specified location
     */
    public void setCell(int x, int y, Cell c) {
        cells[x][y] = c; // Place the cell at the specified position in the array
    }

    /**
     * Retrieves the cell at the specified location.
     * @param x The column index of the cell
     * @param y The row index of the cell
     * @return The cell at the specified location
     */
    public Cell get(int x, int y) {
        return cells[x][y]; // Return the cell at the given coordinates
    }

    /**
     * Retrieves a cell based on its string reference (e.g., "A1").
     * @param ref The reference string of the cell
     * @return The cell corresponding to the reference
     */
    public Cell getCellByReference(String ref) {
        int height = ref.charAt(0) - 'A'; // Convert column letter to index
        int width = Integer.parseInt(ref.substring(1)) - 1; // Convert row number to index
        return get(width, height); // Retrieve the cell using the converted indices
    }

    /**
     * Gets the width of the spreadsheet.
     * @return The number of columns in the spreadsheet
     */
    public int width() {
        return width; // Return the number of columns
    }

    /**
     * Gets the height of the spreadsheet.
     * @return The number of rows in the spreadsheet
     */
    public int height() {
        return height; // Return the number of rows
    }

    /**
     * Converts a column letter to its zero-based index.
     * @param c The column letter reference
     * @return The zero-based index of the column, or -1 if invalid
     */
    public int xCell(String c) {
        if (c.length() < 2 || !Character.isDigit(c.charAt(1))) { // Ensure the reference has at least two characters
            return -1;
        }
        char col = c.charAt(0);
        if (col < 'A' || col > 'Z') { // Check if the column letter is valid
            return -1;
        }
        return col - 'A'; // Convert column letter to zero-based index
    }

    /**
     * Converts a row number in a reference to its zero-based index.
     * @param c The reference string
     * @return The zero-based index of the row, or -1 if invalid
     */
    public int yCell(String c) {
        try {
            int y = Integer.parseInt(c.substring(1)) - 1; // Convert row number to zero-based index
            return (y >= 0 && y < height) ? y : -1; // Validate the row index is within bounds
        } catch (NumberFormatException e) {
            return -1; // Return -1 for invalid numeric conversion
        }
    }

    /**
     * Evaluates the value of the cell at the specified location.
     * @param x The column index of the cell
     * @param y The row index of the cell
     * @return The evaluated value of the cell, or null if the cell is empty
     */
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        return (cell == null) ? null : cell.resolveValue(); // Evaluate cell value
    }

    /**
     * Evaluates the entire spreadsheet and returns a 2D array of results.
     * @return A 2D array containing the evaluated values of all cells
     */
    public String[][] evalAll() {
        String[][] result = new String[height][width]; // Initialize result array
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Cell cell = cells[j][i]; // Access each cell
                result[i][j] = (cell == null) ? null : eval(j, i); // Evaluate or set null for empty cells
            }
        }
        return result;
    }

    private int calculateDepth(Cell cell, int[][] state, int x, int y) {
        if (cell == null || cell.isNumber(cell.getContent()) || cell.isText(cell.getContent())) {
            return 0; // Return depth 0 for simple content like numbers or text
        }

        if (state[x][y] == 1) {
            return -1; // If the cell is currently being visited, a cycle is detected
        }

        if (state[x][y] == 2) {
            return 0; // If the cell is fully processed, no need to recalculate
        }

        state[x][y] = 1; // Mark the cell as currently being visited
        String expression = cell.getContent().substring(1); // Remove the '=' sign from the formula
        int maxDepth = 0;

        for (String token : expression.split("[+\\-*/()]")) { // Split formula into tokens using operators and parentheses
            token = token.trim();
            if (!token.isEmpty() && token.matches("[A-Z]+\\d+")) { // Check if the token is a valid cell reference (e.g., "A1")
                int refX = xCell(token); // Convert the column letter to an index
                int refY = yCell(token); // Convert the row number to an index

                if (refX >= 0 && refY >= 0) { // Ensure the indices are valid
                    Cell referencedCell = get(refX, refY); // Retrieve the referenced cell
                    if (referencedCell != null) {
                        int depth = calculateDepth(referencedCell, state, refX, refY); // Recursively calculate the depth
                        if (depth == -1) {
                            return -1; // If a cycle is detected in a dependency, propagate it
                        }
                        maxDepth = Math.max(maxDepth, depth); // Track the maximum depth among dependencies
                    }
                }
            }
        }

        state[x][y] = 2; // Mark the cell as fully processed
        return maxDepth + 1; // Add 1 to include the current cell in the depth
    }

    // Overloaded method for external use
    public int[][] depth() {
        int[][] result = new int[height][width]; // Initialize the depth array for the spreadsheet
        int[][] state = new int[width][height]; // State array to track visited cells

        for (int x = 0; x < width; x++) { // Loop through all columns
            for (int y = 0; y < height; y++) { // Loop through all rows
                Cell cell = cells[x][y]; // Access each cell
                if (cell != null) {
                    result[y][x] = calculateDepth(cell, state, x, y); // Calculate depth for each cell
                }
            }
        }

        return result; // Return the depth array
    }
}