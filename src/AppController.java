import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class AppController {
    private ThemeMode themeMode = ThemeMode.DARK;

    public void start() {
        SwingUtilities.invokeLater(() -> {
            applySystemLookAndFeel();
            MainFrame mainFrame = new MainFrame();
            MainMenuPanel mainMenuPanel = mainFrame.getMainMenuPanel();
            DifficultyPanel difficultyPanel = new DifficultyPanel();
            GamePanel gamePanel = new GamePanel();
            SettingsPanel settingsPanel = new SettingsPanel();

            final DifficultyController[] difficultyControllerRef = new DifficultyController[1];
            final GameController[] gameControllerRef = new GameController[1];
            final SettingsController[] settingsControllerRef = new SettingsController[1];

            MainMenuController mainMenuController = new MainMenuController(
                    mainFrame,
                    mainMenuPanel,
                    () -> difficultyControllerRef[0].showView(),
                    () -> settingsControllerRef[0].showView());

            DifficultyController difficultyController = new DifficultyController(
                    mainFrame,
                    mainMenuController,
                    mainMenuPanel,
                    difficultyPanel,
                    difficulty -> gameControllerRef[0].showGame(difficulty));
            GameController gameController = new GameController(mainFrame, difficultyController, gamePanel);
            SettingsController settingsController = new SettingsController(
                    mainFrame,
                    mainMenuController,
                    mainMenuPanel,
                    settingsPanel,
                    () -> themeMode,
                    nextThemeMode -> {
                        themeMode = nextThemeMode;
                        applyThemeToPanels(mainMenuPanel, difficultyPanel, gamePanel, settingsPanel);
                    });

            difficultyControllerRef[0] = difficultyController;
            gameControllerRef[0] = gameController;
            settingsControllerRef[0] = settingsController;

            applyThemeToPanels(mainMenuPanel, difficultyPanel, gamePanel, settingsPanel);

            mainMenuController.initialize();
            difficultyController.initialize();
            gameController.initialize();
            settingsController.initialize();
            mainFrame.showWindow();
        });
    }

    private void applyThemeToPanels(
            MainMenuPanel mainMenuPanel,
            DifficultyPanel difficultyPanel,
            GamePanel gamePanel,
            SettingsPanel settingsPanel) {
        mainMenuPanel.setThemeMode(themeMode);
        difficultyPanel.setThemeMode(themeMode);
        gamePanel.setThemeMode(themeMode);
        settingsPanel.setThemeMode(themeMode);
    }

    private void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ignored) {
            // Keep the default look and feel when system style is unavailable.
        }
    }
}
