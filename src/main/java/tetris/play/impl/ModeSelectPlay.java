package tetris.play.impl;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import tetris.config.IMinoConfig;
import tetris.config.IMinoImage;
import tetris.config.IPlayConfig;
import tetris.config.impl.NormalMinoConfig;
import tetris.config.impl.SegaMinoImage;
import tetris.config.impl.TgmPlayConfig;
import tetris.config.impl.WorldMinoImage;
import tetris.data.KeyInputData;
import tetris.play.ITetrisPlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by hkoba on 2016/09/25.
 */
public class ModeSelectPlay implements ITetrisPlay {
    private KeyInputData inputData;

    private IMinoConfig minoConfig;
    private IMinoImage minoImage;
    private IPlayConfig playConfig;

    private long randomSeed;

    /**
     * 今選んでいるモード
     */
    private String selectMode;

    /**
     * 今選択中のモード
     */
    private String selectLevel = "";

    private Map<String, String> selectMenuMap = new HashMap<>();
    private Map<String, String> selectTextMap = new HashMap<>();

    private List<String> curMenuList = new ArrayList<>();

    private int handicapTime;

    private static final String[] menuList = {
            "1:SINGLE PLAY",
            "2:MULTI PLAY",
            "3:LEVEL",
            "30:LEVEL 0",
            "31:LEVEL 100",
            "32:LEVEL 200",
            "33:LEVEL 300",
            "34:LEVEL 400",
            "35:LEVEL 500",
            "4:HANDICAP",
            "40:0:00-",
            "400:0:00",
            "401:0:10",
            "402:0:20",
            "403:0:30",
            "404:0:40",
            "405:0:50",
            "41:1:00-",
            "410:1:00",
            "411:1:10",
            "412:1:20",
            "413:1:30",
            "414:1:40",
            "415:1:50",
            "42:2:00-",
            "420:2:00",
            "421:2:10",
            "422:2:20",
            "423:2:30",
            "424:2:40",
            "425:2:50",
            "43:3:00-",
            "430:3:00",
            "431:3:10",
            "432:3:20",
            "433:3:30",
            "434:3:40",
            "435:3:50",
            "44:4:00-",
            "440:4:00",
            "441:4:10",
            "442:4:20",
            "443:4:30",
            "444:4:40",
            "445:4:50",
            "5:COLOR",
            "51:SEGA COLOR",
            "52:WORLD COLOR"
    };

    public ModeSelectPlay(KeyInputData inputData) {
        this.inputData = inputData;
        minoConfig = new NormalMinoConfig();
        minoImage = new SegaMinoImage();
        playConfig = new TgmPlayConfig(0);
        randomSeed = System.currentTimeMillis();
        // 初期モード
        Map<String, String> selMap = new HashMap<>();
        for (String menu: menuList) {
            int ix = menu.indexOf(':');
            if (ix > 1) {
                String key = menu.substring(0, 1);
                String sel = selMap.get(key);
                if (sel == null || ix > sel.length()) {
                    selMap.put(key, menu.substring(0, ix));
                    selectTextMap.put(key, menu.substring(ix + 1));
                }
            }
        }
        makeMenu();
    }

    public int getHandicapTime() {
        return handicapTime;
    }

    @Override
    public ITetrisPlay nextFrame(ITetrisPlay[] playList, int index) {
        inputData.nextFrame();
        int dy = 0;
        if (inputData.MOVE_DROP.getCount() == 1) {
            dy = 1;
        } else if (inputData.HARD_DROP.getCount() == 1) {
            dy = -1;
        }
        if (dy != 0) {
            int ix = curMenuList.indexOf(selectMode);
            ix = (ix + dy + curMenuList.size()) % curMenuList.size();
            selectMode = curMenuList.get(ix);
        }
        if (inputData.START_BUTTON.getCount() == 1) {
            String mode = selectMode;
            selectMenuMap.put(selectLevel, selectMode);
            selectLevel = selectMode;
            selectMode = selectMenuMap.get(selectLevel);
            makeMenu();
            if (curMenuList.size() == 0) {
                if (selectLevel.length() > 1) {
                    // メニューに戻る
                    for (String menu: menuList) {
                        if (menu.startsWith(mode + ":")) {
                            selectTextMap.put(menu.substring(0, 1), menu.substring(mode.length() + 1));
                            break;
                        }
                    }
                    selectLevel = "";
                    selectMode = selectMenuMap.get(selectLevel);
                    makeMenu();
                }
                return selectMode(mode);
            }
            //return new TetrisPlay(this);
        }
        return this;
    }

    private ITetrisPlay selectMode(String mode) {
        if ("1".equals(mode)) {
            // Single
            return new TetrisPlay(this);
        } else if ("2".equals(mode)) {
            // Multi
            return new MultiWaitPlay(this);
        } else if ("51".equals(mode)) {
            minoImage = new SegaMinoImage();
        } else if ("52".equals(mode)) {
            minoImage = new WorldMinoImage();
        } else if (mode.charAt(0) == '3') {
            // LEVEL
            playConfig = new TgmPlayConfig((mode.charAt(1) - '0') * 100);
        } else if (mode.charAt(0) == '4') {
            // handicap
            handicapTime = (mode.charAt(1) - '0') * 60 * 60 + (mode.charAt(2) - '0') * 600;
        }
        return this;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.white);

        int y = 120;
        for (String menu: menuList) {
            int ix = menu.indexOf(':');
            String mode = menu.substring(0, ix);
            if (curMenuList.contains(mode)) {
                if (selectMode.equals(mode)) {
                    // 選択中
                    g.setColor(Color.red);
                    g.fillRect(32, y - 2, 16 * 8, 20);
                    g.setColor(Color.yellow);
                } else {
                    g.setColor(Color.white);
                }
                g.drawString(menu.substring(ix + 1), 40, y);
                String txt = selectTextMap.get(mode);
                if (txt != null) {
                    g.setColor(new Color(0xc0, 0xc0, 0xff));
                    g.drawString(txt, 48, y + 16);
                }
                y += 40;
            }
        }
    }

    private void makeMenu() {
        System.out.println(selectTextMap);
        curMenuList.clear();
        for (String menu: menuList) {
            int ix = menu.indexOf(':');
            String mode = menu.substring(0, ix);
            if (menu.startsWith(selectLevel) && ix == selectLevel.length() + 1) {
                // 現在のメニュー
                if (selectMode == null) {
                    selectMode = mode;
                }
                curMenuList.add(mode);
            }
        }
    }

    public KeyInputData getInputData() {
        return inputData;
    }

    public IMinoConfig getMinoConfig() {
        return minoConfig;
    }

    public IMinoImage getMinoImage() {
        return minoImage;
    }

    public IPlayConfig getPlayConfig() {
        return playConfig;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }
}
