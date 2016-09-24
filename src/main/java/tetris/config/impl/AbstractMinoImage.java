package tetris.config.impl;

import org.newdawn.slick.*;
import org.newdawn.slick.opengl.pbuffer.GraphicsFactory;
import tetris.config.IMinoImage;

import java.util.HashMap;

/**
 * Created by hkoba on 2016/08/27.
 */
public abstract class AbstractMinoImage implements IMinoImage {

    private HashMap<Color, Image> blockImageMap = new HashMap<>();

    protected Image getBlockImage(Color color) {
        if (color == null) {
            return null;
        }
        Image image = blockImageMap.get(color);
        if (image == null) {
            int sz = getImageSize();
            //image = new ImageBuffer(sz, sz).getImage();
            try {
                image = new Image(sz, sz);
            } catch (SlickException e) {
                e.printStackTrace();
            }
            blockImageMap.put(color, image);
            try {
                Graphics g = image.getGraphics();
                g.setColor(color.darker());
                g.fillRect(0, 0, sz, sz);
                g.setColor(color.brighter());
                g.fillRect(0, 0, sz - 2, sz - 2);
                g.setColor(color);
                g.fillRect(2, 2, sz - 4, sz - 4);
                g.flush();
            } catch (SlickException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    protected abstract Color getBlockColor(char ch);

    protected int getImageSize() {
        return 16;
    }

    @Override
    public Image getMinoImage(char ch) {
        return getBlockImage(getBlockColor(ch));
    }

    @Override
    public Image getFlashImage() {
        return getBlockImage(Color.white);
    }
}
