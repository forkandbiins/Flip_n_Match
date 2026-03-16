import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public final class GameController {
    private final MainFrame mainFrame;
    private final DifficultyController difficultyController;
    private final GamePanel gamePanel;
    private GameModel model;
    private int detonatedRow = -1;
    private int detonatedColumn = -1;

    public GameController(MainFrame mainFrame, DifficultyController difficultyController, GamePanel gamePanel) {
        this.mainFrame = mainFrame;
        this.difficultyController = difficultyController;
        this.gamePanel = gamePanel;
    }

    public void initialize() {
        gamePanel.getBackButton().addActionListener(e -> difficultyController.showView());
    }

    public void showGame(Difficulty difficulty) {
        model = new GameModel(difficulty);
        detonatedRow = -1;
        detonatedColumn = -1;

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

        boolean revealAllMines = false;
        switch (result) {
            case MINE -> {
                detonatedRow = row;
                detonatedColumn = column;
                revealAllMines = true;
                gamePanel.setStatusText("Boom! You hit a mine.");
            }
            case WON ->
                gamePanel.setStatusText("You cleared all safe tiles!");
            case SAFE ->
                gamePanel.setStatusText("Safe tiles left: " + model.getRemainingSafeTileCount());
            default -> {
                // ALREADY_REVEALED is returned earlier, no-op for completeness.
            }
        }

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
        model.toggleFlag(row, column);
        refreshBoard(false);
        event.consume();
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