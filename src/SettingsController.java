import javax.swing.SwingUtilities;

public final class SettingsController {
    private final MainFrame mainFrame;
    private final MainMenuController mainMenuController;
    private final MainMenuPanel mainMenuPanel;
    private final SettingsPanel settingsPanel;

    public SettingsController(
            MainFrame mainFrame,
            MainMenuController mainMenuController,
            MainMenuPanel mainMenuPanel,
            SettingsPanel settingsPanel) {
        this.mainFrame = mainFrame;
        this.mainMenuController = mainMenuController;
        this.mainMenuPanel = mainMenuPanel;
        this.settingsPanel = settingsPanel;
    }

    public void initialize() {
        settingsPanel.getBackButton().addActionListener(e -> onBackSelected());
    }

    public void showView() {
        mainFrame.showPanel(settingsPanel);
        SwingUtilities.invokeLater(() -> settingsPanel.getBackButton().requestFocusInWindow());
    }

    private void onBackSelected() {
        mainFrame.showPanel(mainMenuPanel);
        mainMenuController.onViewShown();
    }
}
