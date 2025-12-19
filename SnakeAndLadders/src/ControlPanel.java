import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class ControlPanel extends JPanel {

    private final MainGameGUI host;

    JLabel lblCurrentName, lblNextName, lblStatus, lblLastWinnerName, lblLastWinnerScore;
    JTextArea txtLog;

    JButton btnStart, btnRoll, btnMuteSFX, btnRestart;
    JSlider volumeSlider;

    JPanel dicePanel;
    int lastDiceVal = 1;
    boolean lastDiceGreen = true;

    JPanel leaderboardList;

    public ControlPanel(MainGameGUI host) {
        this.host = host;

        setPreferredSize(new Dimension(360, 900));
        setBorder(new EmptyBorder(18, 16, 18, 16));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        add(makeTitle("PRECINCT"));
        add(Box.createVerticalStrut(12));

        JPanel pnlChamp = makeSection("LAST CHAMPION");
        lblLastWinnerName = makeLabel("None", 16, MainGameGUI.TEXT_WHITE, SwingConstants.CENTER);
        lblLastWinnerScore = makeLabel("Score: 0", 14, MainGameGUI.UI_TEXT_GOLD, SwingConstants.CENTER);
        pnlChamp.add(lblLastWinnerName);
        pnlChamp.add(Box.createVerticalStrut(6));
        pnlChamp.add(lblLastWinnerScore);
        add(pnlChamp);
        add(Box.createVerticalStrut(12));

        JPanel pnlAudio = makeSection("AUDIO COMMS");
        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setOpaque(false);
        volumeSlider.addChangeListener(e -> host.onVolumeChanged(volumeSlider.getValue()));

        btnMuteSFX = makeButton("MUTE SFX", MainGameGUI.BTN_PRIMARY_BG, MainGameGUI.TEXT_WHITE, 14);
        btnMuteSFX.addActionListener(e -> host.toggleSFXMuteFromUI());

        pnlAudio.add(volumeSlider);
        pnlAudio.add(Box.createVerticalStrut(10));
        pnlAudio.add(btnMuteSFX);
        add(pnlAudio);
        add(Box.createVerticalStrut(12));

        JPanel pnlLead = makeSection("LEADERBOARD");
        leaderboardList = new JPanel();
        leaderboardList.setLayout(new BoxLayout(leaderboardList, BoxLayout.Y_AXIS));
        leaderboardList.setOpaque(false);

        JScrollPane leaderboardScroll = new JScrollPane(leaderboardList);
        leaderboardScroll.setBorder(null);
        leaderboardScroll.setOpaque(false);
        leaderboardScroll.getViewport().setOpaque(false);
        leaderboardScroll.setPreferredSize(new Dimension(320, 160));

        pnlLead.add(leaderboardScroll);
        add(pnlLead);
        add(Box.createVerticalStrut(12));

        JPanel pnlStatus = makeSection("PATROL STATUS");

        btnStart = makeButton("START PATROL", MainGameGUI.BTN_ACCENT_BG, MainGameGUI.TEXT_WHITE, 13);
        btnStart.addActionListener(e -> host.startGame());

        btnRestart = makeButton("RESTART", MainGameGUI.BTN_DANGER_BG, MainGameGUI.TEXT_WHITE, 13);
        btnRestart.addActionListener(e -> host.restartGame());

        JPanel rowButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        rowButtons.setOpaque(false);
        rowButtons.add(btnStart);
        rowButtons.add(btnRestart);

        lblNextName = makeChip("NEXT: -");

        dicePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int W = getWidth();
                int H = getHeight();

                g2.setComposite(AlphaComposite.Src);
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRect(0, 0, W, H);
                g2.setComposite(AlphaComposite.SrcOver);

                Color bg = lastDiceGreen ? new Color(90, 230, 160) : new Color(255, 120, 120);

                int shadowX = 4, shadowY = 6;
                int pad = 6;
                int arc = 18;

                int rx = pad;
                int ry = pad;
                int rw = W - pad * 2 - shadowX;
                int rh = H - pad * 2 - shadowY;

                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRoundRect(rx + shadowX, ry + shadowY, rw, rh, arc, arc);

                g2.setColor(bg);
                g2.fillRoundRect(rx, ry, rw, rh, arc, arc);

                g2.setColor(new Color(0, 0, 0, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(rx, ry, rw, rh, arc, arc);

                g2.setColor(new Color(255, 255, 255, 70));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(rx + 1, ry + 1, rw - 2, rh - 2, arc - 2, arc - 2);

                g2.setFont(new Font("Impact", Font.PLAIN, 44));
                g2.setColor(Color.WHITE);
                String s = String.valueOf(lastDiceVal);
                FontMetrics fm = g2.getFontMetrics();
                int tx = rx + (rw - fm.stringWidth(s)) / 2;
                int ty = ry + (rh - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(s, tx, ty);

                g2.dispose();
            }
        };
        dicePanel.setOpaque(false);
        dicePanel.setBackground(new Color(0,0,0,0));
        dicePanel.setPreferredSize(new Dimension(92, 92));
        dicePanel.setMaximumSize(new Dimension(92, 92));
        dicePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlStatus.add(rowButtons);
        pnlStatus.add(Box.createVerticalStrut(10));
        pnlStatus.add(lblNextName);
        pnlStatus.add(Box.createVerticalStrut(12));
        pnlStatus.add(dicePanel);
        add(pnlStatus);
        add(Box.createVerticalStrut(12));

        JPanel pnlAction = makeSection("ACTION");
        lblCurrentName = makeLabel("-", 20, MainGameGUI.NEON_CYAN, SwingConstants.CENTER);

        btnRoll = makeButton("ROLL DICE", MainGameGUI.BTN_PRIMARY_BG, MainGameGUI.TEXT_WHITE, 14);
        btnRoll.setEnabled(false);
        btnRoll.addActionListener(e -> host.processTurn());

        lblStatus = makeLabel("PRESS START", 14, MainGameGUI.TEXT_MUTED, SwingConstants.CENTER);

        pnlAction.add(lblCurrentName);
        pnlAction.add(Box.createVerticalStrut(10));
        pnlAction.add(btnRoll);
        pnlAction.add(Box.createVerticalStrut(10));
        pnlAction.add(lblStatus);

        add(pnlAction);
        add(Box.createVerticalStrut(12));

        txtLog = new JTextArea(8, 20);
        txtLog.setBackground(new Color(6, 8, 12));
        txtLog.setForeground(new Color(140, 255, 180));
        txtLog.setEditable(false);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);

        JScrollPane logScroll = new JScrollPane(txtLog);
        logScroll.setBorder(new LineBorder(new Color(255, 255, 255, 70), 2, true));
        logScroll.setPreferredSize(new Dimension(320, 220));
        add(logScroll);

        loadLastWinner(host.getSaveFileName());
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(10, 28, 70),
                0, getHeight(), new Color(10, 18, 40));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
    }

    private JLabel makeTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font("Impact", Font.PLAIN, 44));
        title.setForeground(MainGameGUI.UI_TEXT_GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        return title;
    }

    private JPanel makeSection(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(90, 240, 255, 200), 2, true),
                title,
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13),
                MainGameGUI.NEON_CYAN
        ));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(340, 9999));
        p.add(Box.createVerticalStrut(8));
        return p;
    }

    private JLabel makeLabel(String text, int size, Color color, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel makeChip(String text) {
        JLabel chip = new JLabel(text, SwingConstants.CENTER);
        chip.setOpaque(true);
        chip.setBackground(new Color(255, 210, 185));
        chip.setForeground(new Color(20, 20, 20));
        chip.setBorder(new LineBorder(new Color(0, 0, 0, 90), 1));
        chip.setMaximumSize(new Dimension(320, 30));
        chip.setAlignmentX(Component.CENTER_ALIGNMENT);
        return chip;
    }

    private JButton makeButton(String text, Color bg, Color fg, int fontSize) {
        JButton b = new JButton(text) {
            boolean hover = false;
            boolean pressed = false;

            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; pressed = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color base = isEnabled() ? bg : new Color(120, 130, 150);
                if (hover && isEnabled()) base = base.brighter();
                if (pressed && isEnabled()) base = base.darker();

                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRoundRect(3, 5, getWidth() - 8, getHeight() - 8, 18, 18);

                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 18, 18);

                g2.setColor(new Color(255, 255, 255, 90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(999, 44));
        b.setPreferredSize(new Dimension(160, 44));
        b.setMargin(new Insets(10, 12, 10, 12));
        return b;
    }

    public void updateMuteButton(boolean isMuted) {
        btnMuteSFX.setText(isMuted ? "Sound: OFF" : "Sound: ON");
        btnMuteSFX.repaint();
    }

    // ✅ FIX: harus Queue<Player>, bukan Queue<MainGameGUI>
    public void updateScoreboard(Queue<Player> players) {
        leaderboardList.removeAll();
        List<Player> sorted = new ArrayList<>(players);
        sorted.sort((a, b) -> b.getScore() - a.getScore());

        int rank = 1;
        for (Player p : sorted) {
            leaderboardList.add(leaderboardRow(rank, p));
            leaderboardList.add(Box.createVerticalStrut(6));
            rank++;
        }

        leaderboardList.revalidate();
        leaderboardList.repaint();
    }

    // ✅ FIX: parameter Player
    private JPanel leaderboardRow(int rank, Player p) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(true);
        row.setBackground(new Color(255, 255, 255, 18));
        row.setBorder(new LineBorder(new Color(255, 255, 255, 40), 1, true));
        row.setMaximumSize(new Dimension(340, 38));

        JLabel r = new JLabel("#" + rank, SwingConstants.CENTER);
        r.setPreferredSize(new Dimension(50, 32));
        r.setForeground(MainGameGUI.UI_TEXT_GOLD);
        r.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(p.getColor());
                g2.fillOval(2, 2, 12, 12);
                g2.setColor(new Color(0, 0, 0, 100));
                g2.drawOval(2, 2, 12, 12);
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(18, 18));

        JLabel name = new JLabel(p.getName());
        name.setForeground(MainGameGUI.TEXT_WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        left.setOpaque(false);
        left.add(dot);
        left.add(name);

        JLabel score = new JLabel(String.valueOf(p.getScore()), SwingConstants.RIGHT);
        score.setForeground(new Color(200, 255, 235));
        score.setFont(new Font("Segoe UI", Font.BOLD, 14));
        score.setPreferredSize(new Dimension(70, 32));

        row.add(r, BorderLayout.WEST);
        row.add(left, BorderLayout.CENTER);
        row.add(score, BorderLayout.EAST);
        return row;
    }

    private void loadLastWinner(String saveFileName) {
        File f = new File(saveFileName);
        if (f.exists()) {
            try (Scanner s = new Scanner(f)) {
                String n = s.hasNextLine() ? s.nextLine() : "None";
                int sc = (s.hasNextLine()) ? Integer.parseInt(s.nextLine()) : 0;
                updateLastWinnerUI(n, sc);
            } catch (Exception ignored) {}
        }
    }

    public void updateLastWinnerUI(String n, int s) {
        lblLastWinnerName.setText(n);
        lblLastWinnerScore.setText("Score: " + s);
    }

    public void updateDiceVisual(int val, boolean isGreen) {
        lastDiceVal = val;
        lastDiceGreen = isGreen;
        dicePanel.repaint();
    }

    public void log(String s) {
        txtLog.append(s + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public void clearLog() { txtLog.setText(""); }
    public void setStatus(String s) { lblStatus.setText(s); }
}
