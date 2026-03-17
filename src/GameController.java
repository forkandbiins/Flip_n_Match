import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class GameController {
    private final MainFrame mainFrame;
    private final DifficultyController difficultyController;
    private final GamePanel gamePanel;
    private GameModel model;
    private Difficulty currentDifficulty;
    private int detonatedRow = -1;
    private int detonatedColumn = -1;
    private Timer gameTimer;
    private int elapsedSeconds;
    private boolean timerStarted;

    public GameController(MainFrame mainFrame, DifficultyController difficultyController, GamePanel gamePanel) {
        this.mainFrame = mainFrame;
        this.difficultyController = difficultyController;
        this.gamePanel = gamePanel;
    }

    public void initialize() {
        gamePanel.getBackButton().addActionListener(e -> {
            stopTimer();
            difficultyController.showView();
        });
        gamePanel.getRestartButton().addActionListener(e -> {
            if (currentDifficulty != null) {
                showGame(currentDifficulty);
            }
        });
    }

    public void showGame(Difficulty difficulty) {
        currentDifficulty = difficulty;
        stopTimer();
        model = new GameModel(difficulty);
        detonatedRow = -1;
        detonatedColumn = -1;
        elapsedSeconds = 0;
        timerStarted = false;

        gamePanel.buildBoard(
                model.getRowCount(),
                model.getColumnCount(),
                e -> onTileSelected(e.getActionCommand()),
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent event) {
                        maybeToggleFlag(event);
                    }
                });
        gamePanel.setTitleText("Minesweeper - " + difficulty.getLabel());
        gamePanel.setStatusText("Safe tiles left: " + model.getRemainingSafeTileCount());
        gamePanel.setRemainingFlagCount(model.getRemainingFlagCount());
        gamePanel.setElapsedTimeSeconds(elapsedSeconds);
        refreshBoard(false);

        mainFrame.showPanel(gamePanel);
        SwingUtilities.invokeLater(gamePanel::requestBoardFocus);
    }

    private void onTileSelected(String actionCommand) {
        String[] parts = actionCommand.split(":", 2);
        int row = Integer.parseInt(parts[0]);
        int column = Integer.parseInt(parts[1]);

        GameModel.RevealResult result = model.revealTile(row, column);
        if (result == GameModel.RevealResult.ALREADY_REVEALED) {
            return;
        }

        startTimerIfNeeded();

        boolean revealAllMines = false;
        switch (result) {
            case MINE -> {
                detonatedRow = row;
                detonatedColumn = column;
                revealAllMines = true;
                gamePanel.setStatusText("Boom! You hit a mine.");
                stopTimer();
            }
            case WON -> {
                gamePanel.setStatusText("You cleared all safe tiles!");
                stopTimer();
            }
            case SAFE ->
                gamePanel.setStatusText("Safe tiles left: " + model.getRemainingSafeTileCount());
            default -> {
                // ALREADY_REVEALED is returned earlier, no-op for completeness.
            }
        }

        gamePanel.setRemainingFlagCount(model.getRemainingFlagCount());
        refreshBoard(revealAllMines);
    }

    private void maybeToggleFlag(MouseEvent event) {
        if (!event.isPopupTrigger() && !SwingUtilities.isRightMouseButton(event)) {
            return;
        }

        if (model == null || model.isGameOver()) {
            return;
        }

        if (!(event.getComponent() instanceof javax.swing.JButton tileButton)) {
            return;
        }

        String[] parts = tileButton.getActionCommand().split(":", 2);
        int row = Integer.parseInt(parts[0]);
        int column = Integer.parseInt(parts[1]);
        boolean wasFlagged = model.isFlagged(row, column);
        boolean isFlagged = model.toggleFlag(row, column);

        if (!wasFlagged && !isFlagged && model.getRemainingFlagCount() == 0) {
            gamePanel.setStatusText("Flag limit reached (10).");
        }

        gamePanel.setRemainingFlagCount(model.getRemainingFlagCount());
        refreshBoard(false);
        event.consume();
    }

    private void startTimerIfNeeded() {
        if (timerStarted || model == null || model.isGameOver()) {
            return;
        }

        timerStarted = true;
        gameTimer = new Timer(1000, event -> {
            elapsedSeconds++;
            gamePanel.setElapsedTimeSeconds(elapsedSeconds);
        });
        gameTimer.start();
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
        timerStarted = false;
    }

    private void refreshBoard(boolean revealAllMines) {
        boolean shouldRevealAllMines = revealAllMines || (model.isGameOver() && !model.isWon());
        int rows = model.getRowCount();
        int columns = model.getColumnCount();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (model.isRevealed(row, column)) {
                    if (model.isMine(row, column)) {
                        gamePanel.setTileMine(row, column, row == detonatedRow && column == detonatedColumn);
                    } else {
                        int adjacentMineCount = model.getAdjacentMineCount(row, column);
                        gamePanel.setTileRevealed(row, column, adjacentMineCount);
                    }
                    continue;
                }

                if (model.isFlagged(row, column)) {
                    if (shouldRevealAllMines && model.isMine(row, column)) {
                        gamePanel.setTileMine(row, column, false);
                    } else {
                        gamePanel.setTileFlagged(row, column);
                    }
                    continue;
                }

                if (shouldRevealAllMines && model.isMine(row, column)) {
                    gamePanel.setTileMine(row, column, false);
                } else {
                    gamePanel.setTileHidden(row, column, !model.isGameOver());
                }
            }
        }
    }
}