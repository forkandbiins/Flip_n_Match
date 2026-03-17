import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class MainMenuPanel extends JPanel {
    // Global menu scaling controls.
    private static final double BASE_SCREEN_WIDTH = 1920.0;
    private static final double BASE_SCREEN_HEIGHT = 1080.0;
    private static final double MIN_UI_SCALE = 0.85;
    private static final double MAX_UI_SCALE = 1.35;

    // Base sizes at scale = 1.0.
    private static final int BASE_BUTTON_WIDTH = 460;
    private static final int BASE_BUTTON_HEIGHT = 84;
    private static final int BASE_BUTTON_FONT_SIZE = 30;
    private static final int BASE_TITLE_FONT_SIZE = 72;
    private static final int BASE_SUBTITLE_FONT_SIZE = 24;
    private static final int BASE_SECTION_GAP = 42;
    private static final int BASE_BUTTON_GAP = 22;

    private static final Color TITLE_COLOR = new Color(236, 241, 255);
    private static final Color SUBTITLE_COLOR = new Color(176, 193, 233);
    private static final Color BUTTON_TEXT_COLOR = new Color(233, 240, 255);
    private static final Color BUTTON_COLOR = new Color(38, 64, 109);

    private final JButton playButton;
    private final JButton settingsButton;
    private final JButton exitButton;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final Filler sectionGapFiller;
    private final Filler subtitleGapFiller;
    private final Filler firstButtonGapFiller;
    private final Filler secondButtonGapFiller;
    private ThemeMode themeMode = ThemeMode.DARK;

    public MainMenuPanel(String title) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        subtitleLabel = new JLabel("Minesweeper x Memory Match", SwingConstants.CENTER);
        subtitleLabel.setForeground(SUBTITLE_COLOR);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);

        playButton = createMenuButton("Play");
        settingsButton = createMenuButton("Settings");
        exitButton = createMenuButton("Exit");

        sectionGapFiller = createVerticalFiller(24);
        subtitleGapFiller = createVerticalFiller(14);
        firstButtonGapFiller = createVerticalFiller(16);
        secondButtonGapFiller = createVerticalFiller(16);

        add(Box.createVerticalGlue());
        add(titleLabel);
        add(subtitleGapFiller);
        add(subtitleLabel);
        add(sectionGapFiller);
        add(playButton);
        add(firstButtonGapFiller);
        add(settingsButton);
        add(secondButtonGapFiller);
        add(exitButton);
        add(Box.createVerticalGlue());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                applyResponsiveSizing(getSize());
            }
        });

        SwingUtilities.invokeLater(() -> applyResponsiveSizing(getSize()));
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;

        if (themeMode == ThemeMode.DARK) {
            titleLabel.setForeground(TITLE_COLOR);
            subtitleLabel.setForeground(SUBTITLE_COLOR);
            playButton.setForeground(BUTTON_TEXT_COLOR);
            settingsButton.setForeground(BUTTON_TEXT_COLOR);
            exitButton.setForeground(BUTTON_TEXT_COLOR);
            applyButtonPalette(playButton, BUTTON_COLOR);
            applyButtonPalette(settingsButton, BUTTON_COLOR);
            applyButtonPalette(exitButton, BUTTON_COLOR);
        } else {
            titleLabel.setForeground(new Color(22, 48, 100));
            subtitleLabel.setForeground(new Color(60, 98, 168));
            Color lightButtonText = new Color(20, 44, 92);
            playButton.setForeground(lightButtonText);
            settingsButton.setForeground(lightButtonText);
            exitButton.setForeground(lightButtonText);
            Color lightButtonColor = new Color(185, 208, 242);
            applyButtonPalette(playButton, lightButtonColor);
            applyButtonPalette(settingsButton, lightButtonColor);
            applyButtonPalette(exitButton, lightButtonColor);
        }

        repaint();
    }

    public JButton getPlayButton() {
        return playButton;
    }

    public JButton getSettingsButton() {
        return settingsButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }

    private void applyResponsiveSizing(Dimension baseSize) {
        Dimension effectiveSize = baseSize;
        if (effectiveSize.width <= 0 || effectiveSize.height <= 0) {
            effectiveSize = Toolkit.getDefaultToolkit().getScreenSize();
        }

        double widthScale = effectiveSize.width / BASE_SCREEN_WIDTH;
        double heightScale = effectiveSize.height / BASE_SCREEN_HEIGHT;
        double uiScale = clamp(Math.min(widthScale, heightScale), MIN_UI_SCALE, MAX_UI_SCALE);

        int buttonWidth = (int) Math.round(BASE_BUTTON_WIDTH * uiScale);
        int buttonHeight = (int) Math.round(BASE_BUTTON_HEIGHT * uiScale);
        int buttonFontSize = (int) Math.round(BASE_BUTTON_FONT_SIZE * uiScale);
        int titleFontSize = (int) Math.round(BASE_TITLE_FONT_SIZE * uiScale);
        int subtitleFontSize = (int) Math.round(BASE_SUBTITLE_FONT_SIZE * uiScale);
        int sectionGap = (int) Math.round(BASE_SECTION_GAP * uiScale);
        int subtitleGap = (int) Math.round(14 * uiScale);
        int buttonGap = (int) Math.round(BASE_BUTTON_GAP * uiScale);

        // Keep controls reasonable if window gets very narrow.
        buttonWidth = Math.min(buttonWidth, Math.max(280, effectiveSize.width - 120));

        Dimension buttonSize = new Dimension(buttonWidth, buttonHeight);
        Font buttonFont = new Font("Dialog", Font.BOLD, buttonFontSize);

        titleLabel.setFont(new Font("Dialog", Font.BOLD, titleFontSize));
        subtitleLabel.setFont(new Font("Dialog", Font.PLAIN, subtitleFontSize));
        applyButtonSizing(playButton, buttonSize, buttonFont);
        applyButtonSizing(settingsButton, buttonSize, buttonFont);
        applyButtonSizing(exitButton, buttonSize, buttonFont);

        updateFillerHeight(subtitleGapFiller, subtitleGap);
        updateFillerHeight(sectionGapFiller, sectionGap);
        updateFillerHeight(firstButtonGapFiller, buttonGap);
        updateFillerHeight(secondButtonGapFiller, buttonGap);

        revalidate();
        repaint();
    }

    private double clamp(double value, double minValue, double maxValue) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private void applyButtonSizing(JButton button, Dimension size, Font font) {
        button.setFont(font);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
    }

    private Filler createVerticalFiller(int height) {
        Dimension size = new Dimension(0, height);
        return new Filler(size, size, size);
    }

    private void updateFillerHeight(Filler filler, int height) {
        Dimension size = new Dimension(0, height);
        filler.changeShape(size, size, size);
    }

    private JButton createMenuButton(String label) {
        JButton button = new ModernMenuButton(label);
        button.setForeground(BUTTON_TEXT_COLOR);
        applyButtonPalette(button, BUTTON_COLOR);
        button.setAlignmentX(CENTER_ALIGNMENT);
        return button;
    }

    private void applyButtonPalette(JButton button, Color fillColor) {
        button.setBackground(fillColor);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        LinearGradientPaint gradient = new LinearGradientPaint(
                0f,
                0f,
                0f,
                getHeight(),
                new float[] {0f, 1f},
            themeMode == ThemeMode.DARK
                ? new Color[] {new Color(16, 24, 44), new Color(9, 13, 26)}
                : new Color[] {new Color(232, 241, 255), new Color(209, 224, 247)});

        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    private static final class ModernMenuButton extends JButton {
        private static final int ARC_SIZE = 32;
        private boolean hovered;
        private boolean focused;

        ModernMenuButton(String label) {
            super(label);
            setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent event) {
                    focused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            ButtonModel buttonModel = getModel();
            boolean pressed = buttonModel.isArmed() && buttonModel.isPressed();
            int yOffset = pressed ? 2 : 0;

            Graphics2D g2d = (Graphics2D) graphics.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color baseColor = getBackground();
            if (pressed) {
                baseColor = shiftColor(baseColor, -28);
            } else if (hovered) {
                baseColor = shiftColor(baseColor, 18);
            }

            if (!isEnabled()) {
                baseColor = new Color(78, 87, 103);
            }

            g2d.setColor(baseColor);
            g2d.fillRoundRect(0, yOffset, getWidth() - 1, getHeight() - 1 - yOffset, ARC_SIZE, ARC_SIZE);

            if (focused) {
                g2d.setColor(shiftColor(getForeground(), 36));
                g2d.drawRoundRect(1, 1 + yOffset, getWidth() - 3, getHeight() - 3 - yOffset, ARC_SIZE, ARC_SIZE);
            }

            g2d.dispose();

            Graphics2D textGraphics = (Graphics2D) graphics.create();
            if (pressed) {
                textGraphics.translate(0, 1);
            }
            super.paintComponent(textGraphics);
            textGraphics.dispose();
        }

        private static Color shiftColor(Color color, int shift) {
            int red = Math.max(0, Math.min(255, color.getRed() + shift));
            int green = Math.max(0, Math.min(255, color.getGreen() + shift));
            int blue = Math.max(0, Math.min(255, color.getBlue() + shift));
            return new Color(red, green, blue);
        }
    }
}
