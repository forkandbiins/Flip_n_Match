import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class DifficultyPanel extends JPanel {
    // Match main menu responsive size rules.
    private static final double BASE_SCREEN_WIDTH = 1920.0;
    private static final double BASE_SCREEN_HEIGHT = 1080.0;
    private static final double MIN_UI_SCALE = 0.85;
    private static final double MAX_UI_SCALE = 1.35;
    private static final int BASE_BUTTON_WIDTH = 460;
    private static final int BASE_BUTTON_HEIGHT = 84;
    private static final int BASE_BUTTON_FONT_SIZE = 30;
    private static final int BASE_LABEL_FONT_SIZE = 42;
    private static final int BASE_SECTION_GAP = 42;
    private static final int BASE_BUTTON_GAP = 22;

    private static final Color BUTTON_TEXT_COLOR = new Color(233, 240, 255);
    private static final Color BUTTON_COLOR = new Color(38, 64, 109);

    private final JButton backButton;
    private final JLabel difficultyLabel;
    private final JButton easyButton;
    private final JButton mediumButton;
    private final JButton hardButton;
    private final Filler labelGapFiller;
    private final Filler firstButtonGapFiller;
    private final Filler secondButtonGapFiller;
    private ThemeMode themeMode = ThemeMode.DARK;

    public DifficultyPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(24, 24, 0, 24));

        backButton = new ModernBackButton("Back");
        backButton.setFont(new Font("Dialog", Font.BOLD, 18));
        backButton.setForeground(BUTTON_TEXT_COLOR);
        backButton.setBackground(BUTTON_COLOR);
        backButton.setIcon(new ArrowLeftIcon(BUTTON_TEXT_COLOR));
        backButton.setHorizontalAlignment(SwingConstants.LEFT);
        backButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        backButton.setIconTextGap(10);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        topBar.add(backButton, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        difficultyLabel = new JLabel("SELECT A DIFFICULTY");
        difficultyLabel.setFont(new Font("Dialog", Font.BOLD, BASE_LABEL_FONT_SIZE));
        difficultyLabel.setForeground(BUTTON_TEXT_COLOR);
        difficultyLabel.setAlignmentX(CENTER_ALIGNMENT);

        easyButton = createDifficultyButton(Difficulty.EASY);
        mediumButton = createDifficultyButton(Difficulty.MEDIUM);
        hardButton = createDifficultyButton(Difficulty.HARD);
        labelGapFiller = createVerticalFiller(BASE_SECTION_GAP);
        firstButtonGapFiller = createVerticalFiller(BASE_BUTTON_GAP);
        secondButtonGapFiller = createVerticalFiller(BASE_BUTTON_GAP);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(difficultyLabel);
        centerPanel.add(labelGapFiller);
        centerPanel.add(easyButton);
        centerPanel.add(firstButtonGapFiller);
        centerPanel.add(mediumButton);
        centerPanel.add(secondButtonGapFiller);
        centerPanel.add(hardButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                applyDifficultyButtonSizing(getSize());
            }
        });

        applyDifficultyButtonSizing(Toolkit.getDefaultToolkit().getScreenSize());
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;

        if (themeMode == ThemeMode.DARK) {
            difficultyLabel.setForeground(BUTTON_TEXT_COLOR);
            backButton.setForeground(BUTTON_TEXT_COLOR);
            backButton.setBackground(BUTTON_COLOR);
            backButton.setIcon(new ArrowLeftIcon(BUTTON_TEXT_COLOR));
            easyButton.setForeground(BUTTON_TEXT_COLOR);
            mediumButton.setForeground(BUTTON_TEXT_COLOR);
            hardButton.setForeground(BUTTON_TEXT_COLOR);
            easyButton.setBackground(BUTTON_COLOR);
            mediumButton.setBackground(BUTTON_COLOR);
            hardButton.setBackground(BUTTON_COLOR);
        } else {
            Color lightText = new Color(20, 44, 92);
            Color lightButtonColor = new Color(185, 208, 242);
            difficultyLabel.setForeground(lightText);
            backButton.setForeground(lightText);
            backButton.setBackground(lightButtonColor);
            backButton.setIcon(new ArrowLeftIcon(lightText));
            easyButton.setForeground(lightText);
            mediumButton.setForeground(lightText);
            hardButton.setForeground(lightText);
            easyButton.setBackground(lightButtonColor);
            mediumButton.setBackground(lightButtonColor);
            hardButton.setBackground(lightButtonColor);
        }

        repaint();
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JButton getEasyButton() {
        return easyButton;
    }

    public JButton getMediumButton() {
        return mediumButton;
    }

    public JButton getHardButton() {
        return hardButton;
    }

    private JButton createDifficultyButton(Difficulty difficulty) {
        JButton button = new ModernDifficultyButton(difficulty.getButtonLabel());
        button.setFont(new Font("Dialog", Font.BOLD, BASE_BUTTON_FONT_SIZE));
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setBackground(BUTTON_COLOR);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(14, 36, 14, 36));
        button.setMaximumSize(new Dimension(BASE_BUTTON_WIDTH, BASE_BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(BASE_BUTTON_WIDTH, BASE_BUTTON_HEIGHT));
        return button;
    }

    private void applyDifficultyButtonSizing(Dimension baseSize) {
        Dimension effectiveSize = baseSize;
        if (effectiveSize.width <= 0 || effectiveSize.height <= 0) {
            effectiveSize = Toolkit.getDefaultToolkit().getScreenSize();
        }

        double widthScale = effectiveSize.width / BASE_SCREEN_WIDTH;
        double heightScale = effectiveSize.height / BASE_SCREEN_HEIGHT;
        double uiScale = Math.max(MIN_UI_SCALE, Math.min(MAX_UI_SCALE, Math.min(widthScale, heightScale)));

        int buttonWidth = (int) Math.round(BASE_BUTTON_WIDTH * uiScale);
        int buttonHeight = (int) Math.round(BASE_BUTTON_HEIGHT * uiScale);
        int buttonFontSize = (int) Math.round(BASE_BUTTON_FONT_SIZE * uiScale);
        int labelFontSize = (int) Math.round(BASE_LABEL_FONT_SIZE * uiScale);
        int sectionGap = (int) Math.round(BASE_SECTION_GAP * uiScale);
        int buttonGap = (int) Math.round(BASE_BUTTON_GAP * uiScale);
        buttonWidth = Math.min(buttonWidth, Math.max(280, effectiveSize.width - 120));

        Dimension buttonSize = new Dimension(buttonWidth, buttonHeight);
        Font buttonFont = new Font("Dialog", Font.BOLD, buttonFontSize);
        Font labelFont = new Font("Dialog", Font.BOLD, labelFontSize);

        difficultyLabel.setFont(labelFont);
        applySizing(easyButton, buttonSize, buttonFont);
        applySizing(mediumButton, buttonSize, buttonFont);
        applySizing(hardButton, buttonSize, buttonFont);
        updateFillerHeight(labelGapFiller, sectionGap);
        updateFillerHeight(firstButtonGapFiller, buttonGap);
        updateFillerHeight(secondButtonGapFiller, buttonGap);

        revalidate();
        repaint();
    }

    private Filler createVerticalFiller(int height) {
        Dimension size = new Dimension(0, height);
        return new Filler(size, size, size);
    }

    private void updateFillerHeight(Filler filler, int height) {
        Dimension size = new Dimension(0, height);
        filler.changeShape(size, size, size);
    }

    private void applySizing(JButton button, Dimension size, Font font) {
        button.setFont(font);
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
    }

    private static final class ModernBackButton extends JButton {
        private static final int ARC_SIZE = 28;
        private boolean hovered;
        private boolean focused;

        ModernBackButton(String label) {
            super(label);
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

    private static final class ModernDifficultyButton extends JButton {
        private static final int ARC_SIZE = 28;
        private boolean hovered;
        private boolean focused;

        ModernDifficultyButton(String label) {
            super(label);
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
                g2d.drawRoundRect(2, yOffset + 2, getWidth() - 5, getHeight() - 5 - yOffset, ARC_SIZE - 6, ARC_SIZE - 6);
            }

            g2d.dispose();

            Graphics2D textGraphics = (Graphics2D) graphics.create();
            textGraphics.translate(0, yOffset);
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

    private static final class ArrowLeftIcon implements Icon {
        private static final int ICON_WIDTH = 12;
        private static final int ICON_HEIGHT = 12;
        private final Color color;

        ArrowLeftIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2d = (Graphics2D) graphics.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawLine(x + 8, y + 2, x + 3, y + 6);
            g2d.drawLine(x + 3, y + 6, x + 8, y + 10);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return ICON_WIDTH;
        }

        @Override
        public int getIconHeight() {
            return ICON_HEIGHT;
        }
    }
}