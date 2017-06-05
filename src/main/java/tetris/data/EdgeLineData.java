package tetris.data;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 端のラインを描画するためのクラス
 * Created by hkoba on 2016/10/02.
 */
public class EdgeLineData {
    private int drawX;
    private int drawY;
    /**
     * 1:上,2:右,4:下,8:左
     */
    private int drawFlag;

    private int offsetX;

    private static Image edgeImage;

    private static Image getEdgeImage() {
        if (edgeImage == null) {
            try {
                edgeImage = new Image(64, 16);
                Graphics g = edgeImage.getGraphics();
                g.setDrawMode(Graphics.MODE_ALPHA_MAP);
                g.setColor(Color.transparent);
                g.fillRect(0, 0, 32, 16);
                g.setDrawMode(Graphics.MODE_NORMAL);
                g.setLineWidth(4);
                g.drawGradientLine(0, 1, Color.white, 16, 1, Color.lightGray);
                g.drawGradientLine(32, 1, Color.white, 16, 1, Color.lightGray);
                g.drawGradientLine(32, 1, Color.white, 48, 1, Color.lightGray);
                g.drawGradientLine(64, 1, Color.white, 48, 1, Color.lightGray);
                g.flush();
            } catch (SlickException e) {
                e.printStackTrace();
            }
        }
        return edgeImage;
    }

    public EdgeLineData(int bx, int by, int flag) {
        drawX = bx * 16;
        drawY = (by - 2) * 16;
        drawFlag = flag;
        offsetX = ((bx + by) & 1) * 16;
    }

    /**
     * 描画する
     * @param g
     * @param frame
     */
    public void draw(Graphics g, int frame) {
        int sx = (frame & 31) ^ offsetX;
        g.pushTransform();
        g.translate(drawX + 8, drawY + 8);
        for (int i = 0; i < 4; i++) {
            if ((drawFlag & (1 << i)) > 0) {
                g.drawImage(getEdgeImage(), -8, -8, 8, 8, sx, 0, sx + 16, 16);
            }
            g.rotate(0, 0, 90);
            sx = (sx + 0x10) & 31;
        }
        g.popTransform();
    }

    public static List<EdgeLineData> makeEdgeData(Point[] pointList) {
        List<EdgeLineData> retList = new ArrayList<>();
        for (Point pt: pointList) {
            int flag = 15;
            for (Point pt2: pointList) {
                if (pt == pt2) {
                    continue;
                }
                if (pt.x == pt2.x) {
                    if (pt.y - 1 == pt2.y) {
                        flag &= 0xe;
                    } else if (pt.y + 1 == pt2.y) {
                        flag &= 0xb;
                    }
                } else if (pt.y == pt2.y) {
                    if (pt.x - 1 == pt2.x) {
                        flag &= 0x7;
                    } else if (pt.x + 1 == pt2.x) {
                        flag &= 0xd;
                    }
                }
            }
            if (flag > 0) {
                retList.add(new EdgeLineData(pt.x, pt.y, flag));
            }
        }
        return retList;
    }
}
