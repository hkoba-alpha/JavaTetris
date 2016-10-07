package tetris.play.game;

import org.newdawn.slick.*;
import org.newdawn.slick.command.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import tetris.data.KeyInputData;
import tetris.play.KeyAction;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hkoba on 2016/09/22.
 */
public class KeyConfigGame extends BasicGameState implements InputProviderListener {
    public static final int STATE_ID = 1;

    private Map<String, Integer> keyMap = new HashMap<>();
    private Map<String, String[]> keyNameMap = new HashMap<>();

    /**
     * 選択位置
     */
    private int inputNumber = -1;

    private enum ConfigMode {
        SELECT_CONTROLLER,
        KEY_INPUT,
        ENTER_WAIT
    }

    private enum KeyMenuList {
        MOVE_LEFT("MOVE LEFT ", "MOVE_LEFT"),
        MOVE_RIGHT("MOVE RIGHT", "MOVE_RIGHT"),
        MOVE_DROP("MOVE DROP ", "MOVE_DROP"),
        HARD_DROP("HARD DROP ", "HARD_DROP"),
        TURN_LEFT("TURN LEFT ", "TURN_LEFT"),
        TURN_RIGHT("TURN RIGHT", "TURN_RIGHT"),
        HOLD_BUTTON("HOLD      ", "HOLD_BUTTON"),
        START_BUTTON("START     ", "START_BUTTON");

        private String keyName;
        private String fieldName;

        KeyMenuList(String key, String field) {
            keyName = key;
            fieldName = field;
        }
    }

    private class KeyConfigData {
        /**
         * 0 or 1
         */
        private int playerIndex;
        /**
         * -1: key board
         * 0-: controller
         */
        private int controllerIndex;
        private Map<String, Integer> keyMap = new HashMap<>();

        private KeyConfigData(int index, int player) {
            controllerIndex = index;
            playerIndex = player;
            for (Field field: KeyInputData.class.getDeclaredFields()) {
                keyMap.put(field.getName(), null);
            }
            if (index == -1 && player == 0) {
                // Player 1のキーボード
                keyMap.put(KeyMenuList.MOVE_LEFT.fieldName, Input.KEY_LEFT);
                keyMap.put(KeyMenuList.MOVE_RIGHT.fieldName, Input.KEY_RIGHT);
                keyMap.put(KeyMenuList.MOVE_DROP.fieldName, Input.KEY_DOWN);
                keyMap.put(KeyMenuList.HARD_DROP.fieldName, Input.KEY_UP);
                keyMap.put(KeyMenuList.TURN_LEFT.fieldName, Input.KEY_Z);
                keyMap.put(KeyMenuList.TURN_RIGHT.fieldName, Input.KEY_X);
                keyMap.put(KeyMenuList.HOLD_BUTTON.fieldName, Input.KEY_SPACE);
                keyMap.put(KeyMenuList.START_BUTTON.fieldName, Input.KEY_ENTER);
            }
            loadData();
        }

        private File getSaveFile() {
            if (controllerIndex < 0) {
                return new File("temp/keyboard_" + playerIndex + ".dat");
            } else {
                return new File("temp/controller_" + controllerIndex + ".dat");
            }
        }

