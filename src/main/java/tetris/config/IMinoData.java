package tetris.config;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by hkoba on 2016/08/27.
 */
public interface IMinoData {
    /**
     * テトリミノの相対位置
     * @param ix 0-3
     * @return
     */
    Point[] getPoints(int ix);


    /**
     * 回転軸の座標
     * @return
     */
    Point2D getCenterPoint();

    /**
     * SRSのデータ
     * @return
     */
    Point[] getSrsData(int fromIndex, int toIndex);
}
