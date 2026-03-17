import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

public final class SettingsController {
    private final MainFrame mainFrame;
    private final MainMenuController mainMenuController;
    private final MainMenuPanel mainMenuPanel;
    private final SettingsPanel settingsPanel;
    private final Supplier<ThemeMode> themeModeSupplier;
    private final Consumer<ThemeMode> onThemeModeSelected;

    public SettingsController(
            MainFrame mainFrame,
            MainMenuController mainMenuController,
            MainMenuPanel mainMenuPanel,
            SettingsPanel settingsPanel,
            Supplier<ThemeMode> themeModeSupplier,
            Consumer<ThemeMode> onThemeModeSelected) {
        this.mainFrame = mainFrame;
        this.mainMenuController = mainMenuController;
        this.mainMenuPanel = mainMenuPanel;
        this.settingsPanel = settingsPanel;
        this.themeModeSupplier = themeModeSupplier;
        this.onThemeModeSelected = onThemeModeSelected;
    }

    public void initialize() {
        settingsPanel.getBackButton().addActionListener(e -> onBackSelected());
        settingsPanel.getToggleThemeButton().addActionListener(e -> onThemeToggleSelected());
    }

    public void showView() {
        settingsPanel.setThemeMode(themeModeSupplier.get());
        mainFrame.showPanel(settingsPanel);
        SwingUtilities.invokeLater(() -> settingsPanel.getBackButton().requestFocusInWindow());
    }

    private void onBackSelected() {
        mainFrame.showPanel(mainMenuPanel);
        mainMenuController.onViewShown();
    }

    private void onThemeToggleSelected() {
        ThemeMode nextThemeMode = themeModeSupplier.get().toggled();
        onThemeModeSelected.accept(nextThemeMode);
        settingsPanel.setThemeMode(nextThemeMode);
    }
}
