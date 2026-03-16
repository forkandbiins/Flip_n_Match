import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class GamePanel extends JPanel {
    private static final Color HIDDEN_TILE_COLOR = new Color(38, 64, 109);
    private static final Color REVEALED_TILE_COLOR = new Color(226, 233, 246);
    private static final Color MINE_TILE_COLOR = new Color(186, 52, 52);
    private static final Color BOARD_SURFACE_COLOR = new Color(21, 32, 57);
    private static final Color TILE_BORDER_COLOR = new Color(66, 93, 143);
    private static final Color PANEL_TOP_COLOR = new Color(20, 33, 61);
    private static final Color PANEL_BOTTOM_COLOR = new Color(10, 18, 34);
    private static final Color TEXT_COLOR = new Color(233, 240, 255);
    private static final Color ZERO_TILE_TEXT_COLOR = new Color(110, 128, 160);
    private static final Color BUTTON_COLOR = new Color(38, 64, 109);
    private static final Color BUTTON_HOVER_COLOR = new Color(58, 89, 140);
    private static final Color BUTTON_PRESSED_COLOR = new Color(27, 48, 86);
    private static final Color BUTTON_FOCUS_BORDER_COLOR = new Color(129, 173, 255);
    private static final Icon BOMB_ICON = loadSvgIcon("bomb.svg", 18, 18, new Color(250, 252, 255));
    private static final Icon FLAG_ICON = loadSvgIcon("flag.svg", 18, 18, new Color(255, 98, 98));

    private final JButton backButton;
    private final SquareBoardHost boardHost;
    private final JPanel boardSurface;
    private JButton[][] tileButtons;
    private Font tileBaseFont;

    public GamePanel() {
        setLayout(new java.awt.BorderLayout(18, 18));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new java.awt.BorderLayout(12, 12));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        backButton = new ModernBackButton("Back");
        backButton.setFont(new Font("Dialog", Font.BOLD, 18));
        backButton.setForeground(TEXT_COLOR);
        backButton.setBackground(BUTTON_COLOR);
        backButton.setIcon(new ArrowLeftIcon(TEXT_COLOR));
        backButton.setHorizontalAlignment(SwingConstants.LEFT);
        backButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        backButton.setIconTextGap(10);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        topBar.add(backButton, java.awt.BorderLayout.WEST);

        boardHost = new SquareBoardHost();
        boardHost.setOpaque(false);

        boardSurface = new JPanel();
        boardSurface.setOpaque(true);
        boardSurface.setBackground(BOARD_SURFACE_COLOR);
        boardSurface.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 119, 171), 2),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        boardHost.setContent(boardSurface);

        add(topBar, java.awt.BorderLayout.NORTH);
        add(boardHost, java.awt.BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setPaint(new GradientPaint(0, 0, PANEL_TOP_COLOR, 0, getHeight(), PANEL_BOTTOM_COLOR));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    public JButton getBackButton() {
        return backButton;
    }

    public void setTitleText(String title) {
        // Intentionally no-op: top text has been removed.
    }

    public void setStatusText(String status) {
        // Intentionally no-op: top text has been removed.
    }

    public void buildBoard(int rowCount, int columnCount, ActionListener tileListener, MouseAdapter tileMouseListener) {
        boardSurface.removeAll();
        boardSurface.setLayout(new GridLayout(rowCount, columnCount, 6, 6));
        tileButtons = new JButton[rowCount][columnCount];

        int tileFontSize = rowCount >= 12 ? 14 : 16;
        tileBaseFont = new Font("Dialog", Font.BOLD, tileFontSize);

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                JButton tileButton = new JButton();
            tileButton.setFont(tileBaseFont);
                tileButton.setFocusable(false);
                tileButton.setMargin(new Insets(0, 0, 0, 0));
                tileButton.setPreferredSize(new Dimension(40, 40));
                tileButton.setOpaque(true);
                tileButton.setBorder(BorderFactory.createLineBorder(TILE_BORDER_COLOR, 1));
                tileButton.setContentAreaFilled(true);
                tileButton.setActionCommand(row + ":" + column);
                tileButton.addActionListener(tileListener);
                tileButton.addMouseListener(tileMouseListener);

                tileButtons[row][column] = tileButton;
                boardSurface.add(tileButton);
            }
        }

        revalidate();
        repaint();
    }

    public JButton getTileButton(int row, int column) {
        return tileButtons[row][column];
    }

    public int getBoardRowCount() {
        return tileButtons.length;
    }

    public int getBoardColumnCount() {
        return tileButtons.length == 0 ? 0 : tileButtons[0].length;
    }

    public void setTileHidden(int row, int column, boolean enabled) {
        JButton tileButton = tileButtons[row][column];
        tileButton.setText("");
        tileButton.setIcon(null);
        tileButton.setEnabled(enabled);
        tileButton.setFont(tileBaseFont);
        tileButton.setForeground(TEXT_COLOR);
        tileButton.setBackground(HIDDEN_TILE_COLOR);
        tileButton.setBorder(BorderFactory.createLineBorder(TILE_BORDER_COLOR, 1));
    }

    public void setTileFlagged(int row, int column) {
        JButton tileButton = tileButtons[row][column];
        tileButton.setText(FLAG_ICON == null ? "F" : "");
        tileButton.setIcon(FLAG_ICON);
        tileButton.setEnabled(true);
        tileButton.setFont(tileBaseFont);
        tileButton.setForeground(new Color(255, 236, 236));
        tileButton.setBackground(new Color(52, 77, 122));
        tileButton.setBorder(BorderFactory.createLineBorder(new Color(124, 151, 205), 1));
    }

    public void setTileRevealed(int row, int column, int adjacentMineCount) {
        JButton tileButton = tileButtons[row][column];
        Color numberColor = getNumberColor(adjacentMineCount);
        tileButton.setText(adjacentMineCount == 0 ? "" : Integer.toString(adjacentMineCount));
        tileButton.setIcon(null);
        tileButton.setEnabled(true);
        tileButton.setForeground(numberColor);
        tileButton.setBackground(REVEALED_TILE_COLOR);
        tileButton.setBorder(BorderFactory.createLineBorder(new Color(188, 201, 224), 1));

        int emphasizedSize = Math.max(tileBaseFont.getSize() + 2, 16);
        tileButton.setFont(tileBaseFont.deriveFont(Font.BOLD, emphasizedSize));
    }

    public void setTileMine(int row, int column, boolean detonated) {
        JButton tileButton = tileButtons[row][column];
        tileButton.setText(BOMB_ICON == null ? "*" : "");
        tileButton.setIcon(BOMB_ICON);
        tileButton.setEnabled(true);
        tileButton.setFont(tileBaseFont);
        tileButton.setForeground(Color.WHITE);
        tileButton.setBackground(detonated ? MINE_TILE_COLOR : new Color(142, 40, 40));
        tileButton.setBorder(BorderFactory.createLineBorder(new Color(115, 28, 28), 1));
    }

    public void requestBoardFocus() {
        if (tileButtons != null && tileButtons.length > 0 && tileButtons[0].length > 0) {
            tileButtons[0][0].requestFocusInWindow();
        }
    }

    private static final class SquareBoardHost extends JPanel {
        private JPanel content;

        void setContent(JPanel content) {
            this.content = content;
            removeAll();
            setLayout(null);
            add(content);
            revalidate();
            repaint();
        }

        @Override
        public void doLayout() {
            if (content == null) {
                return;
            }

            int side = Math.min(getWidth(), getHeight());
            int x = (getWidth() - side) / 2;
            int y = (getHeight() - side) / 2;
            content.setBounds(x, y, side, side);
        }
    }

    private Color getNumberColor(int adjacentMineCount) {
        return switch (adjacentMineCount) {
            case 1 -> new Color(39, 93, 189);
            case 2 -> new Color(32, 138, 84);
            case 3 -> new Color(200, 56, 46);
            case 4 -> new Color(104, 57, 171);
            case 5 -> new Color(158, 76, 44);
            case 6 -> new Color(26, 139, 148);
            case 7 -> new Color(35, 44, 59);
            case 8 -> new Color(88, 96, 110);
            default -> ZERO_TILE_TEXT_COLOR;
        };
    }

    private static Icon loadSvgIcon(String assetFileName, int iconWidth, int iconHeight, Color color) {
        Path svgPath = Path.of("assets", assetFileName);
        if (!Files.exists(svgPath)) {
            return null;
        }

        try {
            String svg = Files.readString(svgPath, StandardCharsets.UTF_8);
            Matcher pathMatcher = Pattern.compile("d=\"([^\"]+)\"").matcher(svg);
            Matcher viewBoxMatcher = Pattern.compile("viewBox=\"([^\"]+)\"").matcher(svg);
            if (!pathMatcher.find() || !viewBoxMatcher.find()) {
                return null;
            }

            String[] viewBoxValues = viewBoxMatcher.group(1).trim().split("\\s+");
            if (viewBoxValues.length != 4) {
                return null;
            }

            double minX = Double.parseDouble(viewBoxValues[0]);
            double minY = Double.parseDouble(viewBoxValues[1]);
            double viewBoxWidth = Double.parseDouble(viewBoxValues[2]);
            double viewBoxHeight = Double.parseDouble(viewBoxValues[3]);

            Shape shape = SvgPathParser.parse(pathMatcher.group(1));
            return new SvgPathIcon(shape, minX, minY, viewBoxWidth, viewBoxHeight, iconWidth, iconHeight, color);
        } catch (IOException | IllegalArgumentException exception) {
            return null;
        }
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

    private static final class SvgPathIcon implements Icon {
        private final Shape shape;
        private final double minX;
        private final double minY;
        private final double viewBoxWidth;
        private final double viewBoxHeight;
        private final int iconWidth;
        private final int iconHeight;
        private final Color color;

        SvgPathIcon(
                Shape shape,
                double minX,
                double minY,
                double viewBoxWidth,
                double viewBoxHeight,
                int iconWidth,
                int iconHeight,
                Color color) {
            this.shape = shape;
            this.minX = minX;
            this.minY = minY;
            this.viewBoxWidth = viewBoxWidth;
            this.viewBoxHeight = viewBoxHeight;
            this.iconWidth = iconWidth;
            this.iconHeight = iconHeight;
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2d = (Graphics2D) graphics.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);

            double scale = Math.min((double) iconWidth / viewBoxWidth, (double) iconHeight / viewBoxHeight);
            double scaledWidth = viewBoxWidth * scale;
            double scaledHeight = viewBoxHeight * scale;
            double translateX = x + ((iconWidth - scaledWidth) / 2.0) - (minX * scale);
            double translateY = y + ((iconHeight - scaledHeight) / 2.0) - (minY * scale);

            AffineTransform transform = new AffineTransform();
            transform.translate(translateX, translateY);
            transform.scale(scale, scale);
            g2d.fill(transform.createTransformedShape(shape));
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return iconWidth;
        }

        @Override
        public int getIconHeight() {
            return iconHeight;
        }
    }

    private static final class SvgPathParser {
        private final String pathData;
        private int index;
        private double currentX;
        private double currentY;
        private double lastControlX;
        private double lastControlY;
        private double subPathStartX;
        private double subPathStartY;
        private char previousCommand;

        private SvgPathParser(String pathData) {
            this.pathData = pathData;
        }

        static Shape parse(String pathData) {
            return new SvgPathParser(pathData).parsePath();
        }

        private Shape parsePath() {
            Path2D.Double path = new Path2D.Double();

            while (hasMore()) {
                skipWhitespaceAndCommas();
                if (!hasMore()) {
                    break;
                }

                char command = pathData.charAt(index);
                if (Character.isLetter(command)) {
                    index++;
                    previousCommand = command;
                } else {
                    command = previousCommand;
                }

                switch (command) {
                    case 'M', 'm' -> parseMove(path, command == 'm');
                    case 'L', 'l' -> parseLine(path, command == 'l');
                    case 'H', 'h' -> parseHorizontal(path, command == 'h');
                    case 'V', 'v' -> parseVertical(path, command == 'v');
                    case 'Q', 'q' -> parseQuadratic(path, command == 'q');
                    case 'T', 't' -> parseSmoothQuadratic(path, command == 't');
                    case 'Z', 'z' -> {
                        path.closePath();
                        currentX = subPathStartX;
                        currentY = subPathStartY;
                    }
                    default -> throw new IllegalArgumentException("Unsupported SVG path command: " + command);
                }
            }

            return path;
        }

        private void parseMove(Path2D.Double path, boolean relative) {
            double x = nextNumber();
            double y = nextNumber();
            if (relative) {
                x += currentX;
                y += currentY;
            }
            path.moveTo(x, y);
            currentX = x;
            currentY = y;
            subPathStartX = x;
            subPathStartY = y;

            while (hasNumberAhead()) {
                parseLine(path, relative);
            }
        }

        private void parseLine(Path2D.Double path, boolean relative) {
            while (hasNumberAhead()) {
                double x = nextNumber();
                double y = nextNumber();
                if (relative) {
                    x += currentX;
                    y += currentY;
                }
                path.lineTo(x, y);
                currentX = x;
                currentY = y;
            }
        }

        private void parseHorizontal(Path2D.Double path, boolean relative) {
            while (hasNumberAhead()) {
                double x = nextNumber();
                if (relative) {
                    x += currentX;
                }
                path.lineTo(x, currentY);
                currentX = x;
            }
        }

        private void parseVertical(Path2D.Double path, boolean relative) {
            while (hasNumberAhead()) {
                double y = nextNumber();
                if (relative) {
                    y += currentY;
                }
                path.lineTo(currentX, y);
                currentY = y;
            }
        }

        private void parseQuadratic(Path2D.Double path, boolean relative) {
            while (hasNumberAhead()) {
                double controlX = nextNumber();
                double controlY = nextNumber();
                double endX = nextNumber();
                double endY = nextNumber();

                if (relative) {
                    controlX += currentX;
                    controlY += currentY;
                    endX += currentX;
                    endY += currentY;
                }

                path.quadTo(controlX, controlY, endX, endY);
                lastControlX = controlX;
                lastControlY = controlY;
                currentX = endX;
                currentY = endY;
            }
        }

        private void parseSmoothQuadratic(Path2D.Double path, boolean relative) {
            while (hasNumberAhead()) {
                double reflectedControlX = currentX;
                double reflectedControlY = currentY;
                if (previousCommand == 'Q' || previousCommand == 'q' || previousCommand == 'T' || previousCommand == 't') {
                    reflectedControlX = (2 * currentX) - lastControlX;
                    reflectedControlY = (2 * currentY) - lastControlY;
                }

                double endX = nextNumber();
                double endY = nextNumber();
                if (relative) {
                    endX += currentX;
                    endY += currentY;
                }

                path.quadTo(reflectedControlX, reflectedControlY, endX, endY);
                lastControlX = reflectedControlX;
                lastControlY = reflectedControlY;
                currentX = endX;
                currentY = endY;
            }
        }

        private boolean hasMore() {
            return index < pathData.length();
        }

        private boolean hasNumberAhead() {
            skipWhitespaceAndCommas();
            if (!hasMore()) {
                return false;
            }

            char current = pathData.charAt(index);
            return current == '-' || current == '+' || current == '.' || Character.isDigit(current);
        }

        private void skipWhitespaceAndCommas() {
            while (hasMore()) {
                char current = pathData.charAt(index);
                if (!Character.isWhitespace(current) && current != ',') {
                    break;
                }
                index++;
            }
        }

        private double nextNumber() {
            skipWhitespaceAndCommas();
            int start = index;

            if (pathData.charAt(index) == '+' || pathData.charAt(index) == '-') {
                index++;
            }

            while (hasMore() && Character.isDigit(pathData.charAt(index))) {
                index++;
            }

            if (hasMore() && pathData.charAt(index) == '.') {
                index++;
                while (hasMore() && Character.isDigit(pathData.charAt(index))) {
                    index++;
                }
            }

            return Double.parseDouble(pathData.substring(start, index));
        }
    }
}