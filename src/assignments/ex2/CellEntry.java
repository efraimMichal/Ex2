package assignments.ex2;

/**
 * Implementation of CellEntry class for indexing.
 */
public class CellEntry implements Index2D {
    private final int x;
    private final int y;

    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isValid() {
        return x >= 0 && y >= 0;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}