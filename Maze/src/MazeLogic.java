import java.util.*;

public class MazeLogic {
    // prim algorithm code
    public static void generatePrim(Node[][] grid, int cols, int rows) {
        List<Node> walls = new ArrayList<>();
        Node start = grid[1][1];
        start.isWall = false;
        addNearbyWalls(start, grid, walls, cols, rows);

        while (!walls.isEmpty()) {
            Node wall = walls.remove((int) (Math.random() * walls.size()));
            List<Node> neighbors = getPassageNeighbors(wall, grid, cols, rows);
            if (neighbors.size() == 1) {
                wall.isWall = false;
                addNearbyWalls(wall, grid, walls, cols, rows);
            } else if (Math.random() < 0.08) {
                wall.isWall = false;
            }
        }
    }

    private static void addNearbyWalls(Node n, Node[][] grid, List<Node> walls, int cols, int rows) {
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        for (int i = 0; i < 4; i++) {
            int nx = n.x + dx[i], ny = n.y + dy[i];
            if (nx > 0 && nx < cols - 1 && ny > 0 && ny < rows - 1 && grid[nx][ny].isWall) {
                walls.add(grid[nx][ny]);
            }
        }
    }

    public static List<Node> getPassageNeighbors(Node n, Node[][] grid, int cols, int rows) {
        List<Node> res = new ArrayList<>();
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        for (int i = 0; i < 4; i++) {
            int nx = n.x + dx[i], ny = n.y + dy[i];
            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && !grid[nx][ny].isWall) {
                res.add(grid[nx][ny]);
            }
        }
        return res;
    }
}