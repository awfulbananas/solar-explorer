import fromics.Background;
import fromics.Frindow;

import java.awt.*;

public class SpaceScreen extends Background {

    FollowCamera cam;
    Spaceship ship;
    SpaceTree space;

    public SpaceScreen(Frindow observer) {
        super(observer);
        cam = new FollowCamera();
        ship = new Spaceship(0, 0, cam);
        cam.target(ship);
        space = new SpaceTree(cam);
        link(space);
        link(ship);
        link(cam);
    }

    @Override
    public boolean update() {
        ship.updateGravAccel(space.calcTrueGravFromPoint(ship));
        return false;
    }

    @Override
    protected void draw(Graphics g, double xOff, double yOff, double angOff) {

    }
}
