import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class SettingsPanel extends JPanel {
    private static final Color BUTTON_TEXT_COLOR = new Color(233, 240, 255);
    private static final Color BUTTON_COLOR = new Color(38, 64, 109);

    private final JButton backButton;
    private final JButton toggleThemeButton;
    private final JLabel modeLabel;
    private ThemeMode themeMode = ThemeMode.DARK;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(24, 24, 0, 24));

        backButton = new ModernSettingsButton("Back");
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
        centerPanel.setLayout(new javax.swing.BoxLayout(centerPanel, javax.swing.BoxLayout.Y_AXIS));

        modeLabel = new JLabel("Current mode: Dark", SwingConstants.CENTER);
        modeLabel.setFont(new Font("Dialog", Font.BOLD, 28));
        modeLabel.setForeground(BUTTON_TEXT_COLOR);
        modeLabel.setAlignmentX(CENTER_ALIGNMENT);

        toggleThemeButton = new ModernSettingsButton("Switch to Light Mode");
        toggleThemeButton.setFont(new Font("Dialog", Font.BOLD, 22));
        toggleThemeButton.setForeground(BUTTON_TEXT_COLOR);
        toggleThemeButton.setBackground(BUTTON_COLOR);
        toggleThemeButton.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        toggleThemeButton.setAlignmentX(CENTER_ALIGNMENT);

        centerPanel.add(javax.swing.Box.createVerticalGlue());
        centerPanel.add(modeLabel);
        centerPanel.add(javax.swing.Box.createVerticalStrut(24));
        centerPanel.add(toggleThemeButton);
        centerPanel.add(javax.swing.Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JButton getToggleThemeButton() {
        return toggleThemeButton;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;

        if (themeMode == ThemeMode.DARK) {
            modeLabel.setForeground(BUTTON_TEXT_COLOR);
            backButton.setForeground(BUTTON_TEXT_COLOR);
            backButton.setBackground(BUTTON_COLOR);
            backButton.setIcon(new ArrowLeftIcon(BUTTON_TEXT_COLOR));
            toggleThemeButton.setForeground(BUTTON_TEXT_COLOR);
            toggleThemeButton.setBackground(BUTTON_COLOR);
            modeLabel.setText("Current mode: Dark");
            toggleThemeButton.setText("Switch to Light Mode");
        } else {
            Color lightText = new Color(20, 44, 92);
            Color lightButtonColor = new Color(185, 208, 242);
            modeLabel.setForeground(lightText);
            backButton.setForeground(lightText);
            backButton.setBackground(lightButtonColor);
            backButton.setIcon(new ArrowLeftIcon(lightText));
            toggleThemeButton.setForeground(lightText);
            toggleThemeButton.setBackground(lightButtonColor);
            modeLabel.setText("Current mode: Light");
            toggleThemeButton.setText("Switch to Dark Mode");
        }

        repaint();
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

    private static final class ModernSettingsButton extends JButton {
        private static final int ARC_SIZE = 28;
        private boolean hovered;
        private boolean focused;

        ModernSettingsButton(String label) {
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

    private static final class ArrowLeftIcon implements Icon {
        private static final int ICON_WIDTH = 12;
        private static final int ICON_HEIGHT = 12;
        private final Color color;

        ArrowLeftIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return ICON_WIDTH;
        }

        @Override
        public int getIconHeight() {
            return ICON_HEIGHT;
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
    }
}
