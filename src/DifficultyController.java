import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public final class DifficultyController {
    private final MainFrame mainFrame;
    private final MainMenuController mainMenuController;
    private final MainMenuPanel mainMenuPanel;
    private final DifficultyPanel difficultyPanel;
    private final Consumer<Difficulty> onDifficultySelected;

    public DifficultyController(
            MainFrame mainFrame,
            MainMenuController mainMenuController,
            MainMenuPanel mainMenuPanel,
            DifficultyPanel difficultyPanel,
            Consumer<Difficulty> onDifficultySelected) {
        this.mainFrame = mainFrame;
        this.mainMenuController = mainMenuController;
        this.mainMenuPanel = mainMenuPanel;
        this.difficultyPanel = difficultyPanel;
        this.onDifficultySelected = onDifficultySelected;
    }

    public void initialize() {
        difficultyPanel.getBackButton().addActionListener(e -> onBackSelected());
        difficultyPanel.getEasyButton().addActionListener(e -> onDifficultySelected(Difficulty.EASY));
        difficultyPanel.getMediumButton().addActionListener(e -> onDifficultySelected(Difficulty.MEDIUM));
        difficultyPanel.getHardButton().addActionListener(e -> onDifficultySelected(Difficulty.HARD));
    }

    public void showView() {
        mainFrame.showPanel(difficultyPanel);
        SwingUtilities.invokeLater(() -> difficultyPanel.getBackButton().requestFocusInWindow());
    }

    private void onBackSelected() {
        mainFrame.showPanel(mainMenuPanel);
        mainMenuController.onViewShown();
    }

    private void onDifficultySelected(Difficulty difficulty) {
        onDifficultySelected.accept(difficulty);
    }
}