package tetris.play.game;

import org.newdawn.slick.Color;
import org.newdawn.slick.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import tetris.config.impl.TgmPlayConfig;
import tetris.data.KeyInputData;
import tetris.play.ITetrisPlay;
import tetris.play.impl.ModeSelectPlay;
import tetris.play.impl.TetrisPlay;

import java.awt.Font;

/**
 * Created by hkoba on 2016/08/30.
 */
public class TetrisPlayGame extends BasicGameState {
    public static final int STATE_ID = 0;

    public static final int SCREEN_X = 16;
    public static final int SCREEN_Y = 6 * 16;

    public static final int LEVEL_X = 16 * 13;
    public static final int LEVEL_Y = 180;

    private Image bgImage;

    private TrueTypeFont typeFont;

    private ITetrisPlay[] playList = new ITetrisPlay[2];

    public TetrisPlayGame() {
        try {
            typeFont = new TrueTypeFont(new Font("gothic", Font.BOLD, 14), true, "NX".toCharArray());
            bgImage = new Image(512, 16 * 28);
            Graphics g = bgImage.getGraphics();
            for (int ix = 0; ix < 2; ix++) {
                g.setDrawMode(Graphics.MODE_NORMAL);
                g.translate(ix * 256, 0);
                g.setColor(Color.lightGray);
                g.fillRect(SCREEN_X - 16, SCREEN_Y - 16, 12 * 16, 22 * 16);
                g.setColor(Color.white);
                g.fillRect(4, 5 * 16 + 4, 12 * 16 - 8, 22 * 16 - 8);
                g.setColor(Color.lightGray);
                g.fillRect(12, 5 * 16 + 12, 12 * 16 - 24, 22 * 16 - 24);

                g.setColor(Color.lightGray);
                //g.drawLine(8, 8, 8, 80);
                Polygon polygon = new Polygon();
                polygon.addPoint(40, 32);
                polygon.addPoint(0, 32);
                polygon.addPoint(0, 71);
                polygon.addPoint(55, 71);
                polygon.addPoint(55, 7);
                polygon.addPoint(128, 7);
                polygon.addPoint(128, 39);
                polygon.addPoint(191, 39);
                polygon.addPoint(191, 32);
                polygon.addPoint(135, 32);
                polygon.addPoint(135, 0);
                polygon.addPoint(48, 0);
                polygon.addPoint(48, 64);
                polygon.addPoint(7, 64);
                polygon.addPoint(7, 39);
                polygon.addPoint(40, 39);
                g.translate(1, 1);
                g.setColor(Color.white);
                g.fill(polygon);
                g.translate(7, 7);
                for (int i = 7; i > 1; i--) {
                    g.translate(-1, -1);
                    g.setColor(new Color(255 - i * 16, 255 - i * 16, 255 - i * 16));
                    g.fill(polygon);
                }
                g.translate(-1, -1);
                g.setFont(typeFont);
                g.setColor(new Color(240, 255, 192));
                g.setColor(Color.green);
                g.drawString("HOLD", 0, 16);
                g.drawString("NEXT", 73, 16);

                g.setColor(Color.transparent);
                g.setDrawMode(Graphics.MODE_ALPHA_MAP);
                g.fillRect(SCREEN_X, SCREEN_Y, 10 * 16, 20 * 16);
            }
            g.flush();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    public void setInputData(KeyInputData[] inputData) {
        for (int i = 0; i < inputData.length; i++) {
            playList[i] = new ModeSelectPlay(inputData[i]);
        }
    }

    @Override
    public int getID() {
        return STATE_ID;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        for (int i = 0; i < playList.length; i++) {
            g.pushTransform();
            g.translate(i * 256, 0);
            playList[i].draw(g);
            g.popTransform();
        }
        g.drawImage(bgImage, 0, 0);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        ITetrisPlay[] orgList = playList.clone();
        for (int i = 0; i < playList.length; i++) {
            playList[i] = playList[i].nextFrame(orgList, i);
        }
    }
}
