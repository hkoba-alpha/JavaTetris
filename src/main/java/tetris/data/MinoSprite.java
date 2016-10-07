package tetris.data;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import tetris.config.IMinoData;
import tetris.config.IMinoImage;

import java.awt.*;

/**
 * Created by hkoba on 2016/08/18.
 */
public class MinoSprite {
    private IMinoData minoData;

    private char minoCode;
    private int blockX;
    private int blockY;
    private int blockNum;
    private double rotate;
    private Image blockImage;
    private int alpha;
    private int edgeFrame;

    public MinoSprite(char code, IMinoData mino, Image img) {
        minoCode = code;
        minoData = mino;
        blockX = 3;
        blockY = 2;
        blockImage = img;
        alpha = 255;
    }

    public MinoSprite(MinoSprite src) {
        this(src, src.blockImage);
    }
    public MinoSprite(MinoSprite src, Image img) {
        minoCode = src.minoCode;
        minoData = src.minoData;
        blockX = src.blockX;
        blockY = src.blockY;
        blockNum = src.blockNum;
        blockImage = img;
        this.alpha = src.alpha;
        rotate = src.rotate;
    }

    public void setRotate(double rot) {
        rotate = rot;
    }
    public void setAlpha(int al) {
        alpha = al;
    }

    public char getMinoCode() {
        return minoCode;
    }
    public IMinoData getMinoData() {
        return minoData;
    }
    public Image getBlockImage() {
        return blockImage;
    }

    public Point[] getBlockPoint() {
        Point[] ret = new Point[4];
        Point[] blkPoints = minoData.getPoints(blockNum);
        for (int i = 0; i < 4; i++) {
            ret[i] = new Point(blkPoints[i].x + blockX, blkPoints[i].y + blockY);
        }
        return ret;
    }

    public void drawMino(Graphics g) {
        int sz = blockImage.getWidth();
        Color filter = Color.white;
        if (alpha < 255) {
            filter = new Color(255, 255, 255, alpha);
        }
        Point[] ptlist = getBlockPoint();
        for (Point pt: ptlist) {
            g.drawImage(blockImage, pt.x * sz, (pt.y - 2) * sz, filter);
        }
        if (alpha >= 255) {
            EdgeLineData.makeEdgeData(ptlist).forEach(v -> {
                v.draw(g, edgeFrame >> 1);
            });
            edgeFrame++;
        }
    }
    public void drawMino(Graphics g, float scale, int sx, int sy) {
        int size = blockImage.getWidth();
        int sz = (int)(size * scale);
        Color filter = Color.white;
        if (alpha < 255) {
            filter = new Color(255, 255, 255, alpha);
        }
        Point[] ptlist = getBlockPoint();
        for (Point pt: ptlist) {
            int dx = (pt.x - blockX) * sz + sx;
            int dy = (pt.y - blockY) * sz + sy;
            g.drawImage(blockImage, dx, dy, dx + sz, dy + sz, 0, 0, size, size, filter);
        }
    }

    public void move(int dx) {
        blockX += dx;
    }
    public void rotate(int dx) {
        blockNum = (blockNum + dx) & 3;
    }
    public int getTurnIndex() {
        return blockNum;
    }
    public void drop(int dy) {
        blockY += dy;
    }
    public Point getPoint() {
        return new Point(blockX, blockY);
    }
}
