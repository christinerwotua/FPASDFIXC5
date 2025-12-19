import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.net.URL;

// --- PANEL ROUNDED & TRANSPARAN (DASHBOARD LOOK) ---
class RoundedTransparentPanel extends JPanel {
    private int radius;
    public RoundedTransparentPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
        setBackground(new Color(255, 255, 255, 45));
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
    }
}

// --- PANEL BACKGROUND (CENTER-CROP LOGIC) ---
class BackgroundPanel extends JPanel {
    private Image bgImage;
    private int alpha;
    public BackgroundPanel(String fileName, int alpha) {
        this.alpha = alpha;
        try { bgImage = new ImageIcon(getClass().getResource(fileName)).getImage(); }
        catch (Exception e) { System.out.println("Gagal muat gambar: " + fileName); }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            Graphics2D g2 = (Graphics2D) g;
            float scale = Math.max((float) getWidth() / bgImage.getWidth(null), (float) getHeight() / bgImage.getHeight(null));
            int sw = (int) (bgImage.getWidth(null) * scale);
            int sh = (int) (bgImage.getHeight(null) * scale);
            g2.drawImage(bgImage, (getWidth() - sw) / 2, (getHeight() - sh) / 2, sw, sh, this);
            g2.setColor(new Color(34, 47, 91, alpha));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}

// --- TOMBOL MODERN DENGAN HOVER EFFECT ---
class RoundedButton extends JButton {
    private Color normalColor = new Color(135, 54, 30);
    private Color hoverColor = new Color(185, 74, 40);

    public RoundedButton(String label) {
        super(label);
        setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
        setForeground(Color.WHITE); setFont(new Font("Poppins", Font.BOLD, 18));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getModel().isRollover() ? hoverColor : normalColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        super.paintComponent(g);
        g2.dispose();
    }
}

public class Main {
    private static JFrame frame;
    private static CardLayout cardLayout;
    private static JPanel container;
    private static String currentPlayerName, currentMode;
    private static int secondsElapsed = 0;
    private static javax.swing.Timer timer;
    private static JLabel timerLabel, costLabel;
    private static JTextPane explanationArea;
    private static JList<String> leaderboardDisplay;
    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private static List<ScoreRecord> leaderboardData = new ArrayList<>();

    private static Clip backgroundMusic;
    private static boolean isMuted = false;
    private static RoundedButton soundBtn;

