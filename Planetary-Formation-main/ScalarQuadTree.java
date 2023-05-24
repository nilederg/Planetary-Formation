// A recursive field that stores a grid of scalars
public class ScalarQuadTree {
    private final ScalarQuadTree parent;
    private ScalarQuadTree[][] children;
    private boolean leafNode;
    private PlanarScalarData data;

    // Branch/root note constructor
    ScalarQuadTree(ScalarQuadTree parent, int remainingLevels) {
        this.parent = parent;
        this.leafNode = false;
        this.children =  new ScalarQuadTree[2][2];
        if (remainingLevels > 1)
            for (int i = 0; i < 2; i ++)
                for (int j = 0; j < 2; j ++)
                    this.children[i][j] = new ScalarQuadTree(this, remainingLevels - 1);
        else // remaining levels = 1
            for (int i = 0; i < 2; i ++)
                for (int j = 0; j < 2; j ++)
                    this.children[i][j] = new ScalarQuadTree(this);
    }

    // Leaf node constructor
    // A node can still change between branch and leaf after construction
    ScalarQuadTree(ScalarQuadTree parent) {
        this.parent = parent;
        this.leafNode = true;
        this.children = null;
    }

    // Returns the child node that would contain those coordinates
    protected ScalarQuadTree relevantChild(double x, double y) {
        // children[0][.] if x < 0.5, children[1][.] otherwise. Same for y.
        return this.children[(x < 0.5) ? 0 : 1][(y < 0.5) ? 0 : 1];
    }

    protected ScalarQuadTree getLeafNode(double x, double y) {
        if (this.leafNode)
            return this;

        return relevantChild(x, y).getLeafNode(2 * x % 1, 2 * y % 1);
    }

    public double getPoint(double x, double y) {
        if (this.leafNode)
            return data.getPoint(x, y);

        return relevantChild(x, y).getPoint(2 * x % 1, 2 * y % 1);
    }


}
