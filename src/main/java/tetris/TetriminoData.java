package tetris;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;

import java.util.ArrayList;

/**
 * Created by hkoba on 2016/08/16.
 */
public class TetriminoData {
    private ImageBuffer[] iconData;

    private class MinoItem {
        private float rotate;
        private int blockX;
        private int blockY;
        private int[] dx;
        private int[] dy;
        private MinoItem() {
            dx = new int[] {-1, 0, 1, 0};
            dy = new int[] {0, 0, 0, 1};
        }

        private void draw(Graphics g, Image icon) {
            if (rotate != 0) {
                g.pushTransform();
                g.rotate(blockX * 16, blockY * 16, rotate);
                for (int i = 0; i < dx.length; i++) {
                    g.drawImage(icon, (blockX + dx[i]) * 16, (blockY + dy[i]) * 16);
                }
                g.popTransform();
            } else {
                for (int i = 0; i < dx.length; i++) {
                    g.drawImage(icon, (blockX + dx[i]) * 16, (blockY + dy[i]) * 16);
                }
            }
        }
    }

    private ArrayList<MinoItem> minoList = new ArrayList<>();

    public TetriminoData() {
        iconData = new ImageBuffer[8];
        for (int i = 0; i < iconData.length; i++) {
            iconData[i] = new ImageBuffer(16, 16);
            byte[] buf = iconData[i].getRGBA();
            for (int j = 0; j < buf.length; j+=4) {
                if (i > 0) {
                    int x = ((j >> 2) & 0x0f);
                    int y = (j >> 6);
                    if (x > 0 && x < 15 && y > 0 && y < 15) {
                        continue;
                    }
                }
                buf[j] = (byte)128;
                buf[j + 1] = (byte)128;
                buf[j + 2] = (byte)255;
                buf[j + 3] = (byte)(255 - i * 32);
            }
        }
    }

    public void addIcon(int bx, int by) {
        MinoItem item = new MinoItem();
        item.blockX = bx;
        item.blockY = by;
        minoList.add(0, item);
        if (minoList.size() > 8) {
            minoList.remove(8);
        }
    }
    public void addRotate(float rot) {
        MinoItem item = new MinoItem();
        item.blockX = minoList.get(0).blockX;
        item.blockY = minoList.get(0).blockY;
        item.rotate = rot;
        minoList.add(0, item);
        if (minoList.size() > 8) {
            minoList.remove(8);
        }
    }

    public void draw(Graphics g) {
        for (int i = minoList.size() - 1; i >= 0; i--) {
            if (minoList.get(i) != null) {
                minoList.get(i).draw(g, iconData[i].getImage());
            }
        }
    }
}
