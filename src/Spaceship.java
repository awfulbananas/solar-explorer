import fromics.Linkable;
import fromics.Point;

import java.awt.event.KeyEvent;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Spaceship extends Linkable {
    public static final double THRUST = 0.000000000002;
    public static final double ROT_THRUST = 0.000000000001;
    public static final double ROT_BRAKES = 1;
    public static final Point[] DRAW_LOCS = {new Point(1.06066, 0), new Point(-0.43934, 0.5), new Point(-0.43934, -0.5)};
    private static final double DRAW_SIZE = 10;
    private static final int POINT_FREQ = 100000;
    private static final double PATH_SEG_LENGTH_MARGIN = 1;
    private static final int MAX_PATH_SEGS = 1000000;

    private final FollowCamera cam;
    private Point prevSpd;
    private final Point spd;
    private double rotSpd;
    private List<Point> locs;
    private int ppCooldown;
    private boolean showPath;
    private Point gravAccel;

    public Spaceship(double x, double y, FollowCamera cam) {
        super(x, y);
        this.cam = cam;
        showPath = true;
        spd = new Point();
        ang = Math.PI/2;
        locs = new ArrayList<>();
        locs.add(this.copy());
        locs.add(this);
        gravAccel = new Point();
        ppCooldown = POINT_FREQ;
    }

    public Point getSpd(){
        return spd;
    }

    public void updateGravAccel(Point gravAccel) {
        this.gravAccel = gravAccel;
    }

    @Override
    public boolean update() {
     //   System.out.println("update ship");
        prevSpd = spd.copy();
        if(getKey(KeyEvent.VK_LEFT)) {
            rotSpd += ROT_THRUST * dt();
        }
        if(getKey(KeyEvent.VK_RIGHT)) {
            rotSpd -= ROT_THRUST * dt();
        }
        if(getKey(KeyEvent.VK_UP)) {
            spd.add(Math.cos(getAng()) * THRUST * dt(), -Math.sin(getAng()) * THRUST * dt());
        }

        spd.add(gravAccel.copy().mult(dt()));

        setAng(getAng() + rotSpd * dt());
        add(spd.X()* dt(), spd.Y() * dt());
        rotSpd *= ROT_BRAKES;


        ppCooldown -= dt();
        if(ppCooldown <= 0) {
            Point toAdd = this.copy();
            if(toAdd.copy().sub(locs.get(locs.size() - 2)).sMag() > PATH_SEG_LENGTH_MARGIN) {
                locs.add(locs.size() - 1, this.copy());
                if(locs.size() > MAX_PATH_SEGS) locs.removeFirst();
            }
            ppCooldown = POINT_FREQ;
        }
        return false;
    }

    public Point getAccel() {
        return spd.copy().sub(prevSpd);
    }

    @Override
    public void onFirstLink() {
        addKeystrokeFunction((KeyEvent e) -> {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_Q:
                    spd.setX(0);
                    spd.setY(0);
                case KeyEvent.VK_P:
                    showPath = !showPath;
            }
        });
    }

    @Override
    protected void draw(Graphics g, double xOff, double yOff, double angOff) {
        Point scale = cam.getScale();
        Point halfScreen = (new Point(getScreenWidth(), getScreenHeight())).div(2);
        double rot = getAng();

        Point[] newPoints = new Point[DRAW_LOCS.length];
        for(int i = 0; i < DRAW_LOCS.length; i++) {
            newPoints[i] = cam.transformPoint(DRAW_LOCS[i].copy().rot(rot).mult(DRAW_SIZE).add(this)).mult(scale.X()).rot(cam.getAng()).add(halfScreen);
        }

        simpleDrawPoints(g, newPoints);

        if(!showPath) return;

        Point[] path = new Point[locs.size()];

        for(int i = 0; i < path.length; i++) {
            Point pt = locs.get(i);
            path[i] = cam.transformPoint(pt.copy()).mult(scale.X()).rot(cam.getAng()).add(halfScreen);
        }

        simpleDrawPoints(g, path, false);
    }
}
