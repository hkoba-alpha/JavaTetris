package tetris.config;

import org.newdawn.slick.Image;

/**
 * Created by hkoba on 2016/08/27.
 */
public interface IMinoImage {
    Image getMinoImage(char ch);

    Image getFlashImage();
}
