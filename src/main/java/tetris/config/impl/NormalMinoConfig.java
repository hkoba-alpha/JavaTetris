package tetris.config.impl;

import tetris.config.IMinoConfig;
import tetris.config.IMinoData;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 * Created by hkoba on 2016/08/28.
 */
public class NormalMinoConfig implements IMinoConfig {
    private static final int[] minoPoints = {
        // I
        0x01112131, 0x20212223, 0x32221202, 0x13121110,
        // J
        0x00011121, 0x20101112, 0x22211101, 0x02121110,
        // L
        0x20211101, 0x22121110, 0x02011121, 0x00101112,
        // O
        0x10202111, 0x20211110, 0x21111020, 0x11102021,
        //0x11212212, 0x21221211, 0x22121121, 0x12112122,
        // S
        0x20101101, 0x22211110, 0x02121121, 0x00011112,
        // T
        0x10011121, 0x21101112, 0x12211101, 0x01121110,
        // Z
        0x00101121, 0x20211112, 0x22121101, 0x02011110
    };
    private static final Point2D[] centerPoints = {
            new Point2D.Double(2, 2),
            new Point2D.Double(1.5, 1.5),
            new Point2D.Double(1.5, 1.5),
            new Point2D.Double(2, 1),
            new Point2D.Double(1.5, 1.5),
            new Point2D.Double(1.5, 1.5),
            new Point2D.Double(1.5, 1.5)
    };
    private static final int[][] etcMinoSrs = {
            {0, 1, -1, 0, -1, -1, 0, 2, -1, 2},
            {1, 0, 1, 0, 1, 1, 0, -2, 1, -2},
            {1, 2, 1, 0, 1, 1, 0, -2, 1, -2},
            {2, 1, -1, 0, -1, -1, 0, 2, -1, 2},
            {2, 3, 1, 0, 1, -1, 0, 2, 1, 2},
            {3, 2, -1, 0, -1, 1, 0, -2, -1, -2},
            {3, 0, -1, 0, -1, 1, 0, -2, -1, -2},
            {0, 3, 1, 0, 1, -1, 0, 2, 1, 2}
    };
    private static final int[][] iMinoSrc = {
            {0, 1, -2, 0, 1, 0, -2, 1, 1, -2},
            {1, 0, 2, 0, -1, 0, 2, -1, -1, 2},
            {1, 2, -1, 0, 2, 0, -1, -2, 2, 1},
            {2, 1, 1, 0, -2, 0, 1, 2, -2, -1},
            {2, 3, 2, 0, -1, 0, 2, -1, -1, 2},
            {3, 2, -2, 0, 1, 0, -2, 1, 1, -2},
            {3, 0, 1, 0, -2, 0, 1, 2, -2, -1},
            {0, 3, -1, 0, 2, 0, -1, -2, 2, 1}
    };

    private HashMap<Character, IMinoData> minoDataMap = new HashMap<>();

    public NormalMinoConfig() {
        String data = "IJLOSTZ";
        for (int i = 0; i < data.length(); i++) {
            final Point[][] blkPoints = new Point[4][4];
            final HashMap<Integer, Point[]> srsMap = new HashMap<>();
            final Point2D centerPoint = centerPoints[i];
            int[][] srsData = i == 0 ? iMinoSrc: etcMinoSrs;
            for (int j = 0; j < 4; j++) {
                int val = minoPoints[i * 4 + j];
                for (int k = 0; k < 4; k++) {
                    int x = (val >> (28 - k * 8)) & 15;
                    int y = (val >> (24 - k * 8)) & 15;
                    blkPoints[j][k] = new Point(x, y);
                }
            }
            for (int[] srs: srsData) {
                int key = srs[0] | (srs[1] << 2);
                Point[] ptlst = new Point[srs.length / 2 - 1];
                for (int j = 0; j < ptlst.length; j++) {
                    ptlst[j] = new Point(srs[j * 2 + 2], srs[j * 2 + 3]);
                }
                srsMap.put(key, ptlst);
            }
            IMinoData minoData = new IMinoData() {
                @Override
                public Point[] getPoints(int ix) {
                    return blkPoints[ix];
                }

                @Override
                public Point2D getCenterPoint() {
                    return centerPoint;
                }

                @Override
                public Point[] getSrsData(int fromIndex, int toIndex) {
                    return srsMap.get(fromIndex | (toIndex << 2));
                }
            };
            minoDataMap.put(data.charAt(i), minoData);
        }
    }

    @Override
    public IMinoData getMinoData(char ch) {
        return minoDataMap.get(ch);
    }
}
