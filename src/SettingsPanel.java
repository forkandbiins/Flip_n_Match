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
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class SettingsPanel extends JPanel {
    private static final Color BUTTON_TEXT_COLOR = new Color(233, 240, 255);
    private static final Color BUTTON_COLOR = new Color(38, 64, 109);
    private static final Color BUTTON_HOVER_COLOR = new Color(58, 89, 140);
    private static final Color BUTTON_PRESSED_COLOR = new Color(27, 48, 86);
    private static final Color BUTTON_FOCUS_BORDER_COLOR = new Color(129, 173, 255);

    private final JButton backButton;

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
    }

    public JButton getBackButton() {
        return backButton;
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
                new Color[] {new Color(16, 24, 44), new Color(9, 13, 26)});

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

            Color baseColor = BUTTON_COLOR;
            if (pressed) {
                baseColor = BUTTON_PRESSED_COLOR;
            } else if (hovered) {
                baseColor = BUTTON_HOVER_COLOR;
            }

            g2d.setColor(baseColor);
            g2d.fillRoundRect(0, yOffset, getWidth() - 1, getHeight() - 1 - yOffset, ARC_SIZE, ARC_SIZE);

            if (focused) {
                g2d.setColor(BUTTON_FOCUS_BORDER_COLOR);
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
