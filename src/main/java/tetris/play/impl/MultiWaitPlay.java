package tetris.play.impl;

import org.newdawn.slick.Graphics;
import tetris.play.ITetrisPlay;

/**
 * 複数プレイヤーで待ち状態
 * Created by hkoba on 2016/09/25.
 */
public class MultiWaitPlay implements ITetrisPlay {
    private ModeSelectPlay selectPlay;

    private MultiWaitPlay primaryPlay;

    private int waitCount;

    public MultiWaitPlay(ModeSelectPlay play) {
        selectPlay = play;
        waitCount = 60 * 30;
    }

    @Override
    public ITetrisPlay nextFrame(ITetrisPlay[] playList, int index) {
        int count = 0;
        boolean firstFlag = true;
        for (int i = 0; i < playList.length; i++) {
            if (playList[i] instanceof ModeSelectPlay) {
                // まだ選択中
                count++;
            } else if (firstFlag && i < index && playList[i] instanceof MultiWaitPlay) {
                firstFlag = false;
                boolean setFlag = false;
                if (primaryPlay == null) {
                    setFlag = true;
                }
                primaryPlay = (MultiWaitPlay)playList[i];
                if (setFlag) {
                    primaryPlay.waitCount = this.waitCount;
                }
            }
        }
        if (primaryPlay == null) {
            primaryPlay = this;
        }
        //
        if (primaryPlay == this) {
            waitCount--;
        }
        if (count == 0 || primaryPlay.waitCount == 0) {
            // 完了
            return new TetrisPlay(selectPlay);
        }
        return this;
    }

    @Override
    public void draw(Graphics g) {
        if (primaryPlay == null) {
            return;
        }
        g.drawString(Integer.toString(waitCount / 60), 48, 160);
    }
}
