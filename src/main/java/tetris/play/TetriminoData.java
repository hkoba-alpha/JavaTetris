package tetris.play;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import tetris.config.IMinoData;
import tetris.data.MinoSprite;
import tetris.data.ScreenData;

import java.awt.*;
import java.util.ArrayList;

/**
 * 移動中のテトリミノデータ
 * Created by hkoba on 2016/08/30.
 */
public class TetriminoData {
    public static final int GRAVITY_SIZE = 256;

    private MinoSprite minoSprite;

    private int dasTime;
    private int gravitySpeed;
    private int lockTime;
    private int putBlock;

    private int gravityCount;
    private int lockCount;

    private int turnTime;

    private int moveCount;

    private MinoSprite[] traceList = new MinoSprite[8];

    public TetriminoData(MinoSprite mino, int das, int lock, int gravity, int block) {
        minoSprite = mino;
        dasTime = das;
        lockTime = lock;
        gravitySpeed = gravity;
        putBlock = block;
        moveCount = 256;
        lockCount = -1;
    }

    public MinoSprite changeHold(MinoSprite hold) {
        lockCount = -1;
        moveCount = 256;
        turnTime = 0;
        putBlock = (putBlock & 0xffffff00) | hold.getMinoCode();
        MinoSprite ret = new MinoSprite(minoSprite.getMinoCode(), minoSprite.getMinoData(), minoSprite.getBlockImage());
        minoSprite = hold;
        return ret;
    }
    public MinoSprite getGhost(ScreenData screenData) {
        MinoSprite ghost = new MinoSprite(minoSprite);
        ghost.setAlpha(96);
        while (true) {
            if (screenData.isBlock(ghost.getBlockPoint(), 0, 1)) {
                break;
            }
            ghost.drop(1);
        }
        return ghost;
    }

    public void draw(Graphics g) {
        for (MinoSprite sprite: traceList) {
            if (sprite != null) {
                sprite.drawMino(g);
            }
        }
        minoSprite.drawMino(g);
    }

    /**
     * ゲームオーバーをチェックする
     * @param screenData
     * @return
     */
    public boolean isGameOver(ScreenData screenData) {
        int turn = 0;
        if (KeyAction.TURN_LEFT.isPush()) {
            turn = -1;
        } else if (KeyAction.TURN_RIGHT.isPush()) {
            turn = 1;
        }
        turnCheck(screenData, turn);
        return screenData.isBlock(minoSprite.getBlockPoint(), 0, 0);
    }
    public boolean nextFrame(ScreenData screenData) {
        for (int i = 0; i < traceList.length - 1; i++) {
            traceList[i] = traceList[i + 1];
            if (traceList[i] != null) {
                traceList[i].setAlpha(i * 32 + 31);
            }
        }
        traceList[traceList.length - 1] = null;
        turnCheck(screenData);
        moveCheck(screenData);
        dropCheck(screenData);
        return touchCheck(screenData);
    }

    private void addTrace(MinoSprite sprite, int turn, boolean forceFlag) {
        if (traceList[traceList.length - 1] != null) {
            if (!forceFlag) {
                return;
            }
            for (int i = 0; i < traceList.length - 1; i++) {
                traceList[i] = traceList[i + 1];
                if (traceList[i] != null) {
                    traceList[i].setAlpha(i * 32 + 31);
                }
            }
        }
        //MinoSprite newSprite = new MinoSprite(sprite, traceImage);
        MinoSprite newSprite = new MinoSprite(sprite);
        newSprite.setAlpha(0xc0);
        traceList[traceList.length - 1] = newSprite;
    }

    private MinoSprite turn(ScreenData screenData, int turn) {
        MinoSprite newSprite = new MinoSprite(minoSprite);
        while (turn != 0) {
            int old = newSprite.getTurnIndex();
            if (turn < 0) {
                newSprite.rotate(-1);
                turn++;
            } else {
                newSprite.rotate(1);
                turn--;
            }
            Point[] ptList = newSprite.getBlockPoint();
            if (!screenData.isBlock(ptList, 0, 0)) {
                return newSprite;
            }
            Point[] srs = minoSprite.getMinoData().getSrsData(old, newSprite.getTurnIndex());
            if (srs == null) {
                return null;
            }
            for (Point pt : srs) {
                if (!screenData.isBlock(ptList, pt.x, pt.y)) {
                    newSprite.move(pt.x);
                    newSprite.drop(pt.y);
                    return newSprite;
                }
            }
        }
        return null;
    }

