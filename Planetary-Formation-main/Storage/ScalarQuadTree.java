package Storage;

import Storage.Positionals.Vector2;
import Storage.Positionals.Vector3;

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
    protected ScalarQuadTree relevantChild(Vector2 point) {
        // children[0][.] if x < 0.5, children[1][.] otherwise. Same for y.
        return this.children[(point.getX() < 0.5) ? 0 : 1][(point.getY() < 0.5) ? 0 : 1];
    }

    protected ScalarQuadTree getLeafNode(Vector2 point) {
        if (this.leafNode)
            return this;

        Vector2 leafPoint = point.clone();
        leafPoint.mutateWithLambda((x) -> (2 * x % 1));

        return relevantChild(point).getLeafNode(point);
    }

    public double getPoint(Vector2 point) {
        ScalarQuadTree leafNode = getLeafNode(point);

        return leafNode.data.getPoint(point);
    }

    @FunctionalInterface
    public interface LocalMutator {
        double mutate(Vector2 point, double value);
    }

    public void mutateLocal(LocalMutator operation, double size) {
        if (leafNode) {
            //
        }
    }
}
