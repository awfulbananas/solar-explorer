import fromics.Point;

public class SpaceBody extends Point {
    private Point spd;
    private Point prevAccel;
    private double mass;
    private double rad;

    public SpaceBody(double x, double y, Point spd, Point accel, double mass, double rad) {
        super(x, y);
        this.spd = spd;
        this.prevAccel = accel;
        this.mass = mass;
        this.rad = rad;
    }

    public void update(Point accel, double dt) {
//        System.out.println("moving " + this);
//        System.out.println("accel: " + accel);
//        add(dt * (spd.X() + 0.5 * prevAccel.X() * dt), dt * (spd.Y() + 0.5 * prevAccel.Y() * dt));
//        spd.add(0.5 * dt * (prevAccel.X() + accel.X()), 0.5 * dt * (prevAccel.Y() + accel.Y()));
//        prevAccel = accel;
        add(dt * spd.X(), dt * spd.Y());
        spd.add(dt * accel.X(), dt * accel.Y());
    }

    public double getRad() {
        return rad;
    }

    public double getMass() {
        return mass;
    }
}
