package tetris.config.impl;

import org.newdawn.slick.Color;

/**
 * Created by hkoba on 2016/08/28.
 */
public class WorldMinoImage extends AbstractMinoImage {
    private final String charData = "IJLOZST";

    private final Color[] blockColor = {
            Color.cyan.brighter(),
            Color.blue.brighter(),
            Color.orange,
            Color.yellow.brighter(),
            Color.red.brighter(),
            Color.green.brighter(),
            Color.pink.brighter()
    };
    @Override
    protected Color getBlockColor(char ch) {
        if (ch == 0) {
            return null;
        }
        return blockColor[charData.indexOf(ch)];
    }
}
