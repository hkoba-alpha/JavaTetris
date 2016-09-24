package tetris.config.impl;

import org.newdawn.slick.Color;

/**
 * Created by hkoba on 2016/08/28.
 */
public class SegaMinoImage extends AbstractMinoImage {
    private final String charData = "TJLZSIO";

    private final Color[] blockColor = {
            Color.cyan.brighter(),
            Color.blue.brighter(),
            Color.orange,
            Color.green.brighter(),
            Color.pink.brighter(),
            Color.red.brighter(),
            Color.yellow.brighter()
    };
    @Override
    protected Color getBlockColor(char ch) {
        if (ch == 0) {
            return null;
        }
        return blockColor[charData.indexOf(ch)];
    }
}
