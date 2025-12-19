import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StartMenuGUI extends JFrame {

    // Theme (selaras dengan MainGameGUI)
    private static final Color ZOO_BG_TOP       = new Color(10, 24, 54);
    private static final Color ZOO_BG_BOTTOM    = new Color(40, 18, 60);

    private static final Color NEON_CYAN        = new Color(80, 240, 255);
    private static final Color GOLD             = new Color(255, 214, 90);
    private static final Color TEXT_WHITE       = new Color(245, 248, 255);
    private static final Color TEXT_MUTED       = new Color(200, 210, 230);

    private static final Color BTN_PRIMARY_BG   = new Color(45, 140, 210);
    private static final Color BTN_ACCENT_BG    = new Color(255, 160, 60);
    private static final Color BTN_DANGER_BG    = new Color(255, 120, 120);

    public StartMenuGUI() {
        setTitle("Zootopia: Pursuit to City Hall");
        setSize(980, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, ZOO_BG_TOP, 0, getHeight(), ZOO_BG_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // glow bars
                g2.setColor(new Color(80, 240, 255, 26));
                g2.fillRoundRect(36, 34, getWidth() - 72, 10, 10, 10);
                g2.fillRoundRect(36, getHeight() - 50, getWidth() - 72, 10, 10, 10);
            }
        };
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(26, 26, 26, 26));
        setContentPane(root);

        // =========================
        // LEFT HERO
        // =========================
        JPanel hero = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // city silhouette
                g2.setColor(new Color(0, 0, 0, 55));
                for (int i = 0; i < 10; i++) {
                    int bw = 46 + i * 8;
                    int bh = 110 + (i % 3) * 44;
                    int x = 24 + i * 52;
                    int y = h - bh - 34;

                    g2.fillRoundRect(x, y, bw, bh, 12, 12);

                    g2.setColor(new Color(80, 240, 255, 16));
                    g2.drawRoundRect(x, y, bw, bh, 12, 12);

                    g2.setColor(new Color(0, 0, 0, 55));
                }

                // Title block
                g2.setFont(new Font("Impact", Font.PLAIN, 56));
                g2.setColor(GOLD);
                g2.drawString("ZOOTOPIA", 22, 88);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                g2.setColor(TEXT_WHITE);
                g2.drawString("Pursuit to City Hall", 24, 128);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.setColor(new Color(210, 220, 240));
                g2.drawString("Green: forward • Red: back • Prime-start: shortcut access • Finish: 64", 24, 158);

                // badge line
                int cx = 60, cy = 218;
                drawBadge(g2, cx, cy);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(NEON_CYAN);
                g2.drawString("ZPD Training Simulation", 96, 224);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(TEXT_MUTED);
                g2.drawString("Tip: Restart tersedia saat game berjalan.", 96, 246);
            }

            private void drawBadge(Graphics2D g2, int cx, int cy) {
                g2.setColor(new Color(255, 219, 90));
                int s = 32;
                int[] xP = {cx, cx + s / 2, cx, cx - s / 2};
                int[] yP = {cy - s / 2, cy, cy + s / 2, cy};
                g2.fillPolygon(xP, yP, 4);

                g2.setColor(new Color(60, 90, 140));
                g2.setStroke(new BasicStroke(2f));
                g2.drawPolygon(xP, yP, 4);

                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillOval(cx - 5, cy - 5, 10, 10);
            }
        };
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(560, 0));
        root.add(hero, BorderLayout.CENTER);

        // =========================
        // RIGHT MENU CARD
        // =========================
        JPanel right = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // translucent card
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // border glow
                g2.setColor(new Color(80, 240, 255, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 24, 24);
            }
        };
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(24, 24, 24, 24));
        right.setPreferredSize(new Dimension(320, 0));
        root.add(right, BorderLayout.EAST);

        JLabel menuTitle = new JLabel("MAIN MENU", SwingConstants.CENTER);
        menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menuTitle.setForeground(NEON_CYAN);

        JLabel hint = new JLabel(
                "<html><div style='text-align:center;'>Start game, choose mode,<br/>and chase to City Hall!</div></html>",
                SwingConstants.CENTER
        );
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(220, 230, 245));

        JButton btnPlay = makeButton("PLAY", BTN_ACCENT_BG, TEXT_WHITE);
        btnPlay.addActionListener(e -> {
            this.setVisible(false);
            new MainGameGUI(this);
        });

        JButton btnHow = makeButton("HOW TO PLAY", BTN_PRIMARY_BG, TEXT_WHITE);
        btnHow.addActionListener(e -> showHowToPlayDialog());

        JButton btnExit = makeButton("EXIT", BTN_DANGER_BG, TEXT_WHITE);
        btnExit.addActionListener(e -> System.exit(0));

        right.add(menuTitle);
        right.add(Box.createVerticalStrut(10));
        right.add(hint);
        right.add(Box.createVerticalStrut(22));
        right.add(btnPlay);
        right.add(Box.createVerticalStrut(10));
        right.add(btnHow);
        right.add(Box.createVerticalStrut(10));
        right.add(btnExit);
        right.add(Box.createVerticalGlue());

        setVisible(true);
    }

    // =========================
    // HOW TO PLAY (rapih, no white box)
    // =========================
    private void showHowToPlayDialog() {
        JDialog d = new JDialog(this, "HOW TO PLAY", true);
        d.setSize(560, 520);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);

        JPanel bg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                // jangan super.paintComponent supaya tidak “white fill”
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int W = getWidth();
                int H = getHeight();

                // clear transparent
                g2.setComposite(AlphaComposite.Src);
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRect(0, 0, W, H);
                g2.setComposite(AlphaComposite.SrcOver);

                // dialog card gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 248, 255),
                        0, H, new Color(220, 235, 255));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, W, H, 18, 18);

                // border
                g2.setColor(new Color(80, 120, 170));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, W - 3, H - 3, 18, 18);

                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setLayout(new BorderLayout());
        bg.setBorder(new EmptyBorder(18, 18, 18, 18));
        d.setContentPane(bg);

        // header
        JLabel title = new JLabel("ZPD PATROL GUIDE");
        title.setFont(new Font("Impact", Font.PLAIN, 30));
        title.setForeground(new Color(12, 28, 58));

        JLabel sub = new JLabel("Quick rules to win the chase");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(50, 70, 95));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        bg.add(header, BorderLayout.NORTH);

        // content cards
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 0, 12, 0));

        content.add(ruleCard("🎲 Dice Colors", "Green (70%) = maju, Red (30%) = mundur."));
        content.add(Box.createVerticalStrut(10));
        content.add(ruleCard("⭐ Prime-start Shortcut", "Jika posisi awal giliranmu bilangan prima, shortcut aktif.\nShortcut dihitung 1 step."));
        content.add(Box.createVerticalStrut(10));
        content.add(ruleCard("📦 Supply Crate (×5)", "Mendarat di node kelipatan 5 → bonus turn."));
        content.add(Box.createVerticalStrut(10));
        content.add(ruleCard("🐾 Pawpsicles", "Ambil pawpsicle untuk tambah score (evidence)."));
        content.add(Box.createVerticalStrut(10));
        content.add(ruleCard("🏛 City Hall", "Capai node 64 untuk menang."));

        JScrollPane sc = new JScrollPane(content);
        sc.setBorder(null);
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        bg.add(sc, BorderLayout.CENTER);

        JButton ok = new JButton("GOT IT");
        ok.setBackground(new Color(12, 28, 58));
        ok.setForeground(GOLD);
        ok.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ok.setFocusPainted(false);
        ok.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        ok.addActionListener(e -> d.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(ok);
        bg.add(footer, BorderLayout.SOUTH);

        d.setVisible(true);
    }

    private JPanel ruleCard(String heading, String body) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230), 2, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel h = new JLabel(heading);
        h.setFont(new Font("Segoe UI", Font.BOLD, 15));
        h.setForeground(new Color(12, 28, 58));

        JTextArea b = new JTextArea(body);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(new Color(50, 60, 80));
        b.setEditable(false);
        b.setOpaque(false);
        b.setLineWrap(true);
        b.setWrapStyleWord(true);

        card.add(h, BorderLayout.NORTH);
        card.add(b, BorderLayout.CENTER);
        return card;
    }

    // =========================
    // BUTTON (custom paint, smooth)
    // =========================
    private JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            boolean hover = false;
            boolean pressed = false;

            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setHorizontalAlignment(SwingConstants.CENTER);

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

                int arc = 18;
                int shadowX = 3, shadowY = 5;

                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRoundRect(2 + shadowX, 4 + shadowY, getWidth() - 6, getHeight() - 6, arc, arc);

                g2.setColor(base);
                g2.fillRoundRect(2, 4, getWidth() - 6, getHeight() - 6, arc, arc);

                g2.setColor(new Color(255, 255, 255, 90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(2, 4, getWidth() - 6, getHeight() - 6, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(999, 46));
        b.setPreferredSize(new Dimension(220, 46));
        b.setMargin(new Insets(12, 12, 12, 12));
        return b;
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        new StartMenuGUI();
    }
}
