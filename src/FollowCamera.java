import java.awt.Graphics;
import java.awt.event.KeyEvent;

import fromics.Linkable;
import fromics.Point;

public class FollowCamera extends Linkable{
    private static final double maxScaleSpeed = 0.000002;
    private static final double scaleAccel = 0.02;
    private static final double scaleBraking = 0.7;
    private static final double accelOffsetFactor = 100000000;


    private double scaleSpeed;
    private Point accelOffsetScale;
    private Linkable target;

    public FollowCamera() {
        super(0, 0);
        scale =new Point(0.1, 0.1);
        accelOffsetScale = new Point();
        scaleSpeed = 0;
    }

    public void target(Linkable target) {
        this.target = target;
    }



    public boolean update() {

        boolean scaling = false;
        if(getKey(KeyEvent.VK_PAGE_UP)) {
            scaling = true;
            if(Math.abs(scaleSpeed) < maxScaleSpeed) {
                scaleSpeed += (maxScaleSpeed - scaleSpeed) * scaleAccel;
            }
        }
        if(getKey(KeyEvent.VK_PAGE_DOWN)) {
            scaling = true;
            if(Math.abs(scaleSpeed) < maxScaleSpeed) {
                scaleSpeed += ((-maxScaleSpeed) - scaleSpeed) * scaleAccel;
            }
        }
        if(!scaling) {
            scaleSpeed *= scaleBraking;
        }
        scale.mult(1 + scaleSpeed * dt());

        if(target == null) return false;
        Point locDiff = this.copy().sub(target);
        setX(target.X());
        setY(target.Y());
//        Point targetAccel = ((Spaceship)target).getAccel();
//        Point offDiff = targetAccel.copy().mult(accelOffsetFactor).sub(accelOffsetScale).div(accelOffsetFactor);
//        accelOffsetScale.add(offDiff.mult(0.01 * accelOffsetFactor));
//        sub(accelOffsetScale);
        setAng(-target.getAng() + Math.PI/2);

        return false;
    }

    public Point transformPoint(Point p) {
        return p.sub(this);
    }

    @Override
    protected void draw(Graphics g, double xOff, double yOff, double angOff) {}

}
