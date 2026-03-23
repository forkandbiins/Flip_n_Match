import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class GameController {
    private static final int MISMATCH_REVEAL_DELAY_MS = 450;

    private final MainFrame mainFrame;
    private final DifficultyController difficultyController;
    private final GamePanel gamePanel;
    private GameModel model;
    private Difficulty currentDifficulty;
    private int detonatedRow = -1;
    private int detonatedColumn = -1;
    private Timer gameTimer;
    private Timer mismatchHideTimer;
    private int elapsedSeconds;
    private boolean timerStarted;
    private int firstCardRow = -1;
    private int firstCardColumn = -1;
    private boolean cardInputLocked;

    private boolean winDialogShown = false;
    private boolean lossDialogShown = false;

    public GameController(MainFrame mainFrame, DifficultyController difficultyController, GamePanel gamePanel) {
        this.mainFrame = mainFrame;
        this.difficultyController = difficultyController;
        this.gamePanel = gamePanel;
    }

    public void initialize() {
        gamePanel.getBackButton().addActionListener(e -> {
            stopTimer();
            stopMismatchHideTimer();
            difficultyController.showView();
        });
        gamePanel.getRestartButton().addActionListener(e -> {
            if (currentDifficulty != null) {
                stopMismatchHideTimer();
                showGame(currentDifficulty);
            }
        });
    }

    public void showGame(Difficulty difficulty) {
        currentDifficulty = difficulty;
        stopTimer();
        stopMismatchHideTimer();
        model = new GameModel(difficulty);
        detonatedRow = -1;
        detonatedColumn = -1;
        elapsedSeconds = 0;
        timerStarted = false;
        firstCardRow = -1;
        firstCardColumn = -1;
        cardInputLocked = false;

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
        gamePanel.setStatusText("Pairs left: " + model.getRemainingPairCount());
        gamePanel.setRemainingFlagCount(model.getRemainingFlagCount());
        gamePanel.setElapsedTimeSeconds(elapsedSeconds);
        refreshBoard(false);

        mainFrame.showPanel(gamePanel);
        SwingUtilities.invokeLater(gamePanel::requestBoardFocus);
    }

    private void onTileSelected(String actionCommand) {
        if (model == null || model.isGameOver() || cardInputLocked) {
            return;
        }

        String[] parts = actionCommand.split(":", 2);
        int row = Integer.parseInt(parts[0]);
        int column = Integer.parseInt(parts[1]);

        if (firstCardRow == row
                && firstCardColumn == column
                && model.isCardTile(row, column)
                && model.isCardFaceRevealed(row, column)
                && !model.isCardMatched(row, column)) {
            model.hideCardTile(row, column);
            firstCardRow = -1;
            firstCardColumn = -1;
            gamePanel.setStatusText("Card flipped back. Pairs left: " + model.getRemainingPairCount());
            refreshBoard(false);
            return;
        }

        if (firstCardRow >= 0 && !model.isCardTile(row, column)) {
            model.hideCardTile(firstCardRow, firstCardColumn);
            firstCardRow = -1;
            firstCardColumn = -1;
        }

        GameModel.RevealResult result = model.revealTile(row, column);
        if (result == GameModel.RevealResult.ALREADY_REVEALED) {
            refreshBoard(false);
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
                handleLoss();
            }

            case WON -> {
                gamePanel.setStatusText("You matched all pairs!");
                stopTimer();
                handleWin();
            }
            case SAFE -> {
                if (model.isCardTile(row, column)) {
                    handleCardSelection(row, column);
                } else {
                    gamePanel.setStatusText("Pairs left: " + model.getRemainingPairCount());
                }
            }
            default -> {
                // ALREADY_REVEALED is returned earlier, no-op for completeness.
            }
        }

        gamePanel.setRemainingFlagCount(model.getRemainingFlagCount());
        refreshBoard(revealAllMines);
    }

    private void handleCardSelection(int row, int column) {
        if (!model.isCardFaceRevealed(row, column)) {
            gamePanel.setStatusText("Empty tile found. Click X again to reveal its letter.");
            return;
        }

        if (firstCardRow < 0) {
            firstCardRow = row;
            firstCardColumn = column;
            gamePanel.setStatusText("Card " + model.getCardLetter(row, column) + " revealed. Pick its pair.");
            return;
        }

        if (firstCardRow == row && firstCardColumn == column) {
            return;
        }

        boolean matched = model.markCardsMatched(firstCardRow, firstCardColumn, row, column);
        if (matched) {
            firstCardRow = -1;
            firstCardColumn = -1;
            if (model.isWon()) {
                gamePanel.setStatusText("You matched all pairs!");
                stopTimer();
                handleWin();
            } else {
                gamePanel.setStatusText("Match found! Pairs left: " + model.getRemainingPairCount());
            }
        } else {
            int previousRow = firstCardRow;
            int previousColumn = firstCardColumn;
            firstCardRow = -1;
            firstCardColumn = -1;
            cardInputLocked = true;
            gamePanel.setStatusText("No pair. Comparing...");

            mismatchHideTimer = new Timer(MISMATCH_REVEAL_DELAY_MS, event -> {
                model.hideCardTile(previousRow, previousColumn);
                model.hideCardTile(row, column);
                stopMismatchHideTimer();
                gamePanel.setStatusText("No pair. Both cards flipped back to X.");
                refreshBoard(false);
            });
            mismatchHideTimer.setRepeats(false);
            mismatchHideTimer.start();
        }
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

        if (model.isWon()) {
            gamePanel.setStatusText("You flagged all mines!");
            stopTimer();
        } else if (!wasFlagged && !isFlagged && model.getRemainingFlagCount() == 0) {
            gamePanel.setStatusText("Flag limit reached.");
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

    private void stopMismatchHideTimer() {
        if (mismatchHideTimer != null) {
            mismatchHideTimer.stop();
            mismatchHideTimer = null;
        }
        cardInputLocked = false;
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
                    } else if (model.isCardTile(row, column)) {
                        if (model.isCardFaceRevealed(row, column)) {
                            gamePanel.setTileCardFace(row, column, model.getCardLetter(row, column), model.isCardMatched(row, column));
                        } else {
                            gamePanel.setTileCardDiscovered(row, column);
                        }
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

                if (model.isCardTile(row, column)) {
                    gamePanel.setTileCardHidden(row, column, !model.isGameOver());
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

    private void handleWin() {
        stopTimer();
        stopMismatchHideTimer();

        if (winDialogShown) {
            return;
        }
        winDialogShown = true;

        showModernDialog(
            "You matched all pairs!",
            "You Win",
            new java.awt.Color(56, 142, 60), // Green accent
            true
        );
    }

    private void handleLoss() {
        stopTimer();
        stopMismatchHideTimer();

        if (lossDialogShown) {
            return;
        }
        lossDialogShown = true;

        showModernDialog(
            "You hit a mine! Game over.",
            "Game Over",
            new java.awt.Color(211, 47, 47), // Red accent
            false
        );
    }

    private void showModernDialog(String message, String title, java.awt.Color accentColor, boolean isWin) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(mainFrame, title, true);
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(32, 32, 32, 32));

        javax.swing.JLabel iconLabel = new javax.swing.JLabel(isWin ? "\uD83C\uDFC6" : "\uD83D\uDCA3"); // Trophy or Bomb emoji
        iconLabel.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 48));
        iconLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        iconLabel.setForeground(accentColor);
        panel.add(iconLabel);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        javax.swing.JLabel messageLabel = new javax.swing.JLabel("<html><div style='text-align:center;'>" + message + "</div></html>");
        messageLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
        messageLabel.setForeground(java.awt.Color.DARK_GRAY);
        messageLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        messageLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 24, 0));
        panel.add(messageLabel);

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setBackground(java.awt.Color.WHITE);
        buttonPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        javax.swing.JButton newGameButton = new javax.swing.JButton("New Game");
        newGameButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        newGameButton.setBackground(accentColor);
        newGameButton.setForeground(java.awt.Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 24, 8, 24));
        newGameButton.addActionListener((java.awt.event.ActionEvent e) -> {
            dialog.dispose();
            if (currentDifficulty != null) {
                showGame(currentDifficulty);
            }
        });

        javax.swing.JButton exitButton = new javax.swing.JButton("Exit");
        exitButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        exitButton.setBackground(new java.awt.Color(120, 120, 120));
        exitButton.setForeground(java.awt.Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 24, 8, 24));
        exitButton.addActionListener((java.awt.event.ActionEvent e) -> {
            dialog.dispose();
            mainFrame.dispatchEvent(new java.awt.event.WindowEvent(mainFrame, java.awt.event.WindowEvent.WINDOW_CLOSING));
        });

        buttonPanel.add(newGameButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.requestFocus();
    }
}