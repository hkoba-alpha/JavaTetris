package tetris.config.impl;

import tetris.config.IPlayConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hkoba on 2016/09/01.
 */
public class TgmPlayConfig implements IPlayConfig {
    private List<Character> nextList = new ArrayList<>();

    private Random randomData = new Random();
    private String lastMino = "ZS";

    private int firstLevel;

    public TgmPlayConfig(int level) {
        firstLevel = level;
    }

    protected List<Character> makeNextList(String secondList) {
        StringBuilder bufData = new StringBuilder("ITOSZJL");
        if (secondList != null) {
            for (int i = 0; i < bufData.length(); i++) {
                if (secondList.indexOf(bufData.charAt(i)) >= 0) {
                    bufData.deleteCharAt(i);
                    i--;
                }
            }
        }
        List<Character> retList = new ArrayList<>();
        boolean firstFlag = true;
        while (bufData.length() > 0) {
            int ix = randomData.nextInt(bufData.length());
            retList.add(bufData.charAt(ix));
            bufData.deleteCharAt(ix);
            if (firstFlag) {
                firstFlag = false;
                if (secondList != null) {
                    bufData.append(secondList);
                }
            }
        }
        return retList;
    }

    @Override
    public char getNextMino() {
        if (nextList.size() == 0) {
            nextList.addAll(makeNextList(lastMino));
            lastMino = String.valueOf(nextList.get(nextList.size() - 1));
        }
        return nextList.remove(0);
    }

    @Override
    public boolean canHold() {
        return true;
    }

    @Override
    public boolean isGhost(int level) {
        return true;
    }

    @Override
    public int initLevel(long seed) {
        randomData = new Random(seed);
        return firstLevel;
    }

    @Override
    public int getNextLevel(int curLevel, int deleteLines) {
        int limit = getTargetLevel(curLevel) - 1;
        int next = curLevel + deleteLines + 1;
        if (deleteLines == 0 && next > limit) {
            next = limit;
        }
        return next;
    }

    @Override
    public int getTargetLevel(int level) {
        int limit = (level / 100) * 100 + 100;
        if (limit > 999) {
            limit = 999;
        }
        return limit;
    }

    @Override
    public int getHideTurn(int level) {
        return 0;
    }

    @Override
    public int getGravity(int level) {
        int[] gravity = getGravityList();
        int ret = 4;
        for (int i = 0; i < gravity.length; i += 2) {
            if (level < gravity[i]) {
                break;
            }
            ret = gravity[i + 1];
        }
        return ret;
    }

    private static final int[] gravityList = {
        0, 4,
            30, 6,
            35, 8,
            40, 10,
            50, 12,
            60, 16,
            70, 32,
            80, 48,
            90, 64,
            100, 80,
            120, 96,
            140, 112,
            160, 128,
            170, 144,
            200, 4,
            220, 32,
            230, 64,
            233, 96,
            236, 128,
            239, 160,
            243, 192,
            247, 224,
            251, 256,
            300, 512,
            330, 768,
            360, 1024,
            400, 1280,
            420, 1024,
            450, 768,
            500, 5120
    };

    protected int[] getGravityList() {
        return gravityList;
    }

    @Override
    public int getAre(int level) {
        return 25;
    }

    @Override
    public int getLineAre(int level) {
        if (level >= 800) {
            return 12;
        } else if (level >= 700) {
            return 16;
        }
        return 25;
    }

    @Override
    public int getDas(int level) {
        if (level >= 500) {
            return 8;
        }
        return 14;
    }

    @Override
    public int getLock(int level) {
        if (level >= 900) {
            return 17;
        }
        return 30;
    }

    @Override
    public int getLineClear(int level) {
        if (level >= 800) {
            return 6;
        } else if (level >= 700) {
            return 12;
        } else if (level >= 600) {
            return 16;
        } else if (level >= 500) {
            return 25;
        }
        return 40;
    }
}
