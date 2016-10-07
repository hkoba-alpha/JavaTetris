package tetris.data;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Polygon;
import tetris.config.IMinoImage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hkoba on 2016/08/18.
 */
public class ScreenData {
    private static class FadeOutItem {
        private float x;
        private float y;
        private float rotateX;
        private float rotateY;
        private float rotateZ;
        private float dx;
        private float dy;
        private float dRotateX;
        private float dRotateY;
        private float dRotateZ;

        FadeOutItem(int bx, int by) {
            x = bx * 16 + 8;
            y = (by - 2) * 16 + 8;
            dx = (float)(Math.random() * 1 - 0.5);
            dy = (float)(Math.random() * 1 - 0.5);
            dRotateX = (float)(Math.random() * 0.2 - 0.1);
            dRotateY = (float)(Math.random() * 0.2 - 0.1);
            dRotateZ = (float)(Math.random() * 0.2 - 0.1);
        }

        private org.newdawn.slick.geom.Point getPoint(float x, float y) {
            // X
            double xx = x;
            double yy = y * Math.cos(rotateX);
            double zz = y * Math.sin(rotateX);
            // Y
            xx = xx * Math.cos(rotateY) + zz * Math.sin(rotateY);
            zz = -xx * Math.sin(rotateY) + zz * Math.cos(rotateY);
            // Z
            xx = xx * Math.cos(rotateZ) - yy * Math.sin(rotateZ);
            yy = xx * Math.sin(rotateZ) + yy * Math.cos(rotateZ);
            return new org.newdawn.slick.geom.Point((float)xx + this.x, (float)yy + this.y);
        }

        private void draw(Graphics g) {
            Polygon polygon = new Polygon();
            org.newdawn.slick.geom.Point pt1 = getPoint(-8, -8);
            polygon.addPoint(pt1.getX(), pt1.getY());
            org.newdawn.slick.geom.Point pt2 = getPoint(-8, 8);
            polygon.addPoint(pt2.getX(), pt2.getY());
            org.newdawn.slick.geom.Point pt3 = getPoint(8, 8);
            polygon.addPoint(pt3.getX(), pt3.getY());
            org.newdawn.slick.geom.Point pt4 = getPoint(8, -8);
            polygon.addPoint(pt4.getX(), pt4.getY());
            g.fill(polygon);
            x += dx;
            y += dy;
            rotateX += dRotateX;
            rotateY += dRotateY;
            rotateZ += dRotateZ;
        }
    }

    private int[][] blockData;
    private List<FadeOutItem> fadeOutItemList = new ArrayList<>();
    private int fadeCounter;
    private List<EdgeLineData> edgeLineDataList = new ArrayList<>();
    private int edgeFrame;

    private final int[] hideAlphaList = {
        0, 0x80, 0xc0, 0xe0, 0xf0, 0xf8
    };

    public ScreenData() {
        blockData = new int[22][11];
    }

    public boolean isBlock(int x, int y) {
        if (x < 0 || x >= 10 || y < 0 || y >= 22) {
            return true;
        }
        return (blockData[y][x] & 0x7f) > 0;
    }

    public boolean isBlock(Point[] ptlst, int dx, int dy) {
        for (Point pt: ptlst) {
            if (isBlock(pt.x + dx, pt.y + dy)) {
                return true;
            }
        }
        return false;
    }

    private void redrawBlock(int x, int y) {
        if (y >= 0 && y < blockData.length && x >= 0 && x < 10) {
            blockData[y][x] |= 0x80;
        }
    }

    public void putBlock(Point[] ptlst, int blk) {
        for (Point pt: ptlst) {
            blockData[pt.y][pt.x] = blk | 0x300ffc0;
            blockData[pt.y][10] |= (1 << pt.x);
            redrawBlock(pt.x - 1, pt.y);
            redrawBlock(pt.x + 1, pt.y);
            redrawBlock(pt.x, pt.y - 1);
            redrawBlock(pt.x, pt.y + 1);
        }
        makeEdgeList();
    }

    /**
     * ラインを消す
     * @return
     */
    public int deleteLine() {
        int ret = 0;
        int y = 0;
        fadeCounter = 300;
        for (int[] line : blockData) {
            if (line[10] == 0x3ff) {
                // 消えた
                for (int x = 0; x < 10; x++) {
                    line[x] = 0x80;
                    fadeOutItemList.add(new FadeOutItem(x, y));
                }
                line[10] = 0;
                ret++;
            }
            y++;
        }
        // 消えるターンのチェック
        for (int[] line: blockData) {
            for (int x = 0; x < 10; x++) {
                if ((line[x] & 0xff0000) > 0) {
                    line[x] -= 0x10000;
                    if ((line[x] & 0xff0000) < 0x80000) {
                        // 消え始める
                        line[x] -= 0x100;
                    }
                }
            }
        }
        if (ret > 0) {
            makeEdgeList();
        }
        return ret;
    }

