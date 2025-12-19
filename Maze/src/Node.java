import java.awt.Color;

public class Node implements Comparable<Node> {
    public int x, y, weight;
    public String type;
    public boolean isWall = true;
    public boolean visited = false;
    public Node parent;

    // Var pembantu utk si dijkstra
    public int gCost = 1000000;

    public static final Color COLOR_WALL = new Color(34, 47, 91);
    public static final Color COLOR_PATH = new Color(236, 230, 240);
    public static final Color COLOR_GRASS = new Color(110, 137, 86);
    public static final Color COLOR_MUD = new Color(135, 54, 30);
    public static final Color COLOR_WATER = new Color(110, 143, 222);

    public Node(int x, int y) {
        this.x = x; this.y = y;
        double rand = Math.random();
        if (rand < 0.05) { type = "Water"; weight = 10; }
        else if (rand < 0.1) { type = "Mud"; weight = 5; }
        else if (rand < 0.2) { type = "Grass"; weight = 1; }
        else { type = "Terrace"; weight = 0; }
    }


    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.gCost, other.gCost);
    }
}