package tetris.play;

import org.newdawn.slick.Graphics;

/**
 * Created by hkoba on 2016/09/24.
 */
public interface ITetrisPlay {
    ITetrisPlay nextFrame(ITetrisPlay[] playList, int index);

    void draw(Graphics g);
}
