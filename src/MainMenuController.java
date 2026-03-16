import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public final class MainMenuController {
    private final MainFrame mainFrame;
    private final MainMenuPanel mainMenuPanel;
    private final Runnable onPlaySelected;
    private final Runnable onSettingsSelected;

    public MainMenuController(
            MainFrame mainFrame,
            MainMenuPanel mainMenuPanel,
            Runnable onPlaySelected,
            Runnable onSettingsSelected) {
        this.mainFrame = mainFrame;
        this.mainMenuPanel = mainMenuPanel;
        this.onPlaySelected = onPlaySelected;
        this.onSettingsSelected = onSettingsSelected;
    }

    public void initialize() {
        bindActions();
        setupKeyboardNavigation();
        onViewShown();
    }

    public void onViewShown() {
        SwingUtilities.invokeLater(() -> mainMenuPanel.getPlayButton().requestFocusInWindow());
    }

    private void bindActions() {
        mainMenuPanel.getPlayButton().addActionListener(e -> onPlaySelected.run());
        mainMenuPanel.getSettingsButton().addActionListener(e -> onSettingsSelected.run());
        mainMenuPanel.getExitButton().addActionListener(e -> onExitSelected());
    }

    private void setupKeyboardNavigation() {
        List<JButton> orderedButtons = Arrays.asList(
                mainMenuPanel.getPlayButton(),
                mainMenuPanel.getSettingsButton(),
                mainMenuPanel.getExitButton());

        for (int i = 0; i < orderedButtons.size(); i++) {
            JButton button = orderedButtons.get(i);
            JButton previousButton = orderedButtons.get((i - 1 + orderedButtons.size()) % orderedButtons.size());
            JButton nextButton = orderedButtons.get((i + 1) % orderedButtons.size());

            bindFocusKey(button, "focusPrevious", previousButton,
                    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));

            bindFocusKey(button, "focusNext", nextButton,
                    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));

            bindClickKey(button, "activateWithEnter", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            bindClickKey(button, "activateWithSpace", KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        }
    }

    private void bindFocusKey(JButton sourceButton, String actionKey, JButton targetButton, KeyStroke... keyStrokes) {
        InputMap inputMap = sourceButton.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = sourceButton.getActionMap();
        String scopedActionKey = actionKey + sourceButton.getText();

        for (KeyStroke keyStroke : keyStrokes) {
            inputMap.put(keyStroke, scopedActionKey);
        }

        actionMap.put(scopedActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                targetButton.requestFocusInWindow();
            }
        });
    }

    private void bindClickKey(JButton button, String actionKey, KeyStroke keyStroke) {
        InputMap inputMap = button.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = button.getActionMap();
        String scopedActionKey = actionKey + button.getText();

        inputMap.put(keyStroke, scopedActionKey);
        actionMap.put(scopedActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                button.doClick();
            }
        });
    }

    private void onExitSelected() {
        mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
    }
}
