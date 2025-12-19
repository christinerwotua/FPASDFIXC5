import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class MainGameGUI extends JFrame {

    // =====================================================================================
    // THEME
    // =====================================================================================
    public static final Color ZOO_BG_TOP       = new Color(10, 24, 54);
    public static final Color ZOO_BG_BOTTOM    = new Color(40, 18, 60);

    public static final Color UI_PANEL_BG      = new Color(14, 34, 72);
    public static final Color NEON_CYAN        = new Color(80, 240, 255);
    public static final Color UI_TEXT_GOLD     = new Color(255, 214, 90);
    public static final Color TEXT_WHITE       = new Color(245, 248, 255);
    public static final Color TEXT_MUTED       = new Color(200, 210, 230);

    public static final Color BTN_PRIMARY_BG   = new Color(45, 140, 210);
    public static final Color BTN_ACCENT_BG    = new Color(255, 160, 60);
    public static final Color BTN_DANGER_BG    = new Color(255, 120, 120);

    public static final Color TILE_PAVEMENT    = new Color(52, 56, 70);
    public static final Color TILE_BORDER      = new Color(110, 120, 140);

    public static final Color ITEM_BADGE       = new Color(255, 219, 90);

    public static final Color CHAR_JUDY_BLUE   = new Color(60, 190, 255);
    public static final Color CHAR_NICK_ORANGE = new Color(255, 140, 60);
    public static final Color CHAR_BOGO_GREY   = new Color(135, 145, 155);
    public static final Color CHAR_GAZELLE_GOLD= new Color(255, 210, 80);

    private static final String SAVE_FILE_NAME = "zpd_top_agent.txt";

    // =====================================================================================
    // AUDIO FILE NAMES (taruh .wav di folder project / root)
    // =====================================================================================
    private static final String BGM_FILE        = "backsoundlns.wav";
    private static final String SFX_DICE        = "dice.wav";
    private static final String SFX_WALK        = "walk.wav";      // dipakai audio.playWalkSound()
    private static final String SFX_PAWPSICLE   = "pawpsicle.wav";
    private static final String SFX_CRATE       = "crate.wav";
    private static final String SFX_LADDER      = "ladder.wav";
    private static final String SFX_DENIED      = "denied.wav";
    // win.wav dipakai audio.playWinSound() (cek AudioManager kamu)

    // =====================================================================================
    // PLAYER ICON FILES (PNG recommended, transparent)
    // taruh file png di root project (sejajar .wav) biar kebaca
    // =====================================================================================
    private static final String ICON_JUDY    = "judy.png";
    private static final String ICON_NICK    = "nick.png";
    private static final String ICON_BOGO    = "bogo.png";
    private static final String ICON_GAZELLE = "gazelle.png";
    private static final String ICON_FLASH   = "flash.png"; // opsional

    // =====================================================================================
    // MENU REF
    // =====================================================================================
    private final StartMenuGUI menu;

    // =====================================================================================
    // GAME STATE
    // =====================================================================================
    private Board gameBoard;
    private Queue<Player> turnQueue;

    private GamePanel gamePanel;
    private ControlPanel controlPanel;

    private boolean isGameRunning = false;

    private javax.swing.Timer diceAnimationTimer;
    private javax.swing.Timer movementAnimationTimer;
    private javax.swing.Timer ladderAnimationTimer;
    private javax.swing.Timer lockAnimationTimer;

    // Anim state
    private Player animatingPlayer = null;
    private Point2D.Double currentAnimPos = null;
    private Point2D.Double targetAnimPos = null;
    private boolean currentTurnIsForward = true;

    private int activeLockNodeId = -1;
    private double lockAngle = 0;
    private boolean isLockOpen = false;
    private boolean isLockShaking = false;

    // audio manager
    private final AudioManager audio = new AudioManager();

    private enum Mode { PVP, PVC }
    private Mode lastMode = Mode.PVP;
    private int lastPvPCount = 2;

    // =====================================================================================
    // CONSTRUCTOR
    // =====================================================================================
    public MainGameGUI(StartMenuGUI menu) {
        this.menu = menu;

        setTitle("Zootopia: Pursuit to City Hall - ZPD Training Simulation");
        setSize(1380, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, ZOO_BG_TOP, 0, getHeight(), ZOO_BG_BOTTOM);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainContainer.setLayout(new BorderLayout());
        setContentPane(mainContainer);

        gameBoard = new Board();
        turnQueue = new LinkedList<>();

        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setOpaque(false);
        boardContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        gamePanel = new GamePanel(this);
        gamePanel.setBorder(new LineBorder(NEON_CYAN, 3, true));
        boardContainer.add(gamePanel, BorderLayout.CENTER);
        add(boardContainer, BorderLayout.CENTER);

        controlPanel = new ControlPanel(this);
        add(controlPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                backToMenu();
            }
        });

        SwingUtilities.invokeLater(() -> {
            showRulesDialog();
            setupGameMode(); // cancel => back menu
        });
    }

    // =====================================================================================
    // GETTERS (dipakai GamePanel / ControlPanel)
    // =====================================================================================
    public Board getGameBoard() { return gameBoard; }
    public Queue<Player> getTurnQueue() { return turnQueue; }

    public int getActiveLockNodeId() { return activeLockNodeId; }
    public boolean isLockOpen() { return isLockOpen; }
    public boolean isLockShaking() { return isLockShaking; }

    public Player getAnimatingPlayer() { return animatingPlayer; }
    public Point2D.Double getCurrentAnimPos() { return currentAnimPos; }

    public String getSaveFileName() { return SAVE_FILE_NAME; }

    // =====================================================================================
    // UI callbacks from ControlPanel
    // =====================================================================================
    public void onVolumeChanged(int value) {
        audio.setBgmVolume(value);
    }

    public void toggleSFXMuteFromUI() {
        audio.toggleMuteAll(); // mute BGM + SFX
        controlPanel.updateMuteButton(audio.isMuted());
    }


    // =====================================================================================
    // NAV / CLOSE
    // =====================================================================================
    private void backToMenu() {
        stopAllTimers();
        audio.stopWalkSound();
        audio.stopMusic();
        if (menu != null) menu.setVisible(true);
    }

    private void closeToMenu() {
        dispose();
    }

    // =====================================================================================
    // ICON LOADER (PNG)
    // =====================================================================================
    private Image loadIcon(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) f = new File(System.getProperty("user.dir"), fileName);
        if (!f.exists()) return null; // kalau ga ada, GamePanel fallback pawn lama
        return new ImageIcon(f.getAbsolutePath()).getImage();
    }

    // =====================================================================================
    // SETUP MODE
    // =====================================================================================
    private void setupGameMode() {
        String[] options = {"Agent vs Agent (PvP)", "Agent vs Auto-Bot (PvE)"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Patrol Assignment Mode:",
                "ZPD Shift Manager",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { lastMode = Mode.PVP; setupPvP(); }
        else if (choice == 1) { lastMode = Mode.PVC; setupPvC(); }
        else closeToMenu();
    }

    private void setupPvP() {
        Integer count = askPlayerCountLoop();
        if (count == null) { closeToMenu(); return; }
        lastPvPCount = count;
        initPlayers(count);
    }

    private Integer askPlayerCountLoop() {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "Number of Officers (2-4):");
            if (input == null) return null;
            input = input.trim();
            try {
                int count = Integer.parseInt(input);
                if (count < 2 || count > 4) {
                    JOptionPane.showMessageDialog(this, "Jumlah player harus 2 sampai 4.");
                    continue;
                }
                return count;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Input harus ANGKA. Coba lagi.");
            }
        }
    }

    private void setupPvC() {
        Node startNode = gameBoard.getStartNode();
        Color[] colors = { CHAR_JUDY_BLUE, CHAR_NICK_ORANGE };

        String name = JOptionPane.showInputDialog(this, "Enter Your Badge Name:");
        if (name == null) { closeToMenu(); return; }
        if (name.trim().isEmpty()) name = "Rookie";

        turnQueue.clear();

        Player human = new Player(name, startNode, colors[0], false);
        human.setIcon(loadIcon(ICON_JUDY));

        Player bot = new Player("Flash (Bot)", startNode, colors[1], true);
        bot.setIcon(loadIcon(ICON_FLASH)); // kalau ga ada file, aman (null)

        turnQueue.add(human);
        turnQueue.add(bot);

        finishSetup();
    }

    private void initPlayers(int count) {
        Node startNode = gameBoard.getStartNode();
        Color[] colors = { CHAR_JUDY_BLUE, CHAR_NICK_ORANGE, CHAR_BOGO_GREY, CHAR_GAZELLE_GOLD };

        turnQueue.clear();
        for (int i = 0; i < count; i++) {
            String name = JOptionPane.showInputDialog(this, "Officer " + (i + 1) + " Name:");
            if (name == null) { closeToMenu(); return; }
            if (name.trim().isEmpty()) name = "Officer " + (i + 1);

            Player p = new Player(name, startNode, colors[i], false);

            if (i == 0) p.setIcon(loadIcon(ICON_JUDY));
            if (i == 1) p.setIcon(loadIcon(ICON_NICK));
            if (i == 2) p.setIcon(loadIcon(ICON_BOGO));
            if (i == 3) p.setIcon(loadIcon(ICON_GAZELLE));

            turnQueue.add(p);
        }
        finishSetup();
    }

    private void finishSetup() {
        controlPanel.updateScoreboard(turnQueue);
        controlPanel.setStatus("PRESS START");
        updatePlayerInfoLabels();

        isGameRunning = false;
        controlPanel.btnStart.setEnabled(true);
        controlPanel.btnRoll.setEnabled(false);

        gamePanel.repaint();
    }

    // =====================================================================================
    // RESTART
    // =====================================================================================
    public void restartGame() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Restart game?\n- Reset posisi player\n- Reset points\n- Re-random link",
                "RESTART",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        stopAllTimers();
        audio.stopWalkSound();
        audio.stopMusic();

        animatingPlayer = null;
        currentAnimPos = null;
        targetAnimPos = null;

        activeLockNodeId = -1;
        lockAngle = 0;
        isLockOpen = false;
        isLockShaking = false;

        gameBoard = new Board();

        isGameRunning = false;
        controlPanel.btnRoll.setEnabled(false);
        controlPanel.btnStart.setEnabled(true);
        controlPanel.setStatus("RESTARTED");
        controlPanel.clearLog();
        controlPanel.updateDiceVisual(1, true);

        if (lastMode == Mode.PVC) setupPvC();
        else initPlayers(lastPvPCount);

        gamePanel.repaint();
    }

    private void stopAllTimers() {
        if (diceAnimationTimer != null) diceAnimationTimer.stop();
        if (movementAnimationTimer != null) movementAnimationTimer.stop();
        if (ladderAnimationTimer != null) ladderAnimationTimer.stop();
        if (lockAnimationTimer != null) lockAnimationTimer.stop();
    }

    // =====================================================================================
    // GAME LOOP
    // =====================================================================================
    public void startGame() {
        if (turnQueue.isEmpty()) return;

        isGameRunning = true;
        controlPanel.btnStart.setEnabled(false);
        controlPanel.btnRoll.setEnabled(true);

        updatePlayerInfoLabels();
        controlPanel.setStatus("PATROL STARTED!");

        audio.playBackgroundMusic(BGM_FILE); // loop di AudioManager

        checkBotTurn();
    }

    private void checkBotTurn() {
        Player current = turnQueue.peek();
        if (current != null && current.isBot()) {
            controlPanel.btnRoll.setEnabled(false);
            controlPanel.setStatus("Bot thinking...");
            javax.swing.Timer t = new javax.swing.Timer(1200, e -> processTurn());
            t.setRepeats(false);
            t.start();
        } else {
            controlPanel.btnRoll.setEnabled(true);
            controlPanel.setStatus("Your Move!");
        }
    }

    public void processTurn() {
        if (!isGameRunning || turnQueue.isEmpty()) return;
        controlPanel.btnRoll.setEnabled(false);

        audio.playEffect(SFX_DICE);

        startDiceAnimation();
    }

    private void startDiceAnimation() {
        final int[] frames = {0};
        controlPanel.log("Rolling Dice...");

        diceAnimationTimer = new javax.swing.Timer(45, e -> {
            Random rr = new Random();
            int r = rr.nextInt(6) + 1;
            boolean g = rr.nextBoolean();
            controlPanel.updateDiceVisual(r, g);

            frames[0]++;
            if (frames[0] >= 18) {
                diceAnimationTimer.stop();
                finalizeDiceRoll();
            }
        });
        diceAnimationTimer.start();
    }

    private void finalizeDiceRoll() {
        Player p = turnQueue.peek();
        if (p == null) return;

        // store last node id (prime-start check)
        p.setLastPositionId(p.getCurrentPosition().id);

        Random rand = new Random();
        int finalDice = rand.nextInt(6) + 1;
        boolean isGreen = rand.nextDouble() < 0.70;
        currentTurnIsForward = isGreen;

        controlPanel.updateDiceVisual(finalDice, isGreen);
        controlPanel.log(p.getName() + " rolls " + finalDice + (isGreen ? " (FWD/Green)" : " (BACK/Red)"));

        audio.playWalkSound(); // loop jalan
        startMovementAnimation(p, finalDice, isGreen);
    }

    private void startMovementAnimation(Player player, int totalSteps, boolean isGreen) {
        final int[] stepsTaken = {0};

        movementAnimationTimer = new javax.swing.Timer(220, e -> {
            boolean moveSuccess = false;

            if (isGreen) {
                if (player.getCurrentPosition().next != null) {
                    player.stepForward();
                    moveSuccess = true;
                }
            } else {
                int currentId = player.getCurrentPosition().id;
                if (currentId > 1) {
                    player.setPosition(gameBoard.getNodeById(currentId - 1));
                    moveSuccess = true;
                }
            }

            gamePanel.repaint();
            stepsTaken[0]++;

            if (player.getCurrentPosition().id == 64) {
                movementAnimationTimer.stop();
                audio.stopWalkSound();
                handleWin(player);
                return;
            }

            if (stepsTaken[0] >= totalSteps || !moveSuccess) {
                movementAnimationTimer.stop();
                audio.stopWalkSound();
                checkAndStartLadderSequence(player);
            }
        });
        movementAnimationTimer.start();
    }

    private void checkAndStartLadderSequence(Player player) {
        Node current = player.getCurrentPosition();
        if (current.shortcut != null) {
            boolean isPrevPrime = gameBoard.isPrime(player.getLastPositionId());
            boolean canUse = isPrevPrime && currentTurnIsForward;

            activeLockNodeId = current.id;
            lockAngle = 0;
            isLockOpen = false;
            isLockShaking = false;

            if (canUse) {
                controlPanel.log("Prime Badge OK! Shortcut Access.");
                startLockOpenAnimation(player, true);
            } else {
                controlPanel.log("Access Denied! (Need Prime Start)");
                audio.playEffect(SFX_DENIED);
                startLockOpenAnimation(player, false);
            }
        } else {
            endTurn();
        }
    }

    private void startLockOpenAnimation(Player player, boolean success) {
        final int[] frames = {0};
        lockAnimationTimer = new javax.swing.Timer(28, e -> {
            frames[0]++;
            if (success) {
                isLockOpen = true;
                if (lockAngle < 90) lockAngle += 6;
                if (frames[0] > 26) {
                    lockAnimationTimer.stop();
                    prepareClimb(player);
                }
            } else {
                isLockShaking = true;
                if (frames[0] > 26) {
                    lockAnimationTimer.stop();
                    activeLockNodeId = -1;
                    endTurn();
                }
            }
            gamePanel.repaint();
        });
        lockAnimationTimer.start();
    }

    private void prepareClimb(Player player) {
        Node current = player.getCurrentPosition();
        controlPanel.log("Taking Shortcut...");

        audio.playEffect(SFX_LADDER);

        animatingPlayer = player;

        Point pStart = gamePanel.getCoordinates(current.id);
        Point pEnd   = gamePanel.getCoordinates(current.shortcut.id);
        currentAnimPos = new Point2D.Double(pStart.x, pStart.y);
        targetAnimPos  = new Point2D.Double(pEnd.x, pEnd.y);

        player.setPosition(current.shortcut);
        startLadderClimb();
    }

    private void startLadderClimb() {
        ladderAnimationTimer = new javax.swing.Timer(18, e -> {
            double speed = 8.5;
            double dx = targetAnimPos.x - currentAnimPos.x;
            double dy = targetAnimPos.y - currentAnimPos.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < speed) {
                currentAnimPos = targetAnimPos;
                ladderAnimationTimer.stop();
                animatingPlayer = null;
                activeLockNodeId = -1;
                gamePanel.repaint();
                endTurn();
            } else {
                currentAnimPos.x += (dx / dist) * speed;
                currentAnimPos.y += (dy / dist) * speed;
                gamePanel.repaint();
            }
        });
        ladderAnimationTimer.start();
    }

    private void endTurn() {
        Player p = turnQueue.peek();
        if (p == null) return;

        int currentId = p.getCurrentPosition().id;
        boolean bonusTurn = false;

        if (currentId % 5 == 0 && currentId != 64) {
            audio.playEffect(SFX_CRATE);

            controlPanel.log("SUPPLY BOX! Extra Turn!");
            JOptionPane.showMessageDialog(this, "SUPPLY BOX FOUND!\nExtra Turn for " + p.getName());
            bonusTurn = true;
        }

        int points = gameBoard.getPointsAt(currentId);
        if (points > 0) {
            audio.playEffect(SFX_PAWPSICLE);

            p.addScore(points);
            gameBoard.removePoints(currentId);
            gamePanel.repaint();
            controlPanel.log("Evidence +" + points + " Pawpsicles!");
            JOptionPane.showMessageDialog(this, "EVIDENCE FOUND!\n+" + points + " Pawpsicles!");
            controlPanel.updateScoreboard(turnQueue);
        }

        if (bonusTurn) {
            controlPanel.setStatus("BONUS TURN: " + p.getName());
            if (p.isBot()) {
                javax.swing.Timer t = new javax.swing.Timer(1200, e -> processTurn());
                t.setRepeats(false);
                t.start();
            } else {
                controlPanel.btnRoll.setEnabled(true);
            }
        } else {
            turnQueue.poll();
            turnQueue.add(p);
            updatePlayerInfoLabels();
            checkBotTurn();
        }
    }

    // =====================================================================================
    // WIN
    // =====================================================================================
    private void saveWinner(String name, int score) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE_NAME))) {
            writer.println(name);
            writer.println(score);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWin(Player winner) {
        audio.stopWalkSound();
        audio.stopMusic();
        audio.playWinSound();

        saveWinner(winner.getName(), winner.getScore());
        controlPanel.updateLastWinnerUI(winner.getName(), winner.getScore());

        JOptionPane.showMessageDialog(this,
                "CASE CLOSED!\nWinner: " + winner.getName() + "\nScore: " + winner.getScore(),
                "ZPD REPORT",
                JOptionPane.INFORMATION_MESSAGE);

        controlPanel.setStatus("WINNER: " + winner.getName());
        controlPanel.btnRoll.setEnabled(false);
        isGameRunning = false;
    }

    private void updatePlayerInfoLabels() {
        if (turnQueue.isEmpty()) return;
        Player current = turnQueue.peek();
        controlPanel.lblCurrentName.setText(current.getName());

        if (turnQueue.size() > 1) {
            Object[] players = turnQueue.toArray();
            Player next = (Player) players[1];
            controlPanel.lblNextName.setText("NEXT: " + next.getName());
        } else {
            controlPanel.lblNextName.setText("NEXT: -");
        }
    }

    // =====================================================================================
    // RULES DIALOG
    // =====================================================================================
    private void showRulesDialog() {
        JDialog d = new JDialog(this, "ZPD: FIELD MANUAL", true);
        d.setSize(560, 520);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);

        JPanel bg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(245, 248, 255),
                        0, getHeight(), new Color(220, 235, 255)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            }
        };
        bg.setLayout(new BorderLayout());
        bg.setBorder(new EmptyBorder(18, 18, 18, 18));
        d.setContentPane(bg);

        JLabel title = new JLabel("ZPD PATROL FIELD MANUAL");
        title.setFont(new Font("Impact", Font.PLAIN, 30));
        title.setForeground(new Color(12, 28, 58));

        JLabel sub = new JLabel("Rules & Mission Briefing");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(50, 70, 95));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        bg.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 0, 12, 0));

        content.add(ruleCard("🎲 Supply Crate (Kelipatan 5)", "Mendarat di node kelipatan 5 → extra turn."));
        content.add(Box.createVerticalStrut(10));
        content.add(ruleCard("⭐ Prime Badge Access", "Jika start posisi giliranmu bilangan prima, shortcut aktif.\nShortcut dihitung 1 step."));
        content.add(Box.createVerticalStrut(10));
        content.add(ruleCard("🐾 Pawpsicles", "Ambil pawpsicle untuk tambah score."));
        content.add(Box.createVerticalStrut(10));
        content.add(ruleCard("🏛 City Hall", "Capai node 64 untuk menang."));

        JScrollPane sc = new JScrollPane(content);
        sc.setBorder(null);
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        bg.add(sc, BorderLayout.CENTER);

        JButton ok = new JButton("I UNDERSTAND");
        ok.setBackground(new Color(12, 28, 58));
        ok.setForeground(UI_TEXT_GOLD);
        ok.setFont(new Font("Arial", Font.BOLD, 14));
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
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 200, 230), 2, true),
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
}
