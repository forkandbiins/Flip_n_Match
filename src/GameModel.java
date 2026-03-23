import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class GameModel {
    private static final char[] CARD_PLACEHOLDERS = "ABCDEFGHIJKL".toCharArray();
    private static final int EASY_MIN_EMPTY_TILE_TARGET = 10;
    private static final int MEDIUM_MIN_EMPTY_TILE_TARGET = 14;
    private static final int HARD_MIN_EMPTY_TILE_TARGET = 24;
    private static final int MAX_PLACEMENT_ATTEMPTS = 5000;
    private static final int MAX_FIRST_CLICK_ATTEMPTS = 20000;

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
    private final int flagLimit;
    private final boolean[][] mineGrid;
    private final boolean[][] revealedGrid;
    private final boolean[][] flaggedGrid;
    private final char[][] cardLetterGrid;
    private final boolean[][] revealedCardFaceGrid;
    private final boolean[][] matchedCardGrid;
    private final int totalPairCount;
    private int revealedSafeTileCount;
    private int flaggedTileCount;
    private int matchedPairCount;
    private boolean firstRevealResolved;
    private boolean gameOver;
    private boolean won;

    public GameModel(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.rowCount = difficulty.getRowCount();
        this.columnCount = difficulty.getColumnCount();
        this.mineCount = difficulty.getMineCount();
        this.flagLimit = getFlagLimitForDifficulty(difficulty);
        this.totalPairCount = getPairCountForDifficulty(difficulty);

        int totalTiles = rowCount * columnCount;
        if (mineCount <= 0 || mineCount >= totalTiles) {
            throw new IllegalArgumentException("Mine count must be between 1 and total tiles - 1");
        }

        mineGrid = new boolean[rowCount][columnCount];
        revealedGrid = new boolean[rowCount][columnCount];
        flaggedGrid = new boolean[rowCount][columnCount];
        cardLetterGrid = new char[rowCount][columnCount];
        revealedCardFaceGrid = new boolean[rowCount][columnCount];
        matchedCardGrid = new boolean[rowCount][columnCount];
        placeMinesRandomly();
        placeCardPairsOnEmptyTiles(-1, -1, new Random());
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

    public int getMineCount() {
        return mineCount;
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

    public boolean isCardTile(int row, int column) {
        return cardLetterGrid[row][column] != '\0';
    }

    public boolean isCardMatched(int row, int column) {
        return matchedCardGrid[row][column];
    }

    public boolean isCardFaceRevealed(int row, int column) {
        return revealedCardFaceGrid[row][column];
    }

    public char getCardLetter(int row, int column) {
        return cardLetterGrid[row][column];
    }

    public int getRemainingPairCount() {
        return totalPairCount - matchedPairCount;
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

    public int getRemainingFlagCount() {
        return flagLimit - flaggedTileCount;
    }

    public RevealResult revealTile(int row, int column) {
        if (!isInsideBoard(row, column) || flaggedGrid[row][column] || gameOver) {
            return RevealResult.ALREADY_REVEALED;
        }

        if (!firstRevealResolved) {
            configureBoardForFirstReveal(row, column);
            firstRevealResolved = true;
        }

        if (isCardTile(row, column)) {
            if (matchedCardGrid[row][column]) {
                return RevealResult.ALREADY_REVEALED;
            }

            if (!revealedGrid[row][column]) {
                revealSafeArea(row, column);
                return RevealResult.SAFE;
            }

            if (!revealedCardFaceGrid[row][column]) {
                revealedCardFaceGrid[row][column] = true;
                return RevealResult.SAFE;
            }

            return RevealResult.ALREADY_REVEALED;
        }

        if (revealedGrid[row][column]) {
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

    public void hideCardTile(int row, int column) {
        if (!isInsideBoard(row, column) || !isCardTile(row, column) || matchedCardGrid[row][column] || gameOver) {
            return;
        }

        // Return the card back to its discovered-empty "X" state.
        revealedGrid[row][column] = true;
        revealedCardFaceGrid[row][column] = false;
    }

    public boolean markCardsMatched(int firstRow, int firstColumn, int secondRow, int secondColumn) {
        if (!isInsideBoard(firstRow, firstColumn)
                || !isInsideBoard(secondRow, secondColumn)
                || !isCardTile(firstRow, firstColumn)
                || !isCardTile(secondRow, secondColumn)
                || matchedCardGrid[firstRow][firstColumn]
                || matchedCardGrid[secondRow][secondColumn]
                || !revealedCardFaceGrid[firstRow][firstColumn]
                || !revealedCardFaceGrid[secondRow][secondColumn]) {
            return false;
        }

        if (cardLetterGrid[firstRow][firstColumn] != cardLetterGrid[secondRow][secondColumn]) {
            return false;
        }

        matchedCardGrid[firstRow][firstColumn] = true;
        matchedCardGrid[secondRow][secondColumn] = true;
        revealedGrid[firstRow][firstColumn] = true;
        revealedGrid[secondRow][secondColumn] = true;
        revealedCardFaceGrid[firstRow][firstColumn] = true;
        revealedCardFaceGrid[secondRow][secondColumn] = true;
        matchedPairCount++;

        if (matchedPairCount >= totalPairCount) {
            gameOver = true;
            won = true;
        }

        return true;
    }

    public boolean toggleFlag(int row, int column) {
        if (!isInsideBoard(row, column) || revealedGrid[row][column] || gameOver) {
            return flaggedGrid[row][column];
        }

        if (!flaggedGrid[row][column] && flaggedTileCount >= flagLimit) {
            return false;
        }

        boolean nowFlagged = !flaggedGrid[row][column];
        flaggedGrid[row][column] = nowFlagged;
        flaggedTileCount += nowFlagged ? 1 : -1;
        return flaggedGrid[row][column];
    }

    private int getFlagLimitForDifficulty(Difficulty selectedDifficulty) {
        return switch (selectedDifficulty) {
            case EASY -> 10;
            case MEDIUM -> 15;
            case HARD -> 25;
        };
    }

    private void placeMinesRandomly() {
        Random random = new Random();
        int minimumEmptyTileTarget = getMinimumEmptyTileTarget();
        boolean[][] bestMineGrid = new boolean[rowCount][columnCount];
        int bestEmptyTileCount = -1;

        for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
            clearMineGrid();
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

            int emptyTileCount = countEmptyTiles();
            if (emptyTileCount > bestEmptyTileCount) {
                copyGrid(mineGrid, bestMineGrid);
                bestEmptyTileCount = emptyTileCount;
            }

            if (emptyTileCount >= minimumEmptyTileTarget) {
                return;
            }
        }

        // Keep the closest valid candidate if target cannot be reached within retry budget.
        copyGrid(bestMineGrid, mineGrid);
    }

    private void configureBoardForFirstReveal(int firstRow, int firstColumn) {
        Random random = new Random();
        int requiredEmptyTiles = Math.max(getMinimumEmptyTileTarget(), (totalPairCount * 2) + 1);

        for (int attempt = 0; attempt < MAX_FIRST_CLICK_ATTEMPTS; attempt++) {
            clearMineGrid();
            clearCardState();

            if (!placeMinesWithReservedFirstClickArea(firstRow, firstColumn, random)) {
                continue;
            }

            if (countEmptyTiles() < requiredEmptyTiles) {
                continue;
            }

            if (!placeCardPairsOnEmptyTiles(firstRow, firstColumn, random)) {
                continue;
            }

            return;
        }

        // Extended retry budget to preserve the first-click guarantee.
        for (int attempt = 0; attempt < MAX_FIRST_CLICK_ATTEMPTS * 4; attempt++) {
            clearMineGrid();
            clearCardState();

            if (!placeMinesWithReservedFirstClickArea(firstRow, firstColumn, random)) {
                continue;
            }

            if (countEmptyTiles() < requiredEmptyTiles) {
                continue;
            }

            if (!placeCardPairsOnEmptyTiles(firstRow, firstColumn, random)) {
                continue;
            }

            return;
        }

        throw new IllegalStateException("Unable to satisfy first-click safety and card constraints");
    }

    private boolean placeMinesWithReservedFirstClickArea(int firstRow, int firstColumn, Random random) {
        int placedMineCount = 0;
        int maxTries = rowCount * columnCount * 20;
        int tries = 0;

        while (placedMineCount < mineCount && tries < maxTries) {
            tries++;
            int row = random.nextInt(rowCount);
            int column = random.nextInt(columnCount);

            if (mineGrid[row][column]) {
                continue;
            }

            if (Math.abs(row - firstRow) <= 1 && Math.abs(column - firstColumn) <= 1) {
                continue;
            }

            mineGrid[row][column] = true;
            placedMineCount++;
        }

        return placedMineCount == mineCount;
    }

    private int getMinimumEmptyTileTarget() {
        return switch (difficulty) {
            case EASY -> EASY_MIN_EMPTY_TILE_TARGET;
            case MEDIUM -> MEDIUM_MIN_EMPTY_TILE_TARGET;
            case HARD -> HARD_MIN_EMPTY_TILE_TARGET;
        };
    }

    private int countEmptyTiles() {
        int emptyTileCount = 0;
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                if (!mineGrid[row][column] && getAdjacentMineCount(row, column) == 0) {
                    emptyTileCount++;
                }
            }
        }
        return emptyTileCount;
    }

    private int getPairCountForDifficulty(Difficulty selectedDifficulty) {
        return switch (selectedDifficulty) {
            case EASY -> 5;
            case MEDIUM -> 7;
            case HARD -> 12;
        };
    }

    private boolean placeCardPairsOnEmptyTiles(int excludedRow, int excludedColumn, Random random) {
        List<int[]> emptyTileCoordinates = new ArrayList<>();
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                if (!mineGrid[row][column]
                        && getAdjacentMineCount(row, column) == 0
                        && !(row == excludedRow && column == excludedColumn)) {
                    emptyTileCoordinates.add(new int[] {row, column});
                }
            }
        }

        int neededCardTiles = totalPairCount * 2;
        if (emptyTileCoordinates.size() < neededCardTiles) {
            return false;
        }

        Collections.shuffle(emptyTileCoordinates, random);
        List<Character> letterPool = new ArrayList<>();
        for (int pairIndex = 0; pairIndex < totalPairCount; pairIndex++) {
            char letter = CARD_PLACEHOLDERS[pairIndex];
            letterPool.add(letter);
            letterPool.add(letter);
        }
        Collections.shuffle(letterPool, random);

        for (int i = 0; i < neededCardTiles; i++) {
            int[] coordinate = emptyTileCoordinates.get(i);
            cardLetterGrid[coordinate[0]][coordinate[1]] = letterPool.get(i);
        }

        return true;
    }

    private void clearCardState() {
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                cardLetterGrid[row][column] = '\0';
                revealedCardFaceGrid[row][column] = false;
                matchedCardGrid[row][column] = false;
            }
        }
        matchedPairCount = 0;
    }

    private void clearMineGrid() {
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                mineGrid[row][column] = false;
            }
        }
    }

    private void copyGrid(boolean[][] source, boolean[][] target) {
        for (int row = 0; row < rowCount; row++) {
            System.arraycopy(source[row], 0, target[row], 0, columnCount);
        }
    }

    private void revealSafeArea(int startRow, int startColumn) {
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startRow, startColumn});

        while (!queue.isEmpty()) {
            int[] cell = queue.removeFirst();
            int row = cell[0];
            int column = cell[1];

            if (!isInsideBoard(row, column)
                    || revealedGrid[row][column]
                    || mineGrid[row][column]) {
                continue;
            }

            if (isCardTile(row, column)) {
                if (flaggedGrid[row][column]) {
                    flaggedGrid[row][column] = false;
                    if (flaggedTileCount > 0) {
                        flaggedTileCount--;
                    }
                }

                // Discover neighboring card-empty cells as X during flood reveal.
                revealedGrid[row][column] = true;

                for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                    for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                        if (rowOffset == 0 && columnOffset == 0) {
                            continue;
                        }
                        queue.add(new int[] {row + rowOffset, column + columnOffset});
                    }
                }
                continue;
            }

            if (flaggedGrid[row][column]) {
                flaggedGrid[row][column] = false;
                if (flaggedTileCount > 0) {
                    flaggedTileCount--;
                }
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