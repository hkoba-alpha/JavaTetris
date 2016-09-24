package tetris.play.impl;

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
import tetris.data.KeyInputData;
import tetris.data.MinoSprite;
import tetris.data.ScreenData;
import tetris.play.ITetrisPlay;
import tetris.play.TetriminoData;

import java.awt.*;
import java.awt.Font;
import java.util.ArrayList;

/**
 * Created by hkoba on 2016/08/30.
 */
public class TetrisPlay implements ITetrisPlay {
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

    private PlayMode playMode;
    private int curLevel;

    private boolean holdFlag;

    private int nextMoveX;

    private KeyInputData inputData;

    public TetrisPlay(IPlayConfig config, KeyInputData inputData) {
        playConfig = config;
        screenData = new ScreenData();
        waitList = new ArrayList<>();
        this.inputData = inputData;
        try {
            screenImage = new Image(16 * 10, 16 * 20);
        } catch (SlickException e) {
            e.printStackTrace();
        }
        // dummy
        minoConfig = new NormalMinoConfig();
        minoImage = new SegaMinoImage();
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
                ch | (playConfig.getHideTurn(curLevel) << 16),
                inputData);
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
        if (!inputData.HOLD_BUTTON.isPush()) {
            return;
        }
        if (holdMino == null) {
            holdMino = waitList.remove(0);
            stockNextMino();
        }
        holdFlag = true;
        holdMino = tetriminoData.changeHold(holdMino);
    }

    @Override
    public ITetrisPlay nextFrame() {
        inputData.nextFrame();
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
            return this;
        } else if (playMode == PlayMode.WAIT_ARE) {
            waitNum--;
            if (waitNum <= 0) {
                waitNum = 0;
                nextTetrimino();
                holdCheck();
            }
            return this;
        } else if (playMode == PlayMode.GAME_OVER) {
            // ゲームオーバー
            return this;
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
            return this;
        } else {
            // HOLDチェック
            holdCheck();
            if (!tetriminoData.nextFrame(screenData)) {
                tetriminoData = null;
            }
        }
        return this;
    }

    @Override
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
        g.setColor(Color.green);
        g.drawString(String.valueOf(curLevel), 200, 100);
    }
}