    public static void main(String[] args) {
        playMusic("/backsound.wav");
        frame = new JFrame("Zootopia: Case Solver");
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        showWelcomingPage();
        frame.add(container);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void playMusic(String path) {
        try {
            URL url = Main.class.getResource(path);
            if (url == null) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(ais);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void toggleMute() {
        if (backgroundMusic == null) return;
        if (isMuted) {
            backgroundMusic.start();
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            soundBtn.setText("🔊 SOUND: ON");
        } else {
            backgroundMusic.stop();
            soundBtn.setText("🔇 SOUND: OFF");
        }
        isMuted = !isMuted;
    }

    private static void showWelcomingPage() {
        // Overlay background lebih tipis (140) agar gambar asli lebih cerah
        BackgroundPanel welcome = new BackgroundPanel("welcomingpageImage.jpg", 140);
        welcome.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- WRAPPER BOX (Untuk efek Dashboard) ---
        RoundedTransparentPanel glassBox = new RoundedTransparentPanel(40);
        glassBox.setPreferredSize(new Dimension(650, 450));
        glassBox.setLayout(new GridBagLayout());
        GridBagConstraints boxGbc = new GridBagConstraints();
        boxGbc.insets = new Insets(10, 20, 10, 20);

        // JUDUL DENGAN SHADOW 3D
        JLabel title = new JLabel("ZOOTOPIA CASE SOLVER") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(new Color(0, 0, 0, 150)); // Shadow
                g2.drawString(getText(), 4, 54);
                g2.setColor(new Color(241, 196, 15)); // Gold Color
                g2.drawString(getText(), 0, 50);
                g2.dispose();
            }
        };
        title.setFont(new Font("Poppins", Font.BOLD, 48));
        boxGbc.gridx = 0; boxGbc.gridy = 0; boxGbc.gridwidth = 2;
        boxGbc.insets = new Insets(0, 0, 40, 0);
        glassBox.add(title, boxGbc);

        // FORM FIELDS
        boxGbc.gridwidth = 1; boxGbc.insets = new Insets(10, 10, 10, 10);

        JLabel nl = new JLabel("OFFICER NAME:");
        nl.setFont(new Font("Poppins", Font.BOLD, 20)); nl.setForeground(Color.WHITE);
        boxGbc.gridx = 0; boxGbc.gridy = 1; boxGbc.anchor = GridBagConstraints.EAST;
        glassBox.add(nl, boxGbc);

        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("Poppins", Font.PLAIN, 18));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        boxGbc.gridx = 1; boxGbc.anchor = GridBagConstraints.WEST;
        glassBox.add(nameField, boxGbc);

        JLabel ml = new JLabel("MISSION TYPE:");
        ml.setFont(new Font("Poppins", Font.BOLD, 20)); ml.setForeground(Color.WHITE);
        boxGbc.gridx = 0; boxGbc.gridy = 2; boxGbc.anchor = GridBagConstraints.EAST;
        glassBox.add(ml, boxGbc);

        String[] modes = {"Learning Mode", "Rank Mode"};
        JComboBox<String> modeBox = new JComboBox<>(modes);
        modeBox.setFont(new Font("Poppins", Font.PLAIN, 18));
        boxGbc.gridx = 1; boxGbc.anchor = GridBagConstraints.WEST;
        glassBox.add(modeBox, boxGbc);

        // START BUTTON
        RoundedButton start = new RoundedButton("START MISSION");
        start.setPreferredSize(new Dimension(250, 50));
        start.addActionListener(e -> {
            currentPlayerName = nameField.getText().trim().isEmpty() ? "Judy" : nameField.getText();
            currentMode = (String) modeBox.getSelectedItem();
            startGame();
        });
        boxGbc.gridx = 0; boxGbc.gridy = 3; boxGbc.gridwidth = 2;
        boxGbc.anchor = GridBagConstraints.CENTER;
        boxGbc.insets = new Insets(40, 0, 0, 0);
        glassBox.add(start, boxGbc);

        // Menambahkan glassBox ke center layar
        welcome.add(glassBox);
        container.add(welcome, "WELCOME");
        cardLayout.show(container, "WELCOME");
    }

    private static void startGame() {
        JPanel game = new JPanel(new BorderLayout());
        costLabel = new JLabel("TOTAL COST: 0");

        explanationArea = new JTextPane();
        explanationArea.setEditable(false);
        explanationArea.setOpaque(false);
        explanationArea.setBackground(new Color(0,0,0,0));
        explanationArea.setForeground(Color.WHITE);
        explanationArea.setFont(new Font("Poppins", Font.ITALIC, 16));

        javax.swing.text.StyledDocument doc = explanationArea.getStyledDocument();
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        MazePanel maze = new MazePanel(35, 25, costLabel, explanationArea, currentPlayerName, currentMode);
        game.add(maze, BorderLayout.CENTER);
        game.add(createSidebar(maze), BorderLayout.EAST);

        container.add(game, "GAME");
        cardLayout.show(container, "GAME");
        maze.requestFocusInWindow();
        if(currentMode.equals("Rank Mode")) startTimer();
    }