    private void turnCheck(ScreenData screenData) {
        int turn = 0;
        if (KeyAction.TURN_RIGHT.getCount() == 1) {
            // 回転する
            turn = 1;
        } else if (KeyAction.TURN_LEFT.getCount() == 1) {
            // 回転する
            turn = -1;
        }
        turnCheck(screenData, turn);
    }
    private void turnCheck(ScreenData screenData, int turn) {
        if (turnTime > 0) {
            turnTime--;
        }
        if (turn == 0) {
            return;
        }
        MinoSprite newSprite = turn(screenData, turn);
        if (newSprite == null) {
            // 回転できない
            if (turnTime > 0) {
                // 連続回転
                turn *= 2;
                newSprite = turn(screenData, turn);
                if (newSprite == null) {
                    // やっぱり回転できない
                    turnTime = 0;
                }
            } else {
                turnTime = 15;
            }
        }
        if (newSprite != null) {
            // 回転できた
            addTrace(minoSprite, turn * 45, true);
            minoSprite = newSprite;
            moveCount--;
            if (moveCount > 0) {
                if (screenData.isBlock(minoSprite.getBlockPoint(), 0, 1)) {
                    lockCount = 0;
                } else {
                    lockCount = -1;
                }
            }
        }
    }
    private void moveCheck(ScreenData screenData) {
        int ax = 0;
        if (KeyAction.MOVE_RIGHT.getCount() == 1 || KeyAction.MOVE_RIGHT.getCount() >= dasTime) {
            // 右移動
            ax = 1;
        } else if (KeyAction.MOVE_LEFT.getCount() == 1 || KeyAction.MOVE_LEFT.getCount() >= dasTime) {
            // 左移動
            ax = -1;
        }
        if (ax != 0 && !screenData.isBlock(minoSprite.getBlockPoint(), ax, 0)) {
            // 移動可能
            addTrace(minoSprite, 0, false);
            minoSprite.move(ax);
            moveCount--;
            if (moveCount > 0) {
                if (screenData.isBlock(minoSprite.getBlockPoint(), 0, 1)) {
                    lockCount = 0;
                } else {
                    lockCount = -1;
                }
            }
        }
    }
    private void dropCheck(ScreenData screenData) {
        if (lockCount >= 0) {
            //System.out.println("lock=" + lockCount + ", down=" + KeyAction.MOVE_DROP.getCount());
            if (KeyAction.MOVE_DROP.getCount() == 1) {
                // 着地させる
                lockCount = lockTime;
            }
            return;
        }
        gravityCount += gravitySpeed;
        if (gravityCount < GRAVITY_SIZE && KeyAction.MOVE_DROP.isPush()) {
            gravityCount = GRAVITY_SIZE;
        } else if (KeyAction.HARD_DROP.isPush()) {
            // 下まで落とす
            gravityCount = GRAVITY_SIZE * 20;
        }
        int traceSize = 0;
        boolean forceFlag = false;
        while (gravityCount >= GRAVITY_SIZE) {
            // 落ちる
            if (screenData.isBlock(minoSprite.getBlockPoint(), 0, 1)) {
                // 着地が始まった
                lockCount = 0;
                gravityCount = 0;
                moveCount--;
            } else {
                gravityCount -= GRAVITY_SIZE;
                if (traceSize == 0) {
                    addTrace(minoSprite, 0, forceFlag);
                    traceSize = 5;
                    forceFlag = true;
                }
                minoSprite.drop(1);
                traceSize--;
            }
        }
    }
    private boolean touchCheck(ScreenData screenData) {
        if (lockCount < 0) {
            return true;
        }
        lockCount++;
        if (lockCount > lockTime) {
            // 着地した
            screenData.putBlock(minoSprite.getBlockPoint(), putBlock);
            return false;
        }
        return true;
    }
}
