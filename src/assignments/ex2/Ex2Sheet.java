package assignments.ex2;

import java.io.*;

/**
 * Implementation of the Ex2Sheet class, representing a spreadsheet.
 */
public class Ex2Sheet implements Sheet {
    private final Cell[][] table; // 2D array of cells representing the spreadsheet

    /**
     * Constructs a new spreadsheet with specified dimensions.
     * @param x Number of columns in the spreadsheet.
     * @param y Number of rows in the spreadsheet.
     */
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL); // Initialize each cell with an empty value
            }
        }
        eval(); // Evaluate the spreadsheet initially
    }

    /**
     * Constructs a new spreadsheet with default dimensions.
     */
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    /**
     * Returns the string value of the cell at the specified coordinates.
     * @param x Column index of the cell.
     * @param y Row index of the cell.
     * @return Value of the cell as a string.
     */
    @Override
    public String value(int x, int y) {
        Cell cell = get(x, y);
        return (cell != null) ? cell.toString() : Ex2Utils.EMPTY_CELL; // Return cell value or empty string if null
    }

    /**
     * Retrieves the cell at the specified coordinates.
     * @param x Column index of the cell.
     * @param y Row index of the cell.
     * @return The cell object at the specified coordinates.
     */
    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    /**
     * Retrieves a cell based on its string reference (e.g., "A1").
     * @param cords String representation of cell coordinates.
     * @return The cell object corresponding to the reference.
     */
    @Override
    public Cell get(String cords) {
        cords = cords.toUpperCase(); // Normalize to uppercase for case insensitivity
        int col = cords.charAt(0) - 'A'; // Convert column letter to index
        int row = Integer.parseInt(cords.substring(1)); // Convert row number to index
        return isIn(col, row) ? get(col, row) : null; // Return cell if within bounds
    }

    /**
     * Returns the number of columns in the spreadsheet.
     * @return Number of columns.
     */
    @Override
    public int width() {
        return table.length;
    }

    /**
     * Returns the number of rows in the spreadsheet.
     * @return Number of rows.
     */
    @Override
    public int height() {
        return table[0].length;
    }

    /**
     * Sets the value of a cell at the specified coordinates.
     * @param x Column index of the cell.
     * @param y Row index of the cell.
     * @param s Value to set in the cell.
     */
    @Override
    public void set(int x, int y, String s) {
        if (isIn(x, y)) {
            table[x][y] = new SCell(s); // Create a new cell with the given value
            eval(); // Re-evaluate the spreadsheet after setting a value
        }
    }

    /**
     * Evaluates all cells in the spreadsheet, resolving formulas and detecting cycles.
     */
    @Override
    public void eval() {
        int[][] depthArray = depth(); // Calculate the depth of each cell
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                Cell cell = get(x, y);
                if (cell instanceof SCell) {
                    SCell scell = (SCell) cell;
                    if (depthArray[x][y] == Ex2Utils.ERR) {
                        scell.setType(Ex2Utils.ERR_CYCLE_FORM); // Mark as a cyclic dependency
                        scell.setComputedValue(Ex2Utils.ERR_CYCLE);
                    } else {
                        scell.setOrder(depthArray[x][y]); // Set the evaluation order
                        if (scell.getType() == Ex2Utils.FORM) {
                            scell.computeValue(this); // Compute the value if it's a formula
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the specified coordinates are within the bounds of the spreadsheet.
     * @param xx Column index.
     * @param yy Row index.
     * @return True if the coordinates are within bounds, otherwise false.
     */
    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && xx < width() && yy >= 0 && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()]; // Array to store depths
        boolean[][] visited = new boolean[width()][height()]; // Tracks visited cells during depth calculation
        boolean[][] inPath = new boolean[width()][height()]; // Tracks cells in the current recursion path for cycle detection

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (!visited[x][y]) {
                    depths[x][y] = calculateDepth(x, y, visited, inPath, depths); // Calculate depth for each cell
                }
            }
        }

        return depths;
    }

    /**
     * Recursively calculates the depth of a cell, detecting cyclic dependencies.
     * @param x Column index of the cell.
     * @param y Row index of the cell.
     * @param visited Array to track visited cells to avoid redundant calculations.
     * @param inPath Array to track the current path for detecting cycles.
     * @param depths Array to store computed depths.
     * @return Depth of the cell or an error code (-1) if a cycle is detected.
     */
    private int calculateDepth(int x, int y, boolean[][] visited, boolean[][] inPath, int[][] depths) {
        if (inPath[x][y]) {
            return Ex2Utils.ERR; // Cycle detected
        }
        if (visited[x][y]) {
            return depths[x][y]; // Return previously calculated depth
        }

        inPath[x][y] = true; // Mark this cell as part of the current path
        visited[x][y] = true; // Mark this cell as visited

        Cell cell = get(x, y);
        if (!(cell instanceof SCell)) {
            inPath[x][y] = false;
            return 0; // Non-formula cells have zero depth
        }

        SCell scell = (SCell) cell;
        if (scell.getType() != Ex2Utils.FORM) {
            inPath[x][y] = false;
            return 0; // Non-formula cells have zero depth
        }

        int maxDepth = 0;
        String data = scell.getData().substring(1).replaceAll("\\s", ""); // Remove '=' and spaces

        String[] tokens = data.split("[+\\-*/()]"); // Split formula into tokens by operators and parentheses
        for (String token : tokens) {
            token = token.trim().toUpperCase(); // Normalize token to uppercase
            if (token.matches("[A-Z]+\\d+")) { // Check if the token is a valid cell reference
                int col = token.charAt(0) - 'A';
                int row = Integer.parseInt(token.substring(1)); // Convert reference to indices
                if (isIn(col, row)) {
                    int depDepth = calculateDepth(col, row, visited, inPath, depths); // Recursively calculate depth of dependencies
                    if (depDepth == Ex2Utils.ERR) {
                        inPath[x][y] = false;
                        depths[x][y] = Ex2Utils.ERR; // Mark the cell as part of a cycle
                        return Ex2Utils.ERR; // Propagate error if dependency is cyclic
                    }
                    maxDepth = Math.max(maxDepth, depDepth); // Update max depth among dependencies
                }
            }
        }

        inPath[x][y] = false; // Mark this cell as no longer part of the current path
        depths[x][y] = maxDepth + 1; // Add 1 to include the current cell in the depth
        return depths[x][y];
    }



    /**
     * Loads the spreadsheet data from a file. Clears the current spreadsheet before loading.
     * Each line in the file represents a cell's data in the format: x,y,value.
     *
     * @param fileName Name of the file to load.
     * @throws IOException If an error occurs while reading the file.
     */
    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Clear all existing cells in the spreadsheet
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    set(x, y, Ex2Utils.EMPTY_CELL); // Reset each cell to empty
                }
            }

            String line;
            while ((line = reader.readLine()) != null) { // Read each line from the file
                line = line.trim(); // Remove any leading/trailing spaces

                // Skip header lines and empty lines
                if (!line.isEmpty() && !line.startsWith("I2CS")) {
                    String[] parts = line.split(",", 3); // Split into x, y, and value
                    if (parts.length >= 3) {
                        try {
                            int x = Integer.parseInt(parts[0].trim()); // Parse x-coordinate
                            int y = Integer.parseInt(parts[1].trim()); // Parse y-coordinate
                            String value = parts[2].trim(); // Extract the cell value

                            if (isIn(x, y)) { // Validate cell coordinates
                                set(x, y, value); // Assign the value to the specified cell
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping invalid line: " + line); // Log lines with invalid numbers
                        }
                    } else {
                        System.err.println("Skipping malformed line: " + line); // Log malformed lines
                    }
                }
            }

            // Re-evaluate the spreadsheet after loading data
            eval();
        } catch (IOException e) {
            throw new IOException("Error loading file: " + fileName, e); // Handle file-related errors
        }
    }

    /**
     * Saves the current spreadsheet to a file. Only non-empty cells are saved.
     * Each cell's data is saved in the format: x,y,value.
     *
     * @param fileName Name of the file to save.
     * @throws IOException If an error occurs while writing to the file.
     */
    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    Cell cell = get(x, y); // Retrieve the cell
                    if (cell != null && !cell.getData().equals(Ex2Utils.EMPTY_CELL)) { // Only save non-empty cells
                        writer.write(x + "," + y + "," + cell.getData()); // Write cell coordinates and value
                        writer.newLine(); // Move to the next line
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error saving to file: " + fileName, e); // Handle file-related errors
        }
    }

    @Override
    public String eval(int x, int y) {
        return value(x, y);
    }
}