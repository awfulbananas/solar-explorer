import fromics.Background;
import fromics.Frindow;
import fromics.Manager;

import java.awt.image.BufferedImage;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Game extends Manager {
    private Frindow win;

    public static void main(String[] args) {
        Game app = new Game(new Frindow(BufferedImage.TYPE_INT_RGB, 900, 700, "Solar"),20, 20);
        app.startVariableLoop();
    }

    public Game(Frindow observer, int drawDelayMillis, int updateDelayMillis) {
        super(observer, drawDelayMillis, updateDelayMillis);
        win = observer;
        screens = new Background[1];
        win.init(3, this);
        initScreen(0);
    }

    @Override
    protected void initScreen(int n) {
        switch(n) {
            case 0:
                screens[0] = new SpaceScreen(win);
                link(screens[0]);
        }
    }
}