public enum Difficulty {
    EASY("Easy", 8, 8, 10),
    MEDIUM("Medium", 10, 10, 15),
    HARD("Hard", 12, 12, 25);

    private final String label;
    private final int rowCount;
    private final int columnCount;
    private final int mineCount;

    Difficulty(String label, int rowCount, int columnCount, int mineCount) {
        this.label = label;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.mineCount = mineCount;
    }

    public String getLabel() {
        return label;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getMineCount() {
        return mineCount;
    }

    public String getButtonLabel() {
        return label;
    }
}