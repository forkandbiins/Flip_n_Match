import java.awt.BasicStroke;
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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
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
    private static final Color HUD_PANEL_COLOR = new Color(28, 45, 78);
    private static final Color LIGHT_PANEL_TOP_COLOR = new Color(247, 251, 255);
    private static final Color LIGHT_PANEL_BOTTOM_COLOR = new Color(224, 237, 255);
    private static final Color LIGHT_PRIMARY_TEXT = new Color(21, 49, 98);
    private static final Color LIGHT_STATUS_TEXT = new Color(58, 96, 158);
    private static final Color LIGHT_BUTTON_COLOR = new Color(204, 224, 250);
    private static final Color LIGHT_BOARD_SURFACE_COLOR = new Color(197, 220, 248);
    private static final Color LIGHT_BOARD_FRAME_COLOR = new Color(114, 154, 208);
    private static final Color LIGHT_HUD_PANEL_COLOR = new Color(214, 231, 252);
    private static final Color LIGHT_HUD_FRAME_COLOR = new Color(125, 162, 214);
    private static final Color LIGHT_HIDDEN_TILE_COLOR = new Color(157, 193, 238);
    private static final Color LIGHT_HIDDEN_TILE_BORDER_COLOR = new Color(99, 143, 205);
    private static final Color LIGHT_FLAG_TILE_COLOR = new Color(186, 212, 246);
    private static final Color LIGHT_FLAG_TILE_BORDER_COLOR = new Color(112, 154, 211);
    private static final Color LIGHT_REVEALED_TILE_COLOR = new Color(243, 248, 255);
    private static final Color LIGHT_REVEALED_TILE_BORDER_COLOR = new Color(163, 191, 226);
    private static final Color LIGHT_MINE_TILE_COLOR = new Color(220, 112, 112);
    private static final Color LIGHT_MINE_TILE_SAFE_COLOR = new Color(196, 128, 128);
    private static final Color LIGHT_MINE_TILE_BORDER_COLOR = new Color(163, 98, 98);
    private static final String TILE_STATE_KEY = "tileState";
    private static final String TILE_ENABLED_KEY = "tileEnabled";
    private static final String TILE_ADJACENT_KEY = "tileAdjacent";
    private static final String TILE_DETONATED_KEY = "tileDetonated";
    private static final String TILE_CARD_LETTER_KEY = "tileCardLetter";
    private static final Color BOMB_DARK_COLOR = new Color(255, 255, 255);
    private static final Color BOMB_LIGHT_COLOR = new Color(14, 56, 125);
    private static final Color FLAG_COLOR = new Color(255, 0, 0);

    private final JButton backButton;
    private final JButton restartButton;
    private final JLabel titleLabel;
    private final JLabel statusLabel;
    private final JLabel flagCountLabel;
    private final JLabel timerLabel;
    private final JPanel hudPanel;
    private final SquareBoardHost boardHost;
    private final JPanel boardSurface;
    private final Map<Integer, Icon> bombDarkIconCache = new HashMap<>();
    private final Map<Integer, Icon> bombLightIconCache = new HashMap<>();
    private final Map<Integer, Icon> flagIconCache = new HashMap<>();
    private JButton[][] tileButtons;
    private Font tileBaseFont;
    private ThemeMode themeMode = ThemeMode.DARK;

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

        restartButton = new ModernBackButton("Restart");
        restartButton.setFont(new Font("Dialog", Font.BOLD, 16));
        restartButton.setForeground(TEXT_COLOR);
        restartButton.setBackground(BUTTON_COLOR);
        restartButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel centerInfoPanel = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        centerInfoPanel.setOpaque(false);

        titleLabel = new JLabel("Minesweeper", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_COLOR);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 15));
        statusLabel.setForeground(new Color(198, 214, 245));

        centerInfoPanel.add(titleLabel);
        centerInfoPanel.add(statusLabel);

        hudPanel = new JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        hudPanel.setOpaque(true);
        hudPanel.setBackground(HUD_PANEL_COLOR);
        hudPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(84, 114, 166), 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        flagCountLabel = new JLabel("Flags left: 10", SwingConstants.RIGHT);
        flagCountLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        flagCountLabel.setForeground(new Color(244, 245, 255));

        timerLabel = new JLabel("Time: 00:00", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        timerLabel.setForeground(new Color(244, 245, 255));

        hudPanel.add(flagCountLabel);
        hudPanel.add(timerLabel);

        JPanel rightControlsPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        rightControlsPanel.setOpaque(false);
        rightControlsPanel.add(restartButton);
        rightControlsPanel.add(hudPanel);

        topBar.add(backButton, java.awt.BorderLayout.WEST);
        topBar.add(centerInfoPanel, java.awt.BorderLayout.CENTER);
        topBar.add(rightControlsPanel, java.awt.BorderLayout.EAST);

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
        Color topColor = themeMode == ThemeMode.DARK ? PANEL_TOP_COLOR : LIGHT_PANEL_TOP_COLOR;
        Color bottomColor = themeMode == ThemeMode.DARK ? PANEL_BOTTOM_COLOR : LIGHT_PANEL_BOTTOM_COLOR;
        g2d.setPaint(new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JButton getRestartButton() {
        return restartButton;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;

        if (themeMode == ThemeMode.DARK) {
            titleLabel.setForeground(TEXT_COLOR);
            statusLabel.setForeground(new Color(198, 214, 245));
            Color hudTextColor = new Color(244, 245, 255);
            flagCountLabel.setForeground(hudTextColor);
            timerLabel.setForeground(hudTextColor);
            backButton.setForeground(TEXT_COLOR);
            backButton.setBackground(BUTTON_COLOR);
            backButton.setIcon(new ArrowLeftIcon(TEXT_COLOR));
            restartButton.setForeground(TEXT_COLOR);
            restartButton.setBackground(BUTTON_COLOR);
        } else {
            titleLabel.setForeground(LIGHT_PRIMARY_TEXT);
            statusLabel.setForeground(LIGHT_STATUS_TEXT);
            flagCountLabel.setForeground(LIGHT_PRIMARY_TEXT);
            timerLabel.setForeground(LIGHT_PRIMARY_TEXT);
            backButton.setForeground(LIGHT_PRIMARY_TEXT);
            backButton.setBackground(LIGHT_BUTTON_COLOR);
            backButton.setIcon(new ArrowLeftIcon(LIGHT_PRIMARY_TEXT));
            restartButton.setForeground(LIGHT_PRIMARY_TEXT);
            restartButton.setBackground(LIGHT_BUTTON_COLOR);
        }

        boardSurface.setBackground(getBoardSurfaceColor());
        boardSurface.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBoardFrameColor(), 2),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        hudPanel.setBackground(getHudBackgroundColor());
        hudPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getHudFrameColor(), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        reapplyTileTheme();

        repaint();
    }

    public void setTitleText(String title) {
        titleLabel.setText(title);
    }

    public void setStatusText(String status) {
        statusLabel.setText(status);
    }

    public void setRemainingFlagCount(int remainingFlagCount) {
        int clampedCount = Math.max(0, remainingFlagCount);
        flagCountLabel.setText(String.format("Flags left: %02d", clampedCount));
    }

    public void setElapsedTimeSeconds(int elapsedSeconds) {
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    public void buildBoard(int rowCount, int columnCount, ActionListener tileListener, MouseAdapter tileMouseListener) {
        boardSurface.removeAll();
        boardSurface.setLayout(new GridLayout(rowCount, columnCount, 6, 6));
        tileButtons = new JButton[rowCount][columnCount];

        int tileFontSize = rowCount >= 12 ? 16 : 20;
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
        styleTileHidden(tileButton, enabled);
        tileButton.putClientProperty(TILE_STATE_KEY, TileVisualState.HIDDEN);
        tileButton.putClientProperty(TILE_ENABLED_KEY, enabled);
    }

    public void setTileFlagged(int row, int column) {
        JButton tileButton = tileButtons[row][column];
        styleTileFlagged(tileButton);
        tileButton.putClientProperty(TILE_STATE_KEY, TileVisualState.FLAGGED);
    }

    public void setTileRevealed(int row, int column, int adjacentMineCount) {
        JButton tileButton = tileButtons[row][column];
        styleTileRevealed(tileButton, adjacentMineCount);
        tileButton.putClientProperty(TILE_STATE_KEY, TileVisualState.REVEALED);
        tileButton.putClientProperty(TILE_ADJACENT_KEY, adjacentMineCount);
    }

    public void setTileMine(int row, int column, boolean detonated) {
        JButton tileButton = tileButtons[row][column];
        styleTileMine(tileButton, detonated);
        tileButton.putClientProperty(TILE_STATE_KEY, TileVisualState.MINE);
        tileButton.putClientProperty(TILE_DETONATED_KEY, detonated);
    }

    public void setTileCardHidden(int row, int column, boolean enabled) {
        JButton tileButton = tileButtons[row][column];
        styleTileCardHidden(tileButton, enabled);
        tileButton.putClientProperty(TILE_STATE_KEY, TileVisualState.CARD_HIDDEN);
        tileButton.putClientProperty(TILE_ENABLED_KEY, enabled);
    }

    public void setTileCardFace(int row, int column, char letter, boolean matched) {
        JButton tileButton = tileButtons[row][column];
        styleTileCardFace(tileButton, letter, matched);
        tileButton.putClientProperty(TILE_STATE_KEY, matched ? TileVisualState.CARD_MATCHED : TileVisualState.CARD_REVEALED);
        tileButton.putClientProperty(TILE_CARD_LETTER_KEY, letter);
    }

    public void setTileCardDiscovered(int row, int column) {
        JButton tileButton = tileButtons[row][column];
        styleTileCardDiscovered(tileButton);
        tileButton.putClientProperty(TILE_STATE_KEY, TileVisualState.CARD_DISCOVERED);
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
        if (themeMode == ThemeMode.DARK) {
            return switch (adjacentMineCount) {
                case 1 -> new Color(66, 128, 235);
                case 2 -> new Color(34, 186, 106);
                case 3 -> new Color(233, 78, 62);
                case 4 -> new Color(145, 86, 236);
                case 5 -> new Color(212, 116, 62);
                case 6 -> new Color(32, 185, 202);
                case 7 -> new Color(60, 72, 95);
                case 8 -> new Color(116, 128, 148);
                default -> new Color(144, 162, 196);
            };
        }

        return switch (adjacentMineCount) {
            case 1 -> new Color(35, 102, 212);
            case 2 -> new Color(22, 156, 88);
            case 3 -> new Color(215, 64, 55);
            case 4 -> new Color(112, 70, 191);
            case 5 -> new Color(176, 92, 53);
            case 6 -> new Color(21, 156, 170);
            case 7 -> new Color(56, 72, 102);
            case 8 -> new Color(88, 106, 134);
            default -> new Color(96, 118, 150);
        };
    }

    private void reapplyTileTheme() {
        if (tileButtons == null) {
            return;
        }

        for (JButton[] tileRow : tileButtons) {
            for (JButton tileButton : tileRow) {
                Object state = tileButton.getClientProperty(TILE_STATE_KEY);
                if (!(state instanceof TileVisualState tileVisualState)) {
                    continue;
                }

                switch (tileVisualState) {
                    case HIDDEN -> {
                        boolean enabled = Boolean.TRUE.equals(tileButton.getClientProperty(TILE_ENABLED_KEY));
                        styleTileHidden(tileButton, enabled);
                    }
                    case FLAGGED -> styleTileFlagged(tileButton);
                    case REVEALED -> {
                        Object adjacentValue = tileButton.getClientProperty(TILE_ADJACENT_KEY);
                        int adjacentMineCount = adjacentValue instanceof Integer ? (Integer) adjacentValue : 0;
                        styleTileRevealed(tileButton, adjacentMineCount);
                    }
                    case MINE -> {
                        boolean detonated = Boolean.TRUE.equals(tileButton.getClientProperty(TILE_DETONATED_KEY));
                        styleTileMine(tileButton, detonated);
                    }
                    case CARD_HIDDEN -> {
                        boolean enabled = Boolean.TRUE.equals(tileButton.getClientProperty(TILE_ENABLED_KEY));
                        styleTileCardHidden(tileButton, enabled);
                    }
                    case CARD_DISCOVERED -> styleTileCardDiscovered(tileButton);
                    case CARD_REVEALED, CARD_MATCHED -> {
                        Object letterValue = tileButton.getClientProperty(TILE_CARD_LETTER_KEY);
                        char letter = letterValue instanceof Character ? (Character) letterValue : '?';
                        styleTileCardFace(tileButton, letter, tileVisualState == TileVisualState.CARD_MATCHED);
                    }
                }
            }
        }
    }

    private void styleTileHidden(JButton tileButton, boolean enabled) {
        tileButton.setText("");
        tileButton.setIcon(null);
        tileButton.setEnabled(enabled);
        tileButton.setFont(getTileFontForButton(tileButton, 0));
        tileButton.setForeground(themeMode == ThemeMode.DARK ? TEXT_COLOR : LIGHT_PRIMARY_TEXT);
        tileButton.setBackground(themeMode == ThemeMode.DARK ? HIDDEN_TILE_COLOR : LIGHT_HIDDEN_TILE_COLOR);
        tileButton.setBorder(BorderFactory.createLineBorder(themeMode == ThemeMode.DARK ? TILE_BORDER_COLOR : LIGHT_HIDDEN_TILE_BORDER_COLOR, 1));
    }

    private void styleTileFlagged(JButton tileButton) {
        Icon flagIcon = getFlagIconForTheme(tileButton);
        tileButton.setText(flagIcon == null ? "F" : "");
        tileButton.setIcon(flagIcon);
        tileButton.setEnabled(true);
        tileButton.setFont(getTileFontForButton(tileButton, 0));
        tileButton.setForeground(themeMode == ThemeMode.DARK ? new Color(255, 236, 236) : new Color(170, 34, 56));
        tileButton.setBackground(themeMode == ThemeMode.DARK ? new Color(52, 77, 122) : LIGHT_FLAG_TILE_COLOR);
        tileButton.setBorder(BorderFactory.createLineBorder(themeMode == ThemeMode.DARK ? new Color(124, 151, 205) : LIGHT_FLAG_TILE_BORDER_COLOR, 1));
    }

    private void styleTileRevealed(JButton tileButton, int adjacentMineCount) {
        Color numberColor = getNumberColor(adjacentMineCount);
        tileButton.setText(adjacentMineCount == 0 ? "" : Integer.toString(adjacentMineCount));
        tileButton.setIcon(null);
        tileButton.setEnabled(true);
        tileButton.setForeground(numberColor);
        tileButton.setBackground(themeMode == ThemeMode.DARK ? REVEALED_TILE_COLOR : LIGHT_REVEALED_TILE_COLOR);
        tileButton.setBorder(BorderFactory.createLineBorder(themeMode == ThemeMode.DARK ? new Color(188, 201, 224) : LIGHT_REVEALED_TILE_BORDER_COLOR, 1));
        tileButton.setFont(getTileFontForButton(tileButton, 2));
    }

    private void styleTileMine(JButton tileButton, boolean detonated) {
        Icon bombIcon = getBombIconForTheme(tileButton);
        tileButton.setText(bombIcon == null ? "*" : "");
        tileButton.setIcon(bombIcon);
        tileButton.setEnabled(true);
        tileButton.setFont(getTileFontForButton(tileButton, 0));
        tileButton.setForeground(Color.WHITE);

        if (themeMode == ThemeMode.DARK) {
            tileButton.setBackground(detonated ? MINE_TILE_COLOR : new Color(142, 40, 40));
            tileButton.setBorder(BorderFactory.createLineBorder(new Color(115, 28, 28), 1));
        } else {
            tileButton.setBackground(detonated ? LIGHT_MINE_TILE_COLOR : LIGHT_MINE_TILE_SAFE_COLOR);
            tileButton.setBorder(BorderFactory.createLineBorder(LIGHT_MINE_TILE_BORDER_COLOR, 1));
        }
    }

    private void styleTileCardHidden(JButton tileButton, boolean enabled) {
        styleTileHidden(tileButton, enabled);
    }

    private void styleTileCardDiscovered(JButton tileButton) {
        tileButton.setText("X");
        tileButton.setIcon(null);
        tileButton.setEnabled(true);
        tileButton.setFont(getTileFontForButton(tileButton, 2));

        if (themeMode == ThemeMode.DARK) {
            tileButton.setForeground(new Color(120, 140, 176));
            tileButton.setBackground(REVEALED_TILE_COLOR);
            tileButton.setBorder(BorderFactory.createLineBorder(new Color(188, 201, 224), 1));
        } else {
            tileButton.setForeground(new Color(89, 117, 161));
            tileButton.setBackground(LIGHT_REVEALED_TILE_COLOR);
            tileButton.setBorder(BorderFactory.createLineBorder(LIGHT_REVEALED_TILE_BORDER_COLOR, 1));
        }
    }

    private void styleTileCardFace(JButton tileButton, char letter, boolean matched) {
        tileButton.setText(String.valueOf(letter));
        tileButton.setIcon(null);
        tileButton.setEnabled(true);
        tileButton.setFont(getTileFontForButton(tileButton, 3));

        if (themeMode == ThemeMode.DARK) {
            tileButton.setForeground(new Color(224, 255, 224));
            tileButton.setBackground(matched ? new Color(186, 154, 52) : new Color(58, 116, 73));
            tileButton.setBorder(BorderFactory.createLineBorder(matched ? new Color(235, 211, 122) : new Color(110, 182, 126), 1));
        } else {
            tileButton.setForeground(new Color(22, 88, 45));
            tileButton.setBackground(matched ? new Color(255, 236, 163) : new Color(166, 224, 180));
            tileButton.setBorder(BorderFactory.createLineBorder(matched ? new Color(212, 185, 92) : new Color(91, 170, 111), 1));
        }
    }

    private Color getBoardSurfaceColor() {
        return themeMode == ThemeMode.DARK ? BOARD_SURFACE_COLOR : LIGHT_BOARD_SURFACE_COLOR;
    }

    private Color getBoardFrameColor() {
        return themeMode == ThemeMode.DARK ? new Color(90, 119, 171) : LIGHT_BOARD_FRAME_COLOR;
    }

    private Color getHudBackgroundColor() {
        return themeMode == ThemeMode.DARK ? HUD_PANEL_COLOR : LIGHT_HUD_PANEL_COLOR;
    }

    private Color getHudFrameColor() {
        return themeMode == ThemeMode.DARK ? new Color(84, 114, 166) : LIGHT_HUD_FRAME_COLOR;
    }

    private Icon getFlagIconForTheme(JButton tileButton) {
        int size = resolveIconSize(tileButton, 0.72, 16, 30);
        return flagIconCache.computeIfAbsent(size, key -> loadSvgIcon("triangle-flag.svg", key, key, FLAG_COLOR));
    }

    private Icon getBombIconForTheme(JButton tileButton) {
        int size = resolveIconSize(tileButton, 0.62, 14, 26);
        if (themeMode == ThemeMode.DARK) {
            return bombDarkIconCache.computeIfAbsent(size, key -> loadBombIcon(BOMB_DARK_COLOR, key));
        }
        return bombLightIconCache.computeIfAbsent(size, key -> loadBombIcon(BOMB_LIGHT_COLOR, key));
    }

    private int resolveIconSize(JButton tileButton, double scaleFactor, int minSize, int maxSize) {
        int side = Math.min(tileButton.getWidth(), tileButton.getHeight());
        if (side <= 0) {
            side = tileButton.getPreferredSize().width;
        }

        int scaled = (int) Math.round(side * scaleFactor);
        return Math.max(minSize, Math.min(maxSize, scaled));
    }

    private Font getTileFontForButton(JButton tileButton, int boost) {
        int side = Math.min(tileButton.getWidth(), tileButton.getHeight());
        int computedBaseSize;

        if (side > 0) {
            computedBaseSize = (int) Math.round(side * 0.46);
            computedBaseSize = Math.max(14, Math.min(24, computedBaseSize));
        } else {
            computedBaseSize = tileBaseFont == null ? 16 : tileBaseFont.getSize();
        }

        int finalSize = Math.max(14, computedBaseSize + boost);
        return tileBaseFont.deriveFont(Font.BOLD, (float) finalSize);
    }

    private static Icon loadBombIcon(Color color, int iconSize) {
        Icon svgIcon = loadSvgIcon("bomba.svg", iconSize, iconSize, color);
        return svgIcon != null ? svgIcon : new BombFallbackIcon(iconSize, iconSize, color);
    }

    private static Icon loadSvgIcon(String assetFileName, int iconWidth, int iconHeight, Color color) {
        try {
            String svg = readAssetText(assetFileName);
            if (svg == null) {
                return null;
            }

            Matcher pathMatcher = Pattern.compile("<path[^>]*\\bd\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(svg);
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

    private static String readAssetText(String assetFileName) throws IOException {
        try (InputStream stream = openAssetStream(assetFileName)) {
            if (stream == null) {
                return null;
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static InputStream openAssetStream(String assetFileName) throws IOException {
        Path relativeAssetPath = Path.of("assets", assetFileName);
        if (Files.exists(relativeAssetPath)) {
            return Files.newInputStream(relativeAssetPath);
        }

        Path userDirPath = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path fromUserDir = findAssetFromBase(userDirPath, assetFileName);
        if (fromUserDir != null) {
            return Files.newInputStream(fromUserDir);
        }

        URL codeSourceUrl = GamePanel.class.getProtectionDomain().getCodeSource() == null
                ? null
                : GamePanel.class.getProtectionDomain().getCodeSource().getLocation();
        if (codeSourceUrl != null) {
            try {
                Path codeSourcePath = Path.of(codeSourceUrl.toURI()).toAbsolutePath().normalize();
                Path classBasePath = Files.isDirectory(codeSourcePath) ? codeSourcePath : codeSourcePath.getParent();
                Path fromClassBase = findAssetFromBase(classBasePath, assetFileName);
                if (fromClassBase != null) {
                    return Files.newInputStream(fromClassBase);
                }
            } catch (URISyntaxException ignored) {
                // Fall through to classpath lookup below.
            }
        }

        ClassLoader classLoader = GamePanel.class.getClassLoader();
        InputStream classpathAsset = classLoader.getResourceAsStream("assets/" + assetFileName);
        if (classpathAsset != null) {
            return classpathAsset;
        }

        return classLoader.getResourceAsStream(assetFileName);
    }

    private static Path findAssetFromBase(Path basePath, String assetFileName) {
        Path currentPath = basePath;
        while (currentPath != null) {
            Path candidate = currentPath.resolve("assets").resolve(assetFileName);
            if (Files.exists(candidate)) {
                return candidate;
            }
            currentPath = currentPath.getParent();
        }
        return null;
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

    private enum TileVisualState {
        HIDDEN,
        FLAGGED,
        REVEALED,
        MINE,
        CARD_HIDDEN,
        CARD_DISCOVERED,
        CARD_REVEALED,
        CARD_MATCHED
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

    private static final class BombFallbackIcon implements Icon {
        private final int iconWidth;
        private final int iconHeight;
        private final Color color;

        BombFallbackIcon(int iconWidth, int iconHeight, Color color) {
            this.iconWidth = iconWidth;
            this.iconHeight = iconHeight;
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2d = (Graphics2D) graphics.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);

            int bodySize = Math.min(iconWidth, iconHeight) - 6;
            int bodyX = x + 2;
            int bodyY = y + 4;
            g2d.fillOval(bodyX, bodyY, bodySize, bodySize);

            g2d.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(bodyX + bodySize - 2, bodyY + 2, x + iconWidth - 2, y + 2);
            g2d.fillOval(x + iconWidth - 4, y, 3, 3);

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
                    case 'C', 'c' -> parseCubic(path, command == 'c');
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

        private void parseCubic(Path2D.Double path, boolean relative) {
            while (hasNumberAhead()) {
                double control1X = nextNumber();
                double control1Y = nextNumber();
                double control2X = nextNumber();
                double control2Y = nextNumber();
                double endX = nextNumber();
                double endY = nextNumber();

                if (relative) {
                    control1X += currentX;
                    control1Y += currentY;
                    control2X += currentX;
                    control2Y += currentY;
                    endX += currentX;
                    endY += currentY;
                }

                path.curveTo(control1X, control1Y, control2X, control2Y, endX, endY);
                lastControlX = control2X;
                lastControlY = control2Y;
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