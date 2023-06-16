package Storage;

import Storage.Positionals.Vector2;

// A recursive field that stores a grid of scalars
public class ScalarQuadTree {
    private final ScalarQuadTree parent;
    private ScalarQuadTree[][] children;
    private boolean leafNode;
    private PlanarScalarData data;

    // Universal node constructor
    ScalarQuadTree(ScalarQuadTree parent, int remainingLevels) {
        if (remainingLevels == 0) {
            this.parent = parent;
            this.children = null;
            this.leafNode = true;
            this.data = new PlanarScalarGrid(new double[256][256]);
            return;
        }

        this.parent = parent;
        this.children = new ScalarQuadTree[2][2];
        this.leafNode = false;
        this.data = null;
        for (int i = 0; i < 2; i ++)
            for (int j = 0; j < 2; j ++)
                this.children[i][j] = new ScalarQuadTree(this, remainingLevels - 1);
    }

    // Specialized leaf node constructor
    // A node can still change between branch and leaf after construction
    // TODO do that
    private ScalarQuadTree(ScalarQuadTree parent, PlanarScalarData data) {
        this.parent = parent;
        this.children = null;
        this.leafNode = true;
        this.data = data;
    }

    // Returns the child node that would contain those coordinates
    protected ScalarQuadTree relevantChild(Vector2 point) {
        // children[0][.] if x < 0.5, children[1][.] otherwise. Same for y.
        return this.children[(point.getX() < 0.5) ? 0 : 1][(point.getY() < 0.5) ? 0 : 1];
    }

    // Gets the tree traversing all the way down at that point until it hits a leaf node
    protected ScalarQuadTree getLeafNode(Vector2 point) {
        if (this.leafNode)
            return this;

        Vector2 leafPoint = point.clone();
        leafPoint.mutateWithLambda((x) -> (2 * x % 1));

        return relevantChild(point).getLeafNode(point);
    }

    // Gets the value at a provided point in the tree
    public double getPoint(Vector2 point) {
        ScalarQuadTree leafNode = getLeafNode(point);

        return leafNode.data.getPoint(point);
    }

    // Splits the tree into quarters, making it a branch node and creating four children nodes based on its quadrants
    private void split() {
        // You can't iterate on a boolean - sorry!
        children = new ScalarQuadTree[][] {
                new ScalarQuadTree[] {
                        new ScalarQuadTree(this, data.getQuadrant(false, false)),
                        new ScalarQuadTree(this, data.getQuadrant(false, true)),
                }, new ScalarQuadTree[] {
                        new ScalarQuadTree(this, data.getQuadrant(true, false)),
                        new ScalarQuadTree(this, data.getQuadrant(true, true)),
                }
        };
        this.data = null;
        this.leafNode = false;
    }

    // Takes in the point's position and value and outputs a new value for it to be
    @FunctionalInterface
    public interface LocalMutator {
        double mutate(Vector2 point, double value);
    }

    // Takes in the point's position and the range to check and returns if the tree is worthwhile to mutate within that range
    @FunctionalInterface
    public interface ApplicationZone {
        // Point is point on sphere surface, range is radians distance from point
        // Returns true if anything within range of point is in the "zone", false otherwise
        boolean checkWithin(Vector2 point, double range);
    }

    // Operation is applied to every point within zone
    public void mutateLocal(LocalMutator operation, ApplicationZone zone, double size, Vector2 position) {
        // Don't bother with it if not in the zone
        if (!zone.checkWithin(Vector2.sum(position, new Vector2(new double[] {size / 2, size / 2})), size)) {
            System.out.println(position.getX() + " " + position.getY() + ", " + "Skipped at " + size);
            return;
        }

        if (leafNode) {
            data.mutateLocal((Vector2 point, double value) -> {
                double x = point.getX() * size + position.getX();
                double y = point.getY() * size + position.getY();
                if (x > 1 || y > 1) throw new IllegalArgumentException("x or y greater than 1");
                return operation.mutate(new Vector2(new double[] {x, y}), value);
            });
        }
        else {
            for (int i = 0; i < 2; i ++) {
                for (int j = 0; j < 2; j ++) {
                    Vector2 offset = new Vector2(new double[] {size * i / 2, size * j / 2});
                    children[i][j].mutateLocal(operation, zone, size / 2, Vector2.sum(position, offset));
                }
            }
        }
    }
}