        private void loadData() {
            try {
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(getSaveFile()));
                playerIndex = is.readInt();
                keyMap = (Map<String, Integer>) is.readObject();
                is.close();
            } catch (FileNotFoundException e) {
                // 無視
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        private void saveData() {
            try {
                new File("temp").mkdirs();
                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(getSaveFile()));
                os.writeInt(playerIndex);
                os.writeObject(keyMap);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void addListener(InputProvider provider, KeyInputData inputData) {
            keyMap.entrySet().forEach(v -> {
                if (v.getValue() == null) {
                    return;
                }
                try {
                    Field field = KeyInputData.class.getDeclaredField(v.getKey());
                    if (controllerIndex >= 0) {
                        int button = v.getValue();
                        switch (button) {
                            case 0: // left
                                provider.bindCommand(new ControllerDirectionControl(controllerIndex, ControllerDirectionControl.LEFT), (Command) field.get(inputData));
                                break;
                            case 1: // right
                                provider.bindCommand(new ControllerDirectionControl(controllerIndex, ControllerDirectionControl.RIGHT), (Command) field.get(inputData));
                                break;
                            case 2: // up
                                provider.bindCommand(new ControllerDirectionControl(controllerIndex, ControllerDirectionControl.UP), (Command) field.get(inputData));
                                break;
                            case 3: // down
                                provider.bindCommand(new ControllerDirectionControl(controllerIndex, ControllerDirectionControl.DOWN), (Command) field.get(inputData));
                                break;
                            default:
                                provider.bindCommand(new ControllerButtonControl(controllerIndex, button - 3), (Command) field.get(inputData));
                                break;
                        }
                    } else {
                        provider.bindCommand(new KeyControl(v.getValue()), (Command) field.get(inputData));
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private ConfigMode configMode;

    private List<Field> inputKeyList = new ArrayList<>();

    private List<KeyConfigData> configDataList = new ArrayList<>();

    private KeyInputData[] inputDataList = new KeyInputData[2];

    public KeyConfigGame() {
        inputDataList[0] = new KeyInputData();
        inputDataList[1] = new KeyInputData();
        for (Field field: Input.class.getDeclaredFields()) {
            if (field.getName().startsWith("KEY_")) {
                inputKeyList.add(field);
            }
        }
        configMode = ConfigMode.SELECT_CONTROLLER;
    }

    /**
     * リスナを設定する
     * @param input
     * @throws SlickException
     */
    private void initInputListener(Input input) throws SlickException {
        input.clearControlPressedRecord();
        input.clearKeyPressedRecord();
        input.removeAllListeners();

        InputProvider provider = new InputProvider(input);
        provider.addListener(this);

        for (KeyConfigData configData: configDataList) {
            configData.saveData();
            configData.addListener(provider, inputDataList[configData.playerIndex]);
        }
    }

    @Override
    public int getID() {
        return STATE_ID;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        configDataList.add(new KeyConfigData(-1, 0));
        configDataList.add(new KeyConfigData(-1, 1));
        for (int i = 0; i < container.getInput().getControllerCount(); i++) {
            configDataList.add(new KeyConfigData(i, i & 1));
        }
    }

    private void renderSelectMode(Graphics g) {
        g.setColor(Color.white);
        List<String[]> menuList = new ArrayList<>();
        menuList.add(new String[]{null, (inputNumber < 0 ? "*": "") + "PUSH RETURN TO START"});
        int ix = 0;
        for (KeyConfigData configData: configDataList) {
            String[] menu = new String[3];
            if (configData.controllerIndex < 0) {
                menu[0] = "KEY BOARD";
            } else {
                menu[0] = "CONTROLLER " + (configData.controllerIndex + 1);
            }
            menu[1] = "PLAYER 1";
            menu[2] = "PLAYER 2";
            if (configData.playerIndex == 0) {
                menu[2] = "-" + menu[2];
                if (ix == inputNumber) {
                    menu[1] = "*" + menu[1];
                }
            } else {
                menu[1] = "-" + menu[1];
                if (ix == inputNumber) {
                    menu[2] = "*" + menu[2];
                }
            }
            menuList.add(menu);
            ix++;
        }
        int y = 30;
        for (String[] menu: menuList) {
            for (int i = 0; i < menu.length; i++) {
                if (menu[i] == null) {
                    continue;
                }
                int sx = 0;
                int x = i * 120 + 40;
                switch (menu[i].charAt(0)) {
                    case '*':
                        sx = 1;
                        g.setColor(Color.yellow);
                        g.drawRect(x - 4, y - 4, menu[i].length() * 10, 24);
                        break;
                    case '-':
                        sx = 1;
                        g.setColor(Color.darkGray);
                        break;
                    default:
                        g.setColor(Color.white);
                        break;
                }
                g.drawString(menu[i].substring(sx), x, y);
            }
            y += 32;
        }
    }

    private void renderInputMode(Graphics g) {
        int y = 40;
        g.setColor(Color.white);
        boolean firstFlag = true;
        for (KeyMenuList data: KeyMenuList.values()) {
            String[] text = keyNameMap.get(data.keyName);
            if (!firstFlag || keyMap.containsKey(data.keyName)) {
                g.setColor(Color.white);
            } else {
                firstFlag = false;
                g.setColor(Color.yellow);
                g.drawRect(42, y - 4, 230, 24);
            }
            g.drawString(data.keyName + " " + text[0], 50, y);
            if (text[1] != null) {
                g.setColor(Color.yellow);
                g.drawString(text[1], 250, y);
            }
            y += 32;
        }
        if (firstFlag) {
            g.setColor(Color.yellow);
            g.drawString("[PUSH RETURN KEY]", 50, y);
        }
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        switch (configMode) {
            case SELECT_CONTROLLER:
                renderSelectMode(g);
                break;
            default:
                renderInputMode(g);
                break;
        }
    }

    private void checkKeyBoard(Input input) {
        for (Field field: inputKeyList) {
            try {
                Integer key = (Integer)field.get(null);
                if (input.isKeyPressed(key)) {
                    for (KeyMenuList data: KeyMenuList.values()) {
                        if (!keyMap.containsKey(data.keyName)) {
                            keyMap.put(data.keyName, key);
                            keyNameMap.get(data.keyName)[1] = getKeyName(key, true);
                            break;
                        }
                    }
                    if (keyMap.size() == KeyMenuList.values().length) {
                        // 終了
                        configMode = ConfigMode.ENTER_WAIT;
                    }
                    input.clearKeyPressedRecord();
                    break;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    private void checkController(Input input) {
        for (int i = 0; i < 100; i++) {
            if (input.isControlPressed(i, inputNumber - 2)) {
                // ボタンを押した
                for (KeyMenuList data: KeyMenuList.values()) {
                    if (!keyMap.containsKey(data.keyName)) {
                        keyMap.put(data.keyName, i);
                        keyNameMap.get(data.keyName)[1] = getKeyName(i, false);
                        break;
                    }
                }
                if (keyMap.size() == KeyMenuList.values().length) {
                    // 終了
                    configMode = ConfigMode.ENTER_WAIT;
                }
                input.clearKeyPressedRecord();
                input.clearControlPressedRecord();
                break;
            }
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if (configMode == ConfigMode.ENTER_WAIT) {
            if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
                configMode = ConfigMode.SELECT_CONTROLLER;
                // 確定
                KeyConfigData configData = configDataList.get(inputNumber);
                for (KeyMenuList menu: KeyMenuList.values()) {
                    configData.keyMap.put(menu.fieldName, keyMap.get(menu.keyName));
                }
            } else if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
                configMode = ConfigMode.KEY_INPUT;
                container.getInput().clearKeyPressedRecord();
                container.getInput().clearControlPressedRecord();
                keyMap.clear();
                keyNameMap.values().forEach(v -> v[1] = null);
            }
            return;
        } else if (configMode == ConfigMode.KEY_INPUT) {
            // 入力
            if (inputNumber < 0) {
                checkKeyBoard(container.getInput());
            } else {
                checkController(container.getInput());
            }
            return;
        }
        // 選択モード
        if (container.getInput().isKeyPressed(Input.KEY_UP) && inputNumber > -1) {
            inputNumber--;
        } else if (container.getInput().isKeyPressed(Input.KEY_DOWN) && inputNumber < configDataList.size() - 1) {
            inputNumber++;
        } else if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
            if (inputNumber >= 0) {
                KeyConfigData configData = configDataList.get(inputNumber);
                if (configData.controllerIndex >= 0 && configData.playerIndex > 0) {
                    configData.playerIndex--;
                }
            }
        } else if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            if (inputNumber >= 0) {
                KeyConfigData configData = configDataList.get(inputNumber);
                if (configData.controllerIndex >= 0 && configData.playerIndex < 1) {
                    configData.playerIndex++;
                }
            }
        } else if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
            container.getInput().clearKeyPressedRecord();
            container.getInput().clearControlPressedRecord();
            if (inputNumber < 0) {
                // 開始
                initInputListener(container.getInput());
                ((TetrisPlayGame)game.getState(TetrisPlayGame.STATE_ID)).setInputData(inputDataList);
                game.enterState(0);
            } else {
                configMode = ConfigMode.KEY_INPUT;
                keyMap.clear();
                KeyConfigData configData = configDataList.get(inputNumber);
                boolean keyFlag = configData.controllerIndex < 0;
                for (KeyMenuList menu: KeyMenuList.values()) {
                    keyNameMap.put(menu.keyName, new String[]{getKeyName(configData.keyMap.get(menu.fieldName), keyFlag), null});
                }
            }
        }
    }

    private String getKeyName(Integer keyCode, boolean keyFlag) {
        if (keyCode == null) {
            return "<NONE>";
        }
        if (!keyFlag) {
            switch (keyCode) {
                case 0:
                    return "LEFT";
                case 1:
                    return "RIGHT";
                case 2:
                    return "UP";
                case 3:
                    return "DOWN";
                default:
                    return "BUTTON" + (keyCode - 3);
            }
        }
        for (Field field: inputKeyList) {
            try {
                if (keyCode.equals(field.get(null))) {
                    return field.getName().substring(4);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return "<UNKNOWN>";
    }

    @Override
    public void controlPressed(Command command) {
        ((KeyAction)command).event(true);
    }

    @Override
    public void controlReleased(Command command) {
        ((KeyAction)command).event(false);
    }
}