    private static JPanel createSidebar(MazePanel maze) {
        BackgroundPanel sidebar = new BackgroundPanel("backgroundImage.jpg", 200);
        sidebar.setPreferredSize(new Dimension(380, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel info = new JLabel("OFFICER: " + currentPlayerName.toUpperCase());
        info.setForeground(new Color(241, 196, 15));
        info.setFont(new Font("Poppins", Font.BOLD, 22));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(info);

        soundBtn = new RoundedButton(isMuted ? "🔇 SOUND: OFF" : "🔊 SOUND: ON");
        soundBtn.setFont(new Font("Poppins", Font.BOLD, 14));
        soundBtn.addActionListener(e -> toggleMute());
        soundBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(soundBtn);

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        if(currentMode.equals("Rank Mode")) {
            timerLabel = new JLabel("TIME: 00:00");
            timerLabel.setFont(new Font("Poppins", Font.BOLD, 42));
            timerLabel.setForeground(new Color(241, 196, 15));
            timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(timerLabel);
            sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

            RoundedTransparentPanel rankPanel = new RoundedTransparentPanel(20);
            rankPanel.setLayout(new BorderLayout());
            rankPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            leaderboardDisplay = new JList<>(listModel);
            leaderboardDisplay.setOpaque(false);
            leaderboardDisplay.setBackground(new Color(0,0,0,0));
            leaderboardDisplay.setForeground(Color.WHITE);
            leaderboardDisplay.setFont(new Font("Poppins", Font.BOLD, 16));

            JScrollPane scroll = new JScrollPane(leaderboardDisplay);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);

            rankPanel.add(scroll);
            sidebar.add(rankPanel);
        } else {
            String[] bts = {"BFS", "DFS", "DIJKSTRA", "A*"};
            for(String b : bts) {
                RoundedButton btn = new RoundedButton(b);
                btn.addActionListener(e -> { maze.solve(b); maze.requestFocusInWindow(); });
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                sidebar.add(btn); sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
            }

            RoundedTransparentPanel roundedArea = new RoundedTransparentPanel(20);
            roundedArea.setLayout(new BorderLayout());
            roundedArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            roundedArea.add(explanationArea);

            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(roundedArea);
        }

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        addNavButton(sidebar, "NEW MAZE", e -> { maze.initGrid(); maze.requestFocusInWindow(); if(currentMode.equals("Rank Mode")) startTimer(); });
        addNavButton(sidebar, "CHANGE MODE", e -> { if(timer != null) timer.stop(); showWelcomingPage(); });
        addNavButton(sidebar, "EXIT GAME", e -> System.exit(0));

        sidebar.add(Box.createVerticalGlue());
        costLabel.setFont(new Font("Poppins", Font.BOLD, 28));
        costLabel.setForeground(new Color(46, 204, 113));
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(costLabel);

        return sidebar;
    }

    private static void addNavButton(JPanel p, String t, ActionListener a) {
        RoundedButton b = new RoundedButton(t);
        b.addActionListener(a); b.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(b); p.add(Box.createRigidArea(new Dimension(0, 12)));
    }

    private static void startTimer() {
        if(timer != null) timer.stop();
        secondsElapsed = 0;
        timer = new javax.swing.Timer(1000, e -> {
            secondsElapsed++;
            timerLabel.setText(String.format("TIME: %02d:%02d", secondsElapsed/60, secondsElapsed%60));
        });
        timer.start();
    }

    public static void missionCompleteInRank() {
        if(timer != null) timer.stop();
        leaderboardData.add(new ScoreRecord(currentPlayerName, secondsElapsed));
        Collections.sort(leaderboardData, (a, b) -> Integer.compare(a.time, b.time));
        listModel.clear();
        for(int i = 0; i < Math.min(5, leaderboardData.size()); i++) {
            ScoreRecord r = leaderboardData.get(i);
            listModel.addElement((i+1) + ". " + r.name + " (" + (r.time/60) + ":" + (r.time%60) + ")");
        }
    }

    public static void playWinSound() {
        try {
            URL url = Main.class.getResource("/win.wav"); // Pastikan file format .wav
            if (url == null) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
            Clip winClip = AudioSystem.getClip();
            winClip.open(ais);
            winClip.start(); // Putar sekali saja, tidak perlu looping
        } catch (Exception e) {
            System.out.println("Gagal memutar suara kemenangan!");
        }
    }
}

class ScoreRecord {
    String name; int time;
    ScoreRecord(String n, int t) { this.name = n; this.time = t; }
}
