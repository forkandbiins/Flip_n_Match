import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public final class MainFrame extends JFrame {
    private static final String TITLE = "Flip n Match";
    private static final int FRAME_WIDTH = 900;
    private static final int FRAME_HEIGHT = 640;
    private final MainMenuPanel mainMenuPanel;

    public MainFrame() {
        super(TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        mainMenuPanel = new MainMenuPanel(TITLE);
        setContentPane(mainMenuPanel);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public MainMenuPanel getMainMenuPanel() {
        return mainMenuPanel;
    }

    public void showPanel(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    public void showWindow() {
        setVisible(true);
    }
}
