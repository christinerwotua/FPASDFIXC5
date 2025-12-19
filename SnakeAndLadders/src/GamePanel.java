import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel {

    private final MainGameGUI host;

    public GamePanel(MainGameGUI host) {
        this.host = host;
        setOpaque(false);
    }

    public Point getCoordinates(int id) {
        int cols = 8, rows = 8;
        int w = getWidth() / cols;
        int h = getHeight() / rows;
        int mathRow = (id - 1) / 8;
        int col = (mathRow % 2 == 0) ? (id - 1) % 8 : 7 - ((id - 1) % 8);
        return new Point(col * w + w / 2, (7 - mathRow) * h + h / 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Board gameBoard = host.getGameBoard();
        Queue<Player> turnQueue = host.getTurnQueue();

        int cols = 8, rows = 8;
        int w = getWidth() / cols;
        int h = getHeight() / rows;

        // tiles
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * w;
                int y = r * h;
                int mathRow = 7 - r;
                int id = (mathRow % 2 == 0) ? (mathRow * 8) + c + 1 : (mathRow * 8) + (7 - c) + 1;
                drawCityTile(g2, x, y, w, h, id);
            }
        }

        // ladders/shortcuts
        for (int i = 1; i <= 64; i++) {
            Node n = gameBoard.getNodeById(i);
            if (n != null && n.shortcut != null) {
                Point p1 = getCoordinates(n.id);
                Point p2 = getCoordinates(n.shortcut.id);
                drawLadderReal(g2, p1.x, p1.y, p2.x, p2.y);

                boolean isOpen = false;
                Color lockColor = new Color(160, 160, 160);
                if (n.id == host.getActiveLockNodeId()) {
                    isOpen = host.isLockOpen();
                    if (host.isLockShaking()) lockColor = new Color(255, 80, 80);
                }
                drawScanner(g2, p1.x - 10, p1.y - 15, isOpen, lockColor);
            }
        }

        // players
        if (turnQueue != null) {
            Map<Integer, List<Player>> playersOnNode = new HashMap<>();
            for (Player p : turnQueue) {
                playersOnNode.computeIfAbsent(p.getCurrentPosition().id, k -> new ArrayList<>()).add(p);
            }

            for (Integer nodeId : playersOnNode.keySet()) {
                List<Player> occ = playersOnNode.get(nodeId);
                Point pos = getCoordinates(nodeId);
                int startX = pos.x - ((occ.size() - 1) * 18) / 2;
                for (int i = 0; i < occ.size(); i++) {
                    Player p = occ.get(i);
                    if (p == host.getAnimatingPlayer() && host.getCurrentAnimPos() != null) continue;
                    drawPawnCharacter(g2, p, startX + (i * 18), pos.y);
                }
            }

            if (host.getAnimatingPlayer() != null && host.getCurrentAnimPos() != null) {
                Point2D.Double ap = host.getCurrentAnimPos();
                drawPawnCharacter(g2, host.getAnimatingPlayer(), (int) ap.x, (int) ap.y);
            }
        }
    }

    private void drawCityTile(Graphics2D g2, int x, int y, int w, int h, int id) {
        Board gameBoard = host.getGameBoard();

        g2.setColor(MainGameGUI.TILE_PAVEMENT);
        g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 8, 8);

        g2.setColor(MainGameGUI.TILE_BORDER);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 8, 8);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(10, 20, 40, 180));
        g2.drawString(String.valueOf(id), x + 10, y + 22);

        if (gameBoard.isPrime(id)) drawGoldBadge(g2, x + w - 20, y + 12);
        if (id % 5 == 0 && id != 64) drawSupplyBox(g2, x + w - 16, y + h - 16);

        int points = gameBoard.getPointsAt(id);
        if (points > 0) drawPawpsicle(g2, x + 26, y + h - 18);

        if (id == 64) drawCityHallFinish(g2, x, y, w, h);
    }

    private void drawCityHallFinish(Graphics2D g2, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int baseY = y + h - 16;

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(cx - 18, baseY - 6, 36, 12);

        GradientPaint gp = new GradientPaint(x, y, new Color(210, 225, 245),
                x, y + h, new Color(120, 150, 190));
        g2.setPaint(gp);
        g2.fillRoundRect(cx - 16, y + 18, 32, 28, 10, 10);

        g2.setColor(new Color(255, 214, 90));
        int[] rx = {cx - 18, cx, cx + 18};
        int[] ry = {y + 22, y + 6, y + 22};
        g2.fillPolygon(rx, ry, 3);

        g2.setColor(new Color(70, 90, 120, 160));
        for (int i = -10; i <= 10; i += 10) {
            g2.fillRoundRect(cx + i - 2, y + 26, 4, 18, 4, 4);
        }

        g2.setColor(new Color(0, 0, 0, 85));
        g2.fillRoundRect(x + 6, y + h / 2 - 12, w - 12, 22, 12, 12);

        g2.setColor(MainGameGUI.NEON_CYAN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x + 6, y + h / 2 - 12, w - 12, 22, 12, 12);

        g2.setFont(new Font("Impact", Font.PLAIN, 14));
        g2.setColor(MainGameGUI.TEXT_WHITE);
        String label = "CITY HALL";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + h / 2 + 5);
    }

    private void drawGoldBadge(Graphics2D g2, int cx, int cy) {
        int s = 12;
        g2.setColor(MainGameGUI.ITEM_BADGE);
        int[] xP = {cx, cx + s / 2, cx, cx - s / 2};
        int[] yP = {cy - s / 2, cy, cy + s / 2, cy};
        g2.fillPolygon(xP, yP, 4);
        g2.setColor(new Color(60, 90, 140));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(xP, yP, 4);
    }

    private void drawPawpsicle(Graphics2D g2, int cx, int cy) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);

        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillOval(-10, 10, 20, 6);

        g2.setColor(new Color(170, 120, 70));
        RoundRectangle2D stick = new RoundRectangle2D.Double(-3, 4, 6, 14, 4, 4);
        g2.fill(stick);
        g2.setColor(new Color(95, 65, 35));
        g2.draw(stick);

        GradientPaint iceGp = new GradientPaint(0, -12, new Color(255, 150, 160),
                0, 8, new Color(220, 40, 55));
        g2.setPaint(iceGp);
        g2.fill(new Ellipse2D.Double(-10, -10, 20, 20));

        g2.setPaint(new GradientPaint(0, -16, new Color(255, 175, 180),
                0, -6, new Color(220, 50, 65)));
        g2.fill(new Ellipse2D.Double(-11, -15, 7, 7));
        g2.fill(new Ellipse2D.Double(-4, -18, 8, 8));
        g2.fill(new Ellipse2D.Double(4, -15, 7, 7));

        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillOval(-7, -8, 6, 12);

        g2.setTransform(old);
    }

    private void drawSupplyBox(Graphics2D g2, int cx, int cy) {
        int s = 16;
        int x = cx - s / 2;
        int y = cy - s / 2;

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(x + 2, y + 3, s, s, 6, 6);

        GradientPaint gp = new GradientPaint(x, y, new Color(185, 120, 70),
                x, y + s, new Color(110, 65, 30));
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, s, s, 6, 6);

        g2.setColor(new Color(90, 240, 255, 200));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawRoundRect(x, y, s, s, 6, 6);

        g2.setColor(new Color(255, 255, 255, 220));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("?", x + 6, y + 12);
    }

    private void drawLadderReal(Graphics2D g2, int x1, int y1, int x2, int y2) {
        Stroke oldStroke = g2.getStroke();
        Composite oldComp = g2.getComposite();

        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len < 1) return;

        double ux = dx / len, uy = dy / len;
        double nx = -uy, ny = ux;

        double endPadding = 22;
        double sx = x1 + ux * endPadding;
        double sy = y1 + uy * endPadding;
        double ex = x2 - ux * endPadding;
        double ey = y2 - uy * endPadding;

        double ladderLen = Math.hypot(ex - sx, ey - sy);
        if (ladderLen < 1) return;

        double halfWidth = 7.0;

        double r1x1 = sx + nx * halfWidth, r1y1 = sy + ny * halfWidth;
        double r1x2 = ex + nx * halfWidth, r1y2 = ey + ny * halfWidth;

        double r2x1 = sx - nx * halfWidth, r2y1 = sy - ny * halfWidth;
        double r2x2 = ex - nx * halfWidth, r2y2 = ey - ny * halfWidth;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.28f));
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(r1x1 + 2, r1y1 + 2, r1x2 + 2, r1y2 + 2));
        g2.draw(new Line2D.Double(r2x1 + 2, r2y1 + 2, r2x2 + 2, r2y2 + 2));

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
        g2.setColor(new Color(235, 240, 250));
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(r1x1, r1y1, r1x2, r1y2));
        g2.draw(new Line2D.Double(r2x1, r2y1, r2x2, r2y2));

        int rungCount = Math.max(4, (int) (ladderLen / 22.0));
        double step = ladderLen / rungCount;
        double ldx = (ex - sx) / ladderLen, ldy = (ey - sy) / ladderLen;

        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < rungCount; i++) {
            double px = sx + ldx * (i * step);
            double py = sy + ldy * (i * step);

            double ax = px + nx * (halfWidth - 0.5);
            double ay = py + ny * (halfWidth - 0.5);
            double bx = px - nx * (halfWidth - 0.5);
            double by = py - ny * (halfWidth - 0.5);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2.setColor(Color.BLACK);
            g2.draw(new Line2D.Double(ax + 1.2, ay + 1.2, bx + 1.2, by + 1.2));

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.setColor(new Color(235, 240, 250));
            g2.draw(new Line2D.Double(ax, ay, bx, by));
        }

        g2.setComposite(oldComp);
        g2.setStroke(oldStroke);
    }

    private void drawScanner(Graphics2D g2, int x, int y, boolean isOpen, Color c) {
        g2.setColor(new Color(30, 35, 50));
        g2.fillRoundRect(x, y, 20, 26, 6, 6);

        g2.setColor(isOpen ? new Color(90, 230, 160) : c);
        g2.fillOval(x + 5, y + 5, 10, 10);

        g2.setColor(new Color(255, 255, 255, 90));
        g2.drawRoundRect(x, y, 20, 26, 6, 6);
    }

    // ===========================
    // IMPORTANT: ICON SUPPORT HERE
    // ===========================
    private void drawPawnCharacter(Graphics2D g2, Player p, int cx, int cy) {
        // Kalau player punya icon, gambar icon sebagai kotak
        Image icon = p.getIcon(); // pastikan di Player ada getIcon()
        if (icon != null) {
            int size = 24;              // ukuran kotak icon (silakan 18-24)
            int x = cx - size / 2;
            int y = cy - size / 2;

            // shadow biar keliatan naik sedikit (rapih)
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(x + 2, y + 3, size, size, 6, 6);

            // background kotak (buat transparansi icon yg bolong biar ga “hilang”)
            g2.setColor(new Color(245, 245, 245, 220));
            g2.fillRoundRect(x, y, size, size, 6, 6);

            // gambar icon di dalam kotak
            g2.drawImage(icon, x + 2, y + 2, size - 4, size - 4, null);

            // border kotak
            g2.setColor(new Color(20, 20, 20, 140));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x, y, size, size, 6, 6);

            return; // stop di sini biar gak gambar pawn lingkaran lagi
        }

        // -------------------------------------------------------------------------
        // FALLBACK: kalau icon belum ada, baru pakai pawn lama (yang lingkaran)
        // -------------------------------------------------------------------------
        Color base = p.getColor();

        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(-11, 12, 22, 7);

        g2.setColor(base.darker());
        g2.fillRoundRect(-7, 3, 14, 12, 8, 8);

        g2.setColor(base);
        g2.fillOval(-10, -12, 20, 20);

        g2.setColor(Color.WHITE);
        g2.fillOval(-6, -5, 5, 5);
        g2.fillOval(1, -5, 5, 5);
        g2.setColor(Color.BLACK);
        g2.fillOval(-5, -4, 2, 2);
        g2.fillOval(2, -4, 2, 2);

        g2.setColor(new Color(255, 219, 90));
        g2.fillOval(-2, 7, 4, 4);

        g2.setTransform(old);
    }



    private boolean closeColor(Color a, Color b) {
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return (dr * dr + dg * dg + db * db) < 2500;
    }
}
