package tetris.play;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Path;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import tetris.config.IMinoConfig;
import tetris.config.IMinoImage;
import tetris.config.IPlayConfig;
import tetris.config.impl.NormalMinoConfig;
import tetris.config.impl.SegaMinoImage;
import tetris.data.MinoSprite;
import tetris.data.ScreenData;

import java.awt.*;
import java.awt.Font;
import java.util.ArrayList;

/**
 * Created by hkoba on 2016/08/30.
 */
public class TetrisPlay extends BasicGameState {
    private IPlayConfig playConfig;
    private IMinoConfig minoConfig;
    private IMinoImage minoImage;

    /**
     * 次のテトリミノまでのX座標
     */
    private static final int NEXT_POSITION_X = 72;

    /**
     * 次のテトリミノ（小）のX座標
     */
    private static final int NEXT_POSITION_X2 = 40;

    private enum PlayMode {
        WAIT_ARE,
        MOVE_MINO,
        LINE_DELETE,
        GAME_OVER
    }

    /**
     * areやline_delete
     */
    private int waitNum;

    private int dasNum;
    private int lockNum;

    private ScreenData screenData;
    private TetriminoData tetriminoData;

    private MinoSprite holdMino;

    private ArrayList<MinoSprite> waitList;

    private Image screenImage;
    private Image bgImage;

    private PlayMode playMode;
    private int curLevel;

    private boolean holdFlag;

    private int nextMoveX;

    private TrueTypeFont typeFont;

    public TetrisPlay(IPlayConfig config) {
        playConfig = config;
        screenData = new ScreenData();
        waitList = new ArrayList<>();
        // dummy
        minoConfig = new NormalMinoConfig();
        minoImage = new SegaMinoImage();
        try {
            screenImage = new Image(16 * 10, 16 * 20);
            bgImage = new Image(16 * 12, 16 * 28);
            Graphics g = bgImage.getGraphics();
            g.setColor(Color.lightGray);
            g.fillRect(0, 5 * 16, 12 * 16, 22 * 16);
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
            typeFont = new TrueTypeFont(new Font("gothic", Font.BOLD, 14), true, "NX".toCharArray());
            g.setFont(typeFont);
            g.setColor(new Color(240, 255, 192));
            g.setColor(Color.green);
            g.drawString("HOLD", 0, 16);
            g.drawString("NEXT", 73, 16);

            g.setColor(Color.transparent);
            g.setDrawMode(Graphics.MODE_ALPHA_MAP);
            g.fillRect(1 * 16, 6 * 16, 10 * 16, 20 * 16);
            g.flush();
        } catch (SlickException e) {
            e.printStackTrace();
        }
        curLevel = playConfig.initLevel();
        stockNextMino();
    }

    private void stockNextMino() {
        char ch = playConfig.getNextMino();
        waitList.add(new MinoSprite(ch, minoConfig.getMinoData(ch), minoImage.getMinoImage(ch)));
        if (waitList.size() < 4) {
            stockNextMino();
        }
    }

    private void nextTetrimino() {
        MinoSprite mino = waitList.remove(0);
        char ch = mino.getMinoCode();
        tetriminoData = new TetriminoData(mino,
                playConfig.getDas(curLevel),
                playConfig.getLock(curLevel),
                playConfig.getGravity(curLevel),
                ch | (playConfig.getHideTurn(curLevel) << 16));
        stockNextMino();
        holdFlag = false;
        nextMoveX = NEXT_POSITION_X;
        // ゲームオーバーのチェック
        if (tetriminoData.isGameOver(screenData)) {
            playMode = PlayMode.GAME_OVER;
        } else {
            playMode = PlayMode.MOVE_MINO;
        }
    }

    private void holdCheck() {
        if (holdFlag) {
            return;
        }
        if (!KeyAction.BUTTON_HOLD.isPush()) {
            return;
        }
        if (holdMino == null) {
            holdMino = waitList.remove(0);
            stockNextMino();
        }
        holdFlag = true;
        holdMino = tetriminoData.changeHold(holdMino);
    }

    public void nextFrame() {
        if (nextMoveX > 0) {
            nextMoveX -= 8;
        }
        if (playMode == PlayMode.LINE_DELETE) {
            waitNum--;
            if (waitNum <= 0) {
                // AREへ以降
                screenData.dropLines();
                waitNum = playConfig.getLineAre(curLevel);
                playMode = PlayMode.WAIT_ARE;
            }
            return;
        } else if (playMode == PlayMode.WAIT_ARE) {
            waitNum--;
            if (waitNum <= 0) {
                waitNum = 0;
                nextTetrimino();
                holdCheck();
            }
            return;
        } else if (playMode == PlayMode.GAME_OVER) {
            // ゲームオーバー
            return;
        }
        // MOVE_MINO
        if (tetriminoData == null) {
            int lines = screenData.deleteLine();
            if (lines > 0) {
                // 特殊な処理
                curLevel = playConfig.getNextLevel(curLevel, lines);
                waitNum = playConfig.getLineClear(curLevel);
                playMode = PlayMode.LINE_DELETE;
            } else {
                curLevel = playConfig.getNextLevel(curLevel, 0);
                waitNum = playConfig.getAre(curLevel);
                playMode = PlayMode.WAIT_ARE;
            }
            return;
        } else {
            // HOLDチェック
            holdCheck();
            if (!tetriminoData.nextFrame(screenData)) {
                tetriminoData = null;
            }
        }
    }

    public void draw(Graphics g) {
        try {
            screenData.drawScreen(screenImage.getGraphics(), minoImage);
        } catch (SlickException e) {
            e.printStackTrace();
        }
        int xx = 16 * 4 + nextMoveX;
        int yy = 44;
        float scale = 1.0f;
        if (nextMoveX > 0) {
            scale -= nextMoveX * 0.5f / 80;
            yy += nextMoveX * 16 / NEXT_POSITION_X;
        }
        for (MinoSprite mino : waitList) {
            mino.drawMino(g, scale, xx, yy);
            xx += NEXT_POSITION_X2;
            if (xx < 64 + NEXT_POSITION_X) {
                xx = 64 + NEXT_POSITION_X;
            }
            scale = 0.5f;
            yy = 52;
        }
        if (holdMino != null) {
            holdMino.drawMino(g, 0.5f, 16, 48);
        }
        g.translate(16, 6 * 16);
        g.drawImage(screenImage, 0, 0);
        screenData.drawFadeItem(g);
        if (tetriminoData != null) {
            if (playConfig.isGhost(curLevel)) {
                tetriminoData.getGhost(screenData).drawMino(g);
            }
            tetriminoData.draw(g);
        }
        g.translate(-16, -6 * 16);
        g.drawImage(bgImage, 0, 0);
        g.setFont(typeFont);
        g.setColor(Color.green);
        g.drawString(String.valueOf(curLevel), 200, 100);
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        for (KeyAction action: KeyAction.values()) {
            action.resetCount();
        }
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        draw(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        for (KeyAction action: KeyAction.values()) {
            action.nextFrame();
        }
        nextFrame();
    }
}
