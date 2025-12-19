import java.util.*;

public class Board {
    private final Map<Integer, Node> nodes = new HashMap<>();
    private final Map<Integer, Integer> points = new HashMap<>();
    private final Random rand = new Random();

    public Board() {
        for (int i = 1; i <= 64; i++) nodes.put(i, new Node(i));
        for (int i = 1; i <= 63; i++) nodes.get(i).next = nodes.get(i + 1);

        generateShortcuts();
        generatePoints();
    }

    public Node getStartNode() { return nodes.get(1); }
    public Node getNodeById(int id) { return nodes.get(id); }

    public int getPointsAt(int nodeId) { return points.getOrDefault(nodeId, 0); }
    public void removePoints(int nodeId) { points.remove(nodeId); }

    public boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        int lim = (int)Math.sqrt(n);
        for (int i = 3; i <= lim; i += 2) if (n % i == 0) return false;
        return true;
    }

    private void generateShortcuts() {
        // reset
        for (int i = 1; i <= 64; i++) nodes.get(i).shortcut = null;

        int count = 7;
        Set<Integer> usedStart = new HashSet<>();
        Set<Integer> usedEnd = new HashSet<>();

        int tries = 0;
        while (count > 0 && tries < 5000) {
            tries++;

            int start = 2 + rand.nextInt(58);          // 2..59
            int end   = start + (3 + rand.nextInt(18)); // +3..+20

            if (end > 63) continue;
            if (start == 64 || end == 64) continue;
            if (usedStart.contains(start) || usedEnd.contains(end)) continue;

            Node s = nodes.get(start);
            Node e = nodes.get(end);
            if (s == null || e == null) continue;
            if (s.shortcut != null) continue;

            usedStart.add(start);
            usedEnd.add(end);
            s.shortcut = e;
            count--;
        }
    }

    private void generatePoints() {
        points.clear();

        List<Integer> cand = new ArrayList<>();
        for (int i = 2; i <= 63; i++) {
            if (nodes.get(i).shortcut == null) cand.add(i);
        }
        Collections.shuffle(cand, rand);

        int pawCount = 14;
        for (int i = 0; i < Math.min(pawCount, cand.size()); i++) {
            int id = cand.get(i);
            int val;
            double r = rand.nextDouble();
            if (r < 0.60) val = 10;
            else if (r < 0.85) val = 20;
            else val = 30;
            points.put(id, val);
        }
    }
}