    /**
     * 消えたラインを落とす
     */
    public void dropLines() {
        // 空いたラインをドロップする
        int topY = 0;
        while (topY < blockData.length) {
            if (blockData[topY][10] > 0) {
                break;
            }
            topY++;
        }
        int bottomY = topY;
        int topY2 = topY;
        for (int y = blockData.length - 1; y > topY2; y--) {
            if (blockData[y][10] == 0) {
                // 消えていた
                if (y > bottomY) {
                    bottomY = y;
                }
                for (int y1 = y; y1 > topY2; y1--) {
                    blockData[y1] = blockData[y1 - 1];
                }
                blockData[topY2] = new int[11];
                topY2++;
                y++;
            }
        }
        if (topY < bottomY) {
            if (bottomY < blockData.length - 1) {
                bottomY++;
            }
            // フラグをつける
            for (int y = topY; y <= bottomY; y++) {
                int[] line = blockData[y];
                for (int x = 0; x < 10; x++) {
                    line[x] |= 0x80;
                }
            }
        }
        makeEdgeList();
    }

    public void makeEdgeList() {
        edgeLineDataList.clear();
        for (int y = 2; y < blockData.length; y++) {
            for (int x = 0; x < 10; x++) {
                if ((blockData[y][x] & 0x7f) == 0) {
                    continue;
                }
                if (((blockData[y][x] >> 8) & 0xff) < 64) {
                    continue;
                }
                int flag = 0;
                if (!isBlock(x, y - 1)) {
                    flag |= 1;
                }
                if (!isBlock(x + 1, y)) {
                    flag |= 2;
                }
                if (!isBlock(x, y + 1)) {
                    flag |= 4;
                }
                if (!isBlock(x - 1, y)) {
                    flag |= 8;
                }
                if (flag > 0) {
                    edgeLineDataList.add(new EdgeLineData(x, y, flag));
                }
            }
        }
    }

    /**
     * 消えていくのをチェックする
     */
    private void hideCheck() {
        for (int[] line: blockData) {
            for (int x = 0; x < 10; x++) {
                if ((line[x] & 0x7f) == 0) {
                    continue;
                }
                int fill = (line[x] >> 8) & 255;
                if (fill < 255) {
                    int ix = (line[x] >> 16) & 255;
                    int dest = 255;
                    if (ix < hideAlphaList.length) {
                        dest = hideAlphaList[ix];
                    }
                    if (dest < fill) {
                        fill -= 8;
                        if (fill < dest) {
                            fill = dest;
                        }
                        line[x] = (line[x] & 0xff00ff) | (fill << 8) | 0x80;
                    }
                }
            }
        }
    }

    public void drawScreen(Graphics g, IMinoImage minoImage) {
        hideCheck();
        int sz = minoImage.getFlashImage().getWidth();
        for (int y = 2; y < blockData.length; y++) {
            for (int x = 0; x < 10; x++) {
                int blk = blockData[y][x];
                if ((blk & 0x80) > 0) {
                    // 描画する
                    int sx = x * sz;
                    int sy = (y - 2) * sz;
                    if (blk > 0x1000000) {
                        // 落ちた直後のフラッシュ
                        g.drawImage(minoImage.getFlashImage(), sx, sy);
                        blockData[y][x] -= 0x1000000;
                        continue;
                    }
                    g.setDrawMode(Graphics.MODE_ALPHA_MAP);
                    g.setColor(Color.transparent);
                    g.fillRect(sx, sy, sz, sz);
                    g.setDrawMode(Graphics.MODE_NORMAL);
                    if ((blk & 0x7f) > 0) {
                        Image img = minoImage.getMinoImage((char)(blk & 0x7f));
                        if (img != null) {
                            int fill = (blk >> 8) & 255;
                            if (fill < 255) {
                                // 消えつつある
                                g.drawImage(img, sx, sy, new Color(255, 255, 255, fill));
                            } else {
                                g.drawImage(img, sx, sy);
                            }
                        }
                    }
                    blockData[y][x] &= ~0x80;
                }
            }
        }
        edgeLineDataList.forEach(v -> v.draw(g, edgeFrame >> 2));
        edgeFrame++;
    }

    public void drawFadeItem(Graphics g) {
        if (fadeOutItemList.size() > 0) {
            g.setColor(new Color(255, 255, 255, fadeCounter));
            for (FadeOutItem item: fadeOutItemList) {
                item.draw(g);
            }
            fadeCounter -= 4;
            if (fadeCounter < 128) {
                fadeCounter = 0;
                fadeOutItemList.clear();
            }
        }
    }
}
