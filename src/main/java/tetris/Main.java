package tetris;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.command.*;
import org.newdawn.slick.state.StateBasedGame;
import tetris.config.IMinoData;
import tetris.config.IPlayConfig;
import tetris.config.impl.NormalMinoConfig;
import tetris.config.impl.SegaMinoImage;
import tetris.config.impl.TgmPlayConfig;
import tetris.data.MinoSprite;
import tetris.data.ScreenData;
import tetris.play.KeyAction;
import tetris.play.KeyConfigPlay;
import tetris.play.TetrisPlay;

import java.awt.*;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by hkoba on 2016/08/15.
 */
public class Main extends StateBasedGame implements InputProviderListener {
    private int by = 0;
    private TrueTypeFont myFont;

    private NormalMinoConfig minoConfig = new NormalMinoConfig();
    private SegaMinoImage minoImage = new SegaMinoImage();
    private int spIndex;
    TetrisPlay tetrisPlay;

    public Main() {
        super("テトリス");
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        tetrisPlay = new TetrisPlay(new TgmPlayConfig());
        myFont = new TrueTypeFont(new Font("Dialog", Font.BOLD, 14), true, new char[]{
                '日', '本', 'よ'
        });
        this.addState(new KeyConfigPlay());
        this.addState(tetrisPlay);
        //this.enterState(1);

        /*

        InputProvider provider = new InputProvider(container.getInput());
        provider.addListener(this);

        provider.bindCommand(new ControllerDirectionControl(0, ControllerDirectionControl.LEFT), KeyAction.MOVE_LEFT);
        provider.bindCommand(new ControllerDirectionControl(0, ControllerDirectionControl.RIGHT), KeyAction.MOVE_RIGHT);
        int num = 0;
        for (KeyAction action: KeyAction.values()) {
            num++;
            provider.bindCommand(new ControllerButtonControl(0, num), action);
        }

        provider.bindCommand(new KeyControl(Input.KEY_LEFT), KeyAction.MOVE_LEFT);
        provider.bindCommand(new KeyControl(Input.KEY_RIGHT), KeyAction.MOVE_RIGHT);
        provider.bindCommand(new KeyControl(Input.KEY_UP), KeyAction.HARD_DROP);
        provider.bindCommand(new KeyControl(Input.KEY_DOWN), KeyAction.MOVE_DROP);
        provider.bindCommand(new KeyControl(Input.KEY_Z), KeyAction.TURN_LEFT);
        provider.bindCommand(new KeyControl(Input.KEY_X), KeyAction.TURN_RIGHT);
        provider.bindCommand(new KeyControl(Input.KEY_SPACE), KeyAction.BUTTON_HOLD);
        */
    }

    //@Override
    public void update2(GameContainer container, int delta) throws SlickException {
        //System.out.println("update:" + delta + " up.len=" + super.controllerUp.length);
        for (KeyAction action: KeyAction.values()) {
            action.nextFrame();
        }
        tetrisPlay.nextFrame();
    }

    //@Override
    public void render2(GameContainer container, Graphics g) throws SlickException {
        //System.out.println("render");
        g.setFont(myFont);
        g.setColor(Color.green);
        g.drawString("alpha 日本語だよ beta", 50, 300);

        tetrisPlay.draw(g);
    }

    @Override
    public void controlPressed(Command command) {
        ((KeyAction)command).event(true);
    }

    @Override
    public void controlReleased(Command command) {
        ((KeyAction)command).event(false);
        /*
        System.out.println("release:" + command);
        byte[] buf = imageBuffer.getRGBA();
        for (int i = 0; i < buf.length; i += 4) {
            buf[i] = (byte) 0;
            buf[i + 1] = (byte) 0;
            buf[i + 2] = (byte) 255;
        }
        */
    }

    public static void main(String[] args) {
        LibraryLoader.init();

        Main main = new Main();
        try {
            AppGameContainer container = new AppGameContainer(main);
            container.setDisplayMode(512, 448, false);
            container.setTargetFrameRate(60);
            container.setVSync(true);
            //container.setDefaultFont(new UnicodeFont(new Font("Dialog", Font.BOLD, 14)));
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
