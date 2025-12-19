import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MazePanel extends JPanel {
    private int cols, rows;
    private Node[][] grid;
    private Node player, startNode, endNode;
    private List<Node> visitedOrder = new ArrayList<>();
    private List<Node> finalPath = new ArrayList<>();
    private Stack<Node> manualPathStack = new Stack<>();
    private JLabel costDisplay;
    private JTextPane explanationArea;
    private boolean isSolving = false;
    private String officerName, gameMode;

    private Image playerIcon, entryIcon, exitIcon;
    private int playerDirection = 1; // 1 untuk Kanan, -1 untuk Kiri

    public MazePanel(int cols, int rows, JLabel costDisplay, JTextPane explanationArea, String name, String mode) {
        this.cols = cols; this.rows = rows;
        this.costDisplay = costDisplay;
        this.explanationArea = explanationArea;
        this.officerName = name; this.gameMode = mode;

        try {
            playerIcon = new ImageIcon(getClass().getResource("/icon.png")).getImage();
            entryIcon = new ImageIcon(getClass().getResource("/entrypoint.png")).getImage();
            exitIcon = new ImageIcon(getClass().getResource("/exitpoint.png")).getImage();
        } catch (Exception e) { System.out.println("Aset gambar tidak lengkap!"); }

        setBackground(new Color(24, 28, 45));
        setFocusable(true);
        initGrid();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int dx = 0, dy = 0;
                if (e.getKeyCode() == KeyEvent.VK_UP) dy = -1;
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) dy = 1;
                else if (e.getKeyCode() == KeyEvent.VK_LEFT) { dx = -1; playerDirection = -1; }
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT) { dx = 1; playerDirection = 1; }
                movePlayer(dx, dy);
            }
        });
    }

    public void initGrid() {
        grid = new Node[cols][rows];
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) grid[i][j] = new Node(i, j);
        }
        MazeLogic.generatePrim(grid, cols, rows);
        startNode = grid[1][1];
        endNode = grid[cols - 2][rows - 2];
        endNode.isWall = false;
        player = startNode;
        manualPathStack.clear();
        manualPathStack.push(player);
        resetSolver();
        requestFocusInWindow();
    }

    public void resetSolver() {
        isSolving = false;
        visitedOrder.clear();
        finalPath.clear();
        for (Node[] r : grid) {
            for (Node n : r) {
                n.visited = false;
                n.parent = null;
                n.gCost = 1000000;
            }
        }
        updateCost();
        repaint();
    }

    private void updateCost() {
        int totalCost = 0;
        if (!finalPath.isEmpty()) {
            for (Node n : finalPath) if (n != startNode) totalCost += n.weight;
        } else {
            for (int i = 1; i < manualPathStack.size(); i++) totalCost += manualPathStack.get(i).weight;
        }
        costDisplay.setText("Total Cost: " + totalCost);
    }

    private void movePlayer(int dx, int dy) {
        if (dx == 0 && dy == 0) return;
        int nx = player.x + dx;
        int ny = player.y + dy;

        if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && !grid[nx][ny].isWall) {
            Node nextNode = grid[nx][ny];
            if (manualPathStack.size() > 1 && nextNode == manualPathStack.get(manualPathStack.size() - 2)) {
                manualPathStack.pop();
            } else {
                manualPathStack.push(nextNode);
            }
            player = nextNode;
            finalPath.clear();
            isSolving = false;
            updateCost();
            repaint();

            if (player == endNode) triggerWin();
        }
    }

    private void triggerWin() {
        Main.playWinSound(); // Suara berhenti otomatis dalam 3 detik (diatur di Main.java)
        if (gameMode.equals("Rank Mode")) Main.missionCompleteInRank();

        javax.swing.Timer popupDelay = new javax.swing.Timer(300, e -> {
            JOptionPane.showMessageDialog(this, "Mission Accomplished, Officer " + officerName + "!");
        });
        popupDelay.setRepeats(false);
        popupDelay.start();
    }

    public void solve(String mode) {
        resetSolver();
        isSolving = true;
        String desc = "";

        if (mode.equals("BFS")) { runBFS(startNode); desc = "BFS (Breadth-First Search) adalah algoritma pencarian pada graf atau pohon yang menjelajahi level per level, dimulai dari simpul awal, mengunjungi semua tetangga terdekatnya terlebih dahulu sebelum pindah ke level berikutnya,"; }
        else if (mode.equals("DFS")) { runDFS(startNode); desc = "Depth-First Search (DFS) adalah algoritma penelusuran atau pencarian pada struktur data pohon (tree) atau graf yang memprioritaskan kedalaman, yaitu menjelajahi sejauh mungkin ke satu cabang sebelum melakukan backtracking (mundur)"; }
        else if (mode.equals("DIJKSTRA")) { runWeightedSearch(startNode, true); desc = "Algoritma Dijkstra adalah metode untuk menemukan jalur terpendek (shortest path) dari satu simpul (node) ke semua simpul lain dalam sebuah graf berbobot (weighted graph)"; }
        else if (mode.equals("A*")) { runWeightedSearch(startNode, false); desc = "Algoritmo A* (A-Star) adalah algoritma pencarian graf yang efisien untuk menemukan jalur terpendek dari titik awal ke tujuan, sering digunakan dalam robotika dan navigasi"; }

        if (explanationArea != null) explanationArea.setText("\n" + desc);
        updateCost();
        repaint();

        if (!finalPath.isEmpty()) triggerWin();
    }

    private void runWeightedSearch(Node start, boolean isDijkstra) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        start.gCost = 0;
        pq.add(start);

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            if (curr.visited) continue;
            curr.visited = true;
            visitedOrder.add(curr);
            if (curr == endNode) break;

            for (Node m : MazeLogic.getPassageNeighbors(curr, grid, cols, rows)) {
                int h = isDijkstra ? 0 : (Math.abs(m.x - endNode.x) + Math.abs(m.y - endNode.y));
                int tentativeG = curr.gCost + m.weight + 1;
                if (tentativeG < m.gCost) {
                    m.gCost = tentativeG;
                    m.parent = curr;
                    // Note: gCost di Node digunakan PriorityQueue untuk sorting
                    pq.add(m);
                }
            }
        }
        buildPath();
    }

    private void runBFS(Node start) {
        Queue<Node> q = new LinkedList<>();
        q.add(start); start.visited = true;
        while (!q.isEmpty()) {
            Node curr = q.poll(); visitedOrder.add(curr);
            if (curr == endNode) break;
            for (Node m : MazeLogic.getPassageNeighbors(curr, grid, cols, rows)) {
                if (!m.visited) { m.visited = true; m.parent = curr; q.add(m); }
            }
        }
        buildPath();
    }

    private void runDFS(Node start) {
        Stack<Node> s = new Stack<>();
        s.push(start);
        while (!s.isEmpty()) {
            Node curr = s.pop();
            if (curr.visited) continue;
            curr.visited = true; visitedOrder.add(curr);
            if (curr == endNode) break;
            for (Node m : MazeLogic.getPassageNeighbors(curr, grid, cols, rows)) {
                if (!m.visited) { m.parent = curr; s.push(m); }
            }
        }
        buildPath();
    }

    private void buildPath() {
        Node t = endNode;
        while (t != null && t != startNode) {
            finalPath.add(t);
            t = t.parent;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int tileSize = Math.min(getWidth() / cols, getHeight() / rows);
        int offsetX = (getWidth() - (cols * tileSize)) / 2;
        int offsetY = (getHeight() - (rows * tileSize)) / 2;

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                Node n = grid[i][j];
                int x = offsetX + (i * tileSize), y = offsetY + (j * tileSize);

                if (n.isWall) g2.setColor(Node.COLOR_WALL);
                else {
                    if (n.type.equals("Water")) g2.setColor(Node.COLOR_WATER);
                    else if (n.type.equals("Mud")) g2.setColor(Node.COLOR_MUD);
                    else if (n.type.equals("Grass")) g2.setColor(Node.COLOR_GRASS);
                    else g2.setColor(Node.COLOR_PATH);
                }

                if (finalPath.contains(n)) g2.setColor(new Color(241, 196, 15));
                g2.fillRect(x, y, tileSize, tileSize);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.drawRect(x, y, tileSize, tileSize);

                if (n == startNode && entryIcon != null) g2.drawImage(entryIcon, x, y, tileSize, tileSize, this);
                if (n == player && playerIcon != null) {
                    if (playerDirection == 1) g2.drawImage(playerIcon, x, y, tileSize, tileSize, this);
                    else g2.drawImage(playerIcon, x + tileSize, y, -tileSize, tileSize, this);
                } else if (n == endNode && exitIcon != null) g2.drawImage(exitIcon, x, y, tileSize, tileSize, this);
            }
        }
    }
}