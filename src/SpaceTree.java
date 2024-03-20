import fromics.Linkable;
import fromics.Point;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class SpaceTree extends Linkable {
    public static final double GRAV_MARGIN_SQRD = 0.1;
    public static final SpaceBody[] INIT_BODIES = {
            new SpaceBody(0, 0, new Point(0, 0), new Point(0, 0), 100, 200),
            new SpaceBody(1000, 0, new Point(0, -0.00015), new Point(0, 0), 100, 200)
    };
    public static final double G_CONST = 0.0000001;

    private SpaceNode root;
    private final FollowCamera cam;
    private int gravCooldown = 0;

    public SpaceTree(FollowCamera cam) {
        super(0, 0);
        this.cam = cam;
        root = new SpaceNode();
        for (int i = 0; i < INIT_BODIES.length; i++) {
            SpaceBody initBody = INIT_BODIES[i];
            addBody(initBody);
        }
    }

    private void expand(SpaceBody b) {
        int prevRootQuad = b.X() < root.min.X() ? 1 : 0;
        prevRootQuad += b.Y() < root.min.Y() ? 2 : 0;
        SpaceNode prevRoot = root;
        Point[] mins = root.calcOuterMins();
        root = new SpaceNode(prevRoot.depth - 1, mins[prevRootQuad], prevRoot.objs, null);
        root.children = new SpaceNode[4];
        root.children[prevRootQuad] = prevRoot;
        Point[] newNestedMins = root.calcNestedMins();
        for(int i = 0; i < 4; i++) {
            if(i == prevRootQuad) continue;
            root.children[i] = new SpaceNode(prevRoot.depth, newNestedMins[i], root.objs, root);
        }
        prevRoot.parent = root;
        addBody(b);
    }

    private void addBody(SpaceBody b) {
        if(b.isWithinBounds(root.min, root.max)) {
            root.giveBody(b);
        } else {
            expand(b);
        }
    }

    private void printBodies() {
        if(!root.objs.isEmpty()) System.out.println("bodies: " + root.objs);
    }

    @Override
    public boolean update() {
        root.updateObjs();
        root.updateTree();
        if(root.children != null) {
            pruneTree();
        }
        root.updateGravity();
        if(gravCooldown == 0) {
            moveBodies();
        } else {
            gravCooldown--;
        }

        return false;
    }

    private void pruneTree() {
        return; //TODO: make this work later
        /*int emptyRootQuads = 0;
        for (int i = 0; i < 4; i++) {
            emptyRootQuads += root.children[i].objs.isEmpty() ? 1 : 0;
        }
        if (emptyRootQuads == 3) {
            loop:
            for (int i = 0; i < 4; i++) {
                if (!root.children[i].objs.isEmpty()) {
                    root = root.children[i];
                    break loop;
                }
            }
        }*/
    }

    private void moveBodies() {
        for(SpaceBody b : root.objs) {
            b.update(calcApproxGravFromPoint(b), dt());
        }
    }

    public Point calcTrueGravFromPoint(Point b) {
        Point accel = new Point();
        for(SpaceBody ot : root.objs) {
            Point diff = ot.copy().sub(b);
            double distSqrd = diff.sMag();
            if(distSqrd > 0) {
                if(distSqrd < ot.getRad() * ot.getRad()) distSqrd = Math.pow(2 * ot.getRad() - diff.mag(), 2);
                accel.add(diff.normalize().mult(G_CONST).div(distSqrd).mult(ot.getMass()));
            }
        }
        return accel;
    }

    private Point calcApproxGravFromPoint(Point b) {
        return calcApproxGravFromPoint(b, root);
    }

    private Point calcApproxGravFromPoint(Point b, SpaceNode curr) {
        if(curr.totalMass == 0) return new Point();
        Point diff = curr.centerOfMass.copy().sub(b);
        double distSqrd = diff.sMag();
        Point accel;
        if(curr.children == null) {
            SpaceBody obj = curr.objs.getFirst();
            //TODO: I could make a small optimization by not normalizing since diff is proportional to r
            if(distSqrd > 0) {
                accel = diff.normalize().mult(G_CONST).div(distSqrd).mult(obj.getMass());
            } else {
                accel = new Point();
            }
        } else {
            double distRatio = Math.pow(curr.max.X() - curr.min.X(), 2) / distSqrd;
            if(distRatio < GRAV_MARGIN_SQRD) {
                accel = diff.normalize().mult(G_CONST).div(distSqrd).mult(curr.getMass());
            } else {
                accel = new Point();
                for (SpaceNode n : curr.children) {
                    accel.add(calcApproxGravFromPoint(b, n));
                }
            }
        }
        return accel;
    }

    private class SpaceNode {
        public static final int SPLIT_MARGIN = 1;
        private static final int MERGE_MARGIN = 2;

        private final Point min;
        private final Point max;
        private SpaceNode parent;

        private SpaceNode[] children;
        private final List<SpaceBody> objs;

        int depth;

        private Point centerOfMass;
        private double totalMass;

        public SpaceNode() {
            this(-7, new Point(1, 1), new ArrayList<>(), null);
        }

        public SpaceNode(int depth, Point min, List<SpaceBody> toAdd, SpaceNode parent) {
            this.min = min;
            this.parent = parent;
            this.max = min.copy().add(Math.pow(2, -depth),Math.pow(2, -depth));
            this.depth = depth;
            totalMass = 0;
            centerOfMass = new Point();
            objs = new ArrayList<>();
            for(SpaceBody b : toAdd) {
                if(b.isWithinBounds(min, max)) {
                    objs.add(b);
                    totalMass += b.getMass();
                    centerOfMass.add(b.copy().mult(b.getMass()));
                }
            }
            if(totalMass != 0) centerOfMass.div(totalMass);
        }

        public void updateTree() {
            if(children == null) {
                if(objs.size() > SPLIT_MARGIN) {
                    children = new SpaceNode[4];
                    Point[] mins = calcNestedMins();
                    for(int i = 0; i < 4; i++) {
                        children[i] = new SpaceNode(depth + 1, mins[i], objs, this);
                    }
                }
            } else if(objs.size() < MERGE_MARGIN) {
                children = null;
            }

            if(children != null) for(SpaceNode n : children) n.updateTree();

        }

        public double getMass() {
            return totalMass;
        }

        private void updateObjs() {
            if(children == null) {
                int curLength = objs.size();
                int loops = 0;
                for(int i = 0; i < curLength; i++) {
                    loops++;
                    SpaceBody b = objs.get(i);
                    if(!b.isWithinBounds(min, max)) {
                        objs.remove(i);
                        i--;
                        curLength--;
                        totalMass -= b.getMass();
                        if(parent == null) {
                            expand(b);
                        } else {
                            parent.passBody(b);
                        }
                    }
                }
                return;
            }
            for(SpaceNode n : children) n.updateObjs();
        }

        private void passBody(SpaceBody body) {
            if(body.isWithinBounds(min, max)) {
                indexBody(body);
            } else {
                objs.remove(body);
                totalMass -= body.getMass();
                if(parent != null) {
                    parent.passBody(body);
                } else {
                    expand(body);
                }

            }
        }

        private void giveBody(SpaceBody body) {
            objs.add(body);
            totalMass += body.getMass();
            if(children != null) {
                indexBody(body);
            }
        }

        private void indexBody(SpaceBody body) {
            int quad = body.X() < children[0].max.X() ? 0 : 1;
            quad += body.Y() < children[0].max.Y() ? 0 : 2;
            children[quad].giveBody(body);
        }

        private void updateGravity() {
            if(totalMass == 0) return;

            if(children == null) {
                if(objs.size() == 1) {
                    centerOfMass = objs.getFirst().copy();
                } else {
                    centerOfMass = new Point();
                    for(SpaceBody b : objs) centerOfMass.add(b.copy().mult(b.getMass()));
                    centerOfMass.div(totalMass);
                }

            } else {
                centerOfMass = new Point();
                for(SpaceNode n : children) {
                    n.updateGravity();
                    centerOfMass.add(n.centerOfMass.copy().mult(n.totalMass));
                }
                centerOfMass.div(totalMass);
            }

        }

        private Point[] calcNestedMins() {
            Point[] mins = new Point[4];
            mins[0] = min.copy();
            mins[1] = min.copy().add(Math.pow(2, -(depth + 1)), 0);
            mins[2] = min.copy().add(0, Math.pow(2, -(depth + 1)));
            mins[3] = min.copy().add(Math.pow(2, -(depth + 1)), Math.pow(2, -(depth + 1)));
            return mins;
        }

        private Point[] calcOuterMins() {
            Point[] mins = new Point[4];
            mins[0] = min.copy();
            mins[1] = min.copy().sub(Math.pow(2, -depth), 0);
            mins[2] = min.copy().sub(0, Math.pow(2, -depth));
            mins[3] = min.copy().sub(Math.pow(2, -depth), Math.pow(2, -depth));
            return mins;
        }

        public void draw(Graphics g) {
            Point scale = cam.getScale();
            Point halfScreen = (new Point(getScreenWidth(), getScreenHeight())).div(2.0);
            Point upperLeft = min.copy();
            Point lowerRight = max.copy();
            Point upperRight = new Point(min.X(), max.Y());
            Point lowerLeft = new Point(max.X(), min.Y());
            Point[] corners = new Point[4];
            corners[0] = upperLeft;
            corners[1] = upperRight;
            corners[2] = lowerRight;
            corners[3] = lowerLeft;
            for(Point pt : corners) {
                cam.transformPoint(pt.add(X(), Y())).mult(scale.X()).rot(cam.getAng()).add(halfScreen);
            }
            Linkable.simpleDrawPoints(g, corners);
            if(children != null) for(SpaceNode n : children) n.draw(g);
            if(parent != null) return;
            for(SpaceBody b : objs) {
                Point bLoc = cam.transformPoint(b.copy()).mult(scale.X()).rot(cam.getAng()).add(halfScreen);
                int bSize = (int)(b.getRad() * scale.X());
                g.drawOval((int)bLoc.X() - bSize / 2, (int)bLoc.Y() - bSize / 2, bSize, bSize);
            }
        }
    }

    @Override
    protected void draw(Graphics g, double xOff, double yOff, double angOff) {
        root.draw(g);
    }


}
