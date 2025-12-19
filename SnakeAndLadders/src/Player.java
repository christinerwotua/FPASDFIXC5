import java.awt.Color;
import java.awt.Image;

public class Player {
    private final String name;
    private Node currentPosition;
    private final Color color;
    private final boolean bot;

    private int score = 0;

    // dipakai untuk prime-start shortcut rule
    private int lastPositionId = 1;

    // === NEW: icon karakter ===
    private Image icon;


    public Player(String name, Node startNode, Color color, boolean bot) {
        this.name = name;
        this.currentPosition = startNode;
        this.color = color;
        this.bot = bot;
    }

    public String getName() { return name; }
    public Node getCurrentPosition() { return currentPosition; }
    public void setPosition(Node n) { this.currentPosition = n; }
    public Color getColor() { return color; }
    public boolean isBot() { return bot; }

    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }

    public void stepForward() {
        if (currentPosition != null && currentPosition.next != null) {
            currentPosition = currentPosition.next;
        }
    }

    public int getLastPositionId() { return lastPositionId; }
    public void setLastPositionId(int id) { this.lastPositionId = id; }

    // === NEW: icon getter/setter ===
    public Image getIcon() { return icon; }
    public void setIcon(Image icon) { this.icon = icon; }
}
