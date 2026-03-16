import java.util.ArrayDeque;
import java.util.Random;

public final class GameModel {
    public enum RevealResult {
        ALREADY_REVEALED,
        SAFE,
        MINE,
        WON
    }

    private final Difficulty difficulty;
    private final int rowCount;
    private final int columnCount;
    private final int mineCount;
    private final boolean[][] mineGrid;
    private final boolean[][] revealedGrid;
    private final boolean[][] flaggedGrid;
    private int revealedSafeTileCount;
    private boolean gameOver;
    private boolean won;

    public GameModel(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.rowCount = difficulty.getRowCount();
        this.columnCount = difficulty.getColumnCount();
        this.mineCount = difficulty.getMineCount();

        int totalTiles = rowCount * columnCount;
        if (mineCount <= 0 || mineCount >= totalTiles) {
            throw new IllegalArgumentException("Mine count must be between 1 and total tiles - 1");
        }

        mineGrid = new boolean[rowCount][columnCount];
        revealedGrid = new boolean[rowCount][columnCount];
        flaggedGrid = new boolean[rowCount][columnCount];
        placeMinesRandomly();
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public boolean isMine(int row, int column) {
        return mineGrid[row][column];
    }

    public boolean isRevealed(int row, int column) {
        return revealedGrid[row][column];
    }

    public boolean isFlagged(int row, int column) {
        return flaggedGrid[row][column];
    }

    public int getAdjacentMineCount(int row, int column) {
        int mineTotal = 0;
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                if (rowOffset == 0 && columnOffset == 0) {
                    continue;
                }

                int nextRow = row + rowOffset;
                int nextColumn = column + columnOffset;
                if (isInsideBoard(nextRow, nextColumn) && mineGrid[nextRow][nextColumn]) {
                    mineTotal++;
                }
            }
        }
        return mineTotal;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }

    public int getRemainingSafeTileCount() {
        int safeTileCount = (rowCount * columnCount) - mineCount;
        return safeTileCount - revealedSafeTileCount;
    }

    public RevealResult revealTile(int row, int column) {
        if (!isInsideBoard(row, column) || revealedGrid[row][column] || flaggedGrid[row][column] || gameOver) {
            return RevealResult.ALREADY_REVEALED;
        }

        if (mineGrid[row][column]) {
            revealedGrid[row][column] = true;
            gameOver = true;
            won = false;
            return RevealResult.MINE;
        }

        revealSafeArea(row, column);
        if (getRemainingSafeTileCount() == 0) {
            gameOver = true;
            won = true;
            return RevealResult.WON;
        }

        return RevealResult.SAFE;
    }

    public boolean toggleFlag(int row, int column) {
        if (!isInsideBoard(row, column) || revealedGrid[row][column] || gameOver) {
            return flaggedGrid[row][column];
        }

        flaggedGrid[row][column] = !flaggedGrid[row][column];
        return flaggedGrid[row][column];
    }

    private void placeMinesRandomly() {
        Random random = new Random();
        int placedMineCount = 0;

        while (placedMineCount < mineCount) {
            int row = random.nextInt(rowCount);
            int column = random.nextInt(columnCount);
            if (mineGrid[row][column]) {
                continue;
            }

            mineGrid[row][column] = true;
            placedMineCount++;
        }
    }

    private void revealSafeArea(int startRow, int startColumn) {
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startRow, startColumn});

        while (!queue.isEmpty()) {
            int[] cell = queue.removeFirst();
            int row = cell[0];
            int column = cell[1];

            if (!isInsideBoard(row, column) || revealedGrid[row][column] || mineGrid[row][column]) {
                continue;
            }

            revealedGrid[row][column] = true;
            revealedSafeTileCount++;

            if (getAdjacentMineCount(row, column) > 0) {
                continue;
            }

            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                    if (rowOffset == 0 && columnOffset == 0) {
                        continue;
                    }
                    queue.add(new int[] {row + rowOffset, column + columnOffset});
                }
            }
        }
    }

    private boolean isInsideBoard(int row, int column) {
        return row >= 0 && row < rowCount && column >= 0 && column < columnCount;
    }
}